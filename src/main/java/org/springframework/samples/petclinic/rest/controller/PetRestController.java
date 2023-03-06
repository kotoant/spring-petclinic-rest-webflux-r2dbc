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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.rest.api.PetsApi;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.service.ReactiveClinicService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api")
public class PetRestController implements PetsApi {

    private final ReactiveClinicService reactiveClinicService;

    private final PetMapper petMapper;

    public PetRestController(ReactiveClinicService reactiveClinicService, PetMapper petMapper) {
        this.reactiveClinicService = reactiveClinicService;
        this.petMapper = petMapper;
    }

//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<PetDto>> getPet(Integer petId) {
        return reactiveClinicService.findPetById(petId).map(petMapper::toPetDto).map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<List<PetDto>>> listPets() {
        return reactiveClinicService.findAllPets()
            .collectList().map(petMapper::toPetsDto).map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }


//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<PetDto>> updatePet(Integer petId, PetDto petDto) {
        return reactiveClinicService.findPetById(petId).flatMap(currentPet -> {
                currentPet.setBirthDate(petDto.getBirthDate());
                currentPet.setName(petDto.getName());
                currentPet.setType(petMapper.toPetType(petDto.getType()));
                currentPet.setTypeId(currentPet.getType().getId());
                return reactiveClinicService.savePet(currentPet);
            }).map(pet -> new ResponseEntity<>(petMapper.toPetDto(pet), HttpStatus.OK))
            .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

}
