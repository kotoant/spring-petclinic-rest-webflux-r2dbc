package org.springframework.samples.petclinic.r2dbc;

import org.jooq.Records;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Visit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;
import static org.springframework.samples.petclinic.jooq.Tables.PETS;
import static org.springframework.samples.petclinic.jooq.Tables.TYPES;
import static org.springframework.samples.petclinic.jooq.Tables.VISITS;

public class CustomOwnerRepositoryImpl implements CustomOwnerRepository {

    private final R2dbcOwnerRepository repository;
    private final DSLAccess dslAccess;

    public CustomOwnerRepositoryImpl(R2dbcOwnerRepository repository, DSLAccess dslAccess) {
        this.repository = repository;
        this.dslAccess = dslAccess;
    }

    @Override
    public Flux<Owner> findByLastName(String lastName) {
        return loadPetsAndVisits(repository.findOwnersByLastNameStartsWith(lastName));
    }

    @Override
    public Mono<Owner> findById(int id) {
        return loadPetsAndVisits(repository.findById(id));
    }

    private Flux<Owner> loadPetsAndVisits(Flux<Owner> owners) {
        return owners.collectList()
            .flatMapMany(list -> Flux.concat(list.stream().map(this::loadPetsAndVisits).collect(Collectors.toList())));
    }

    private Mono<Owner> loadPetsAndVisits(Mono<Owner> ownerMono) {
        return ownerMono.flatMap(this::loadPetsAndVisits);
    }

    private Mono<Owner> loadPetsAndVisits(Owner owner) {
        return dslAccess.withDSLContext(ctx -> Flux.from(
                ctx.select(PETS.ID, PETS.NAME, PETS.BIRTH_DATE, PETS.TYPE_ID, PETS.OWNER_ID, TYPES.ID, TYPES.NAME,
                        multiset(
                            select(VISITS.ID, VISITS.PET_ID, VISITS.VISIT_DATE, VISITS.DESCRIPTION)
                                .from(VISITS)
                                .where(VISITS.PET_ID.eq(PETS.ID))
                        ).as("visits").convertFrom(r -> r.map(Records.mapping(VisitRecord::new)))
                    )
                    .from(PETS.join(TYPES).on(PETS.TYPE_ID.eq(TYPES.ID)))
                    .where(PETS.OWNER_ID.eq(owner.getId()))
            )
            .map(Records.mapping(PetRecord::new))
            .map(record -> {
                var pet = new Pet();
                pet.setId(record.id);
                pet.setOwner(owner);
                pet.setOwnerId(owner.getId());
                pet.setType(new PetType(record.typeId, record.typeName));
                pet.setName(record.name);
                pet.setBirthDate(record.birthDate);
                pet.setVisits(record.visits.stream().map(v -> new Visit(v.id, v.petId, v.visitDate, v.description)).collect(Collectors.toList()));
                return pet;
            })
            .collectList().doOnNext(owner::setPets).thenReturn(owner));
    }

    record VisitRecord(int id, int petId, LocalDate visitDate, String description) {
    }

    record PetRecord(int id, String name, LocalDate birthDate, int petId, int ownerId, int typeId, String typeName,
                     List<VisitRecord> visits) {
    }

}
