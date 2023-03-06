package org.springframework.samples.petclinic.r2dbc;

import org.springframework.samples.petclinic.model.Visit;
import reactor.core.publisher.Mono;

public class CustomVisitRepositoryImpl implements CustomVisitRepository {

    private final R2dbcOwnerRepository ownerRepository;
    private final R2dbcPetRepository petRepository;
    private final R2dbcVisitRepository visitRepository;

    public CustomVisitRepositoryImpl(R2dbcOwnerRepository ownerRepository, R2dbcPetRepository petRepository, R2dbcVisitRepository visitRepository) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.visitRepository = visitRepository;
    }

    @Override
    public Mono<Visit> findById(int id) {
        return visitRepository.findById(id)
            .zipWhen(visit -> petRepository.findById(visit.getPetId()))
            .zipWhen(tuple -> ownerRepository.findById(tuple.getT2().getOwnerId())).map(tuple -> {
                Visit visit = tuple.getT1().getT1();
                visit.setPet(tuple.getT1().getT2());
                visit.getPet().setOwner(tuple.getT2());
                return visit;
            });
    }

}
