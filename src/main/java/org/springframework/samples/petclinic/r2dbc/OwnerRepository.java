package org.springframework.samples.petclinic.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.samples.petclinic.model.Owner;

public interface OwnerRepository extends R2dbcRepository<Owner, Integer>, CustomOwnerRepository {
}
