package org.springframework.samples.petclinic.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.samples.petclinic.model.Owner;
import reactor.core.publisher.Flux;

public interface R2dbcOwnerRepository extends R2dbcRepository<Owner, Integer> {
    Flux<Owner> findOwnersByLastNameStartsWith(String lastNamePrefix);
}
