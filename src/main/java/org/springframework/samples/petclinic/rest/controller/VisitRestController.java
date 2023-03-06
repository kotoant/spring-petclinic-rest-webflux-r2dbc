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
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.rest.api.VisitsApi;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
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
public class VisitRestController implements VisitsApi {

    private final ReactiveClinicService reactiveClinicService;

    private final VisitMapper visitMapper;

    public VisitRestController(ReactiveClinicService reactiveClinicService, VisitMapper visitMapper) {
        this.reactiveClinicService = reactiveClinicService;
        this.visitMapper = visitMapper;
    }


//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<List<VisitDto>>> listVisits() {
        return reactiveClinicService.findAllVisits().collectList().map(visitMapper::toVisitsDto).map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<VisitDto>> getVisit(Integer visitId) {
        return reactiveClinicService.findVisitById(visitId).map(visitMapper::toVisitDto).map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

//    @PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
    @Override
    public Mono<ResponseEntity<VisitDto>> updateVisit(Integer visitId, VisitDto visitDto) {
        return reactiveClinicService.findVisitById(visitId).flatMap(currentVisit -> {
                currentVisit.setDate(visitDto.getDate());
                currentVisit.setDescription(visitDto.getDescription());
                return reactiveClinicService.saveVisit(currentVisit);
            }).map(visitMapper::toVisitDto).map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(new ResponseEntity<>(HttpStatus.NOT_FOUND)));
    }

}
