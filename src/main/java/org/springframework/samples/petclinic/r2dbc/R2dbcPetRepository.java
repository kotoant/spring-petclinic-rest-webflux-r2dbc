package org.springframework.samples.petclinic.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.samples.petclinic.model.Pet;

public interface R2dbcPetRepository extends R2dbcRepository<Pet, Integer> {
}
