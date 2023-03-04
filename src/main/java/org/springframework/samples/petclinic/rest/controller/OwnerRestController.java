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
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestController implements OwnersApi {

    private final ClinicService clinicService;

    private final OwnerMapper ownerMapper;

    private final PetMapper petMapper;

    private final VisitMapper visitMapper;

    private final Scheduler scheduler;

    public OwnerRestController(ClinicService clinicService,
                               OwnerMapper ownerMapper,
                               PetMapper petMapper,
                               VisitMapper visitMapper,
                               Scheduler scheduler) {
        this.clinicService = clinicService;
        this.ownerMapper = ownerMapper;
        this.petMapper = petMapper;
        this.visitMapper = visitMapper;
        this.scheduler = scheduler;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<List<OwnerDto>>> listOwners(String lastName) {
        return wrapBlockingCall(() -> {
            Collection<Owner> owners;
            if (lastName != null) {
                owners = this.clinicService.findOwnerByLastName(lastName);
            } else {
                owners = this.clinicService.findAllOwners();
            }
            if (owners.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(ownerMapper.toOwnerDtoCollection(owners), HttpStatus.OK);
        });
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<OwnerDto>> getOwner(Integer ownerId) {
        return wrapBlockingCall(() -> {
            Owner owner = this.clinicService.findOwnerById(ownerId);
            if (owner == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), HttpStatus.OK);
        });
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<OwnerDto>> addOwner(OwnerFieldsDto ownerFieldsDto) {
        return wrapBlockingCall(() -> {
            HttpHeaders headers = new HttpHeaders();
            Owner owner = ownerMapper.toOwner(ownerFieldsDto);
            this.clinicService.saveOwner(owner);
            OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
            headers.setLocation(UriComponentsBuilder.newInstance()
                .path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
            return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
        });
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<OwnerDto>> updateOwner(Integer ownerId, OwnerFieldsDto ownerFieldsDto) {
        return wrapBlockingCall(() -> {
            Owner currentOwner = this.clinicService.findOwnerById(ownerId);
            if (currentOwner == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            currentOwner.setAddress(ownerFieldsDto.getAddress());
            currentOwner.setCity(ownerFieldsDto.getCity());
            currentOwner.setFirstName(ownerFieldsDto.getFirstName());
            currentOwner.setLastName(ownerFieldsDto.getLastName());
            currentOwner.setTelephone(ownerFieldsDto.getTelephone());
            this.clinicService.saveOwner(currentOwner);
            return new ResponseEntity<>(ownerMapper.toOwnerDto(currentOwner), HttpStatus.OK);
        });
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<PetDto>> addPetToOwner(Integer ownerId, PetFieldsDto petFieldsDto) {
        return wrapBlockingCall(() -> {
            HttpHeaders headers = new HttpHeaders();
            Pet pet = petMapper.toPet(petFieldsDto);
            Owner owner = new Owner();
            owner.setId(ownerId);
            pet.setOwner(owner);
            this.clinicService.savePet(pet);
            PetDto petDto = petMapper.toPetDto(pet);
            headers.setLocation(UriComponentsBuilder.newInstance().path("/api/pets/{id}")
                .buildAndExpand(pet.getId()).toUri());
            return new ResponseEntity<>(petDto, headers, HttpStatus.CREATED);
        });
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<VisitDto>> addVisitToOwner(Integer ownerId, Integer petId, VisitFieldsDto visitFieldsDto) {
        return wrapBlockingCall(() -> {
            HttpHeaders headers = new HttpHeaders();
            Visit visit = visitMapper.toVisit(visitFieldsDto);
            Pet pet = new Pet();
            pet.setId(petId);
            visit.setPet(pet);
            this.clinicService.saveVisit(visit);
            VisitDto visitDto = visitMapper.toVisitDto(visit);
            headers.setLocation(UriComponentsBuilder.newInstance().path("/api/visits/{id}")
                .buildAndExpand(visit.getId()).toUri());
            return new ResponseEntity<>(visitDto, headers, HttpStatus.CREATED);
        });
    }

    private <T> Mono<T> wrapBlockingCall(Callable<T> callable) {
        return Mono.fromCallable(callable).subscribeOn(scheduler);
    }

    private Mono<Void> wrapBlockingCall(Runnable runnable) {
        return wrapBlockingCall(() -> {
            runnable.run();
            return null;
        });
    }
}
