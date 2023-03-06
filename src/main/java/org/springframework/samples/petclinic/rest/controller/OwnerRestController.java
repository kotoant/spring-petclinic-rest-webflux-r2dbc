/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ReactiveClinicService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestController implements OwnersApi {

    private static final Logger logger = LoggerFactory.getLogger(OwnerRestController.class);

    private final ReactiveClinicService reactiveClinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    public OwnerRestController(ReactiveClinicService reactiveClinicService,
                               OwnerMapper ownerMapper,
                               PetMapper petMapper,
                               VisitMapper visitMapper) {
        this.reactiveClinicService = reactiveClinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
    }

//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<List<OwnerDto>>> listOwners(String lastName) {
        return (lastName != null
            ? reactiveClinicService.findOwnerByLastName(lastName)
            : reactiveClinicService.findAllOwners())
            .collectList().map(ownerMapper::toOwnerDtoCollection).map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<OwnerDto>> getOwner(Integer ownerId) {
        return reactiveClinicService.findOwnerById(ownerId).map(ownerMapper::toOwnerDto).map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<OwnerDto>> addOwner(OwnerFieldsDto ownerFieldsDto) {
        return reactiveClinicService.saveOwner(ownerMapper.toOwner(ownerFieldsDto)).map(owner -> {
            HttpHeaders headers = new HttpHeaders();
            OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
            headers.setLocation(UriComponentsBuilder.newInstance()
                .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
            return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
        });
    }

//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<OwnerDto>> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        return reactiveClinicService.findOwnerById(ownerId).flatMap(currentOwner -> {
                currentOwner.setAddress(ownerFieldsDto.getAddress());
                currentOwner.setCity(ownerFieldsDto.getCity());
                currentOwner.setFirstName(ownerFieldsDto.getFirstName());
                currentOwner.setLastName(ownerFieldsDto.getLastName());
                currentOwner.setTelephone(ownerFieldsDto.getTelephone());
                return reactiveClinicService.saveOwner(currentOwner);
            }).map(ownerMapper::toOwnerDto).map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<PetDto>> addPetToOwner(Integer ownerId, PetFieldsDto petFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Pet pet = petMapper.toPet(petFieldsDto);
        Owner owner = new Owner();
        owner.setId(ownerId);
        pet.setOwner(owner);
        pet.setOwnerId(ownerId);
        pet.setTypeId(petFieldsDto.getType().getId());
        return reactiveClinicService.savePet(pet).map(saved -> {
            PetDto petDto = petMapper.toPetDto(saved);
            headers.setLocation(UriComponentsBuilder.newInstance().path("/api/pets/{id}")
                .buildAndExpand(saved.getId()).toUri());
//            logger.info("addPetToOwner: headers: {}", headers);
//            logger.info("addPetToOwner: petDto: {}", petDto);
            return new ResponseEntity<>(petDto, headers, HttpStatus.CREATED);
        });
    }

//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<VisitDto>> addVisitToOwner(Integer ownerId, Integer petId, VisitFieldsDto visitFieldsDto) {
        HttpHeaders headers = new HttpHeaders();
        Visit visit = visitMapper.toVisit(visitFieldsDto);
        Pet pet = new Pet();
        pet.setId(petId);
        visit.setPet(pet);
        visit.setPetId(petId);
        return reactiveClinicService.saveVisit(visit).map(saved -> {
            VisitDto visitDto = visitMapper.toVisitDto(saved);
            headers.setLocation(UriComponentsBuilder.newInstance().path("/api/visits/{id}")
                .buildAndExpand(saved.getId()).toUri());
//            logger.info("addVisitToOwner: headers: {}", headers);
//            logger.info("addVisitToOwner: visitDto: {}", visitDto);
            return new ResponseEntity<>(visitDto, headers, HttpStatus.CREATED);
        });
    }

}
