package org.springframework.samples.petclinic.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.samples.petclinic.model.Visit;

public interface R2dbcVisitRepository extends R2dbcRepository<Visit, Integer> {
}
