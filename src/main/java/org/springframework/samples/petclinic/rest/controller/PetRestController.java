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

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.rest.api.PetsApi;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("api")
public class PetRestController implements PetsApi {

    private final ClinicService clinicService;

    private final PetMapper petMapper;

    private final Scheduler scheduler;

    public PetRestController(ClinicService clinicService, PetMapper petMapper, Scheduler scheduler) {
        this.clinicService = clinicService;
        this.petMapper = petMapper;
        this.scheduler = scheduler;
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<PetDto>> getPet(Integer petId) {
        return wrapBlockingCall(() -> {
            PetDto pet = petMapper.toPetDto(this.clinicService.findPetById(petId));
            if (pet == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(pet, HttpStatus.OK);
        });
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<List<PetDto>>> listPets() {
        return wrapBlockingCall(() -> {
            List<PetDto> pets = new ArrayList<>(petMapper.toPetsDto(this.clinicService.findAllPets()));
            if (pets.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(pets, HttpStatus.OK);
        });
    }


    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<PetDto>> updatePet(Integer petId, PetDto petDto) {
        return wrapBlockingCall(() -> {
            Pet currentPet = this.clinicService.findPetById(petId);
            if (currentPet == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            currentPet.setBirthDate(petDto.getBirthDate());
            currentPet.setName(petDto.getName());
            currentPet.setType(petMapper.toPetType(petDto.getType()));
            this.clinicService.savePet(currentPet);
            return new ResponseEntity<>(petMapper.toPetDto(currentPet), HttpStatus.OK);
        });
    }

    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Transactional
    @Override
    public Mono<ResponseEntity<PetDto>> deletePet(Integer petId) {
        return wrapBlockingCall(() -> {
            Pet pet = this.clinicService.findPetById(petId);
            if (pet == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            this.clinicService.deletePet(pet);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
