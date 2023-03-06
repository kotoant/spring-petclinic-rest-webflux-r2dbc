package org.springframework.samples.petclinic.r2dbc;

import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.util.EntityUtils;
import reactor.core.publisher.Mono;

public class CustomPetRepositoryImpl implements CustomPetRepository {

    private final R2dbcPetRepository repository;
    private final OwnerRepository ownerRepository;

    public CustomPetRepositoryImpl(R2dbcPetRepository repository, OwnerRepository ownerRepository) {
        this.repository = repository;
        this.ownerRepository = ownerRepository;
    }

    @Override
    public Mono<Pet> findById(int id) {
        return repository.findById(id)
            .flatMap(pet -> ownerRepository.findById(pet.getOwnerId()))
            .map(owner -> EntityUtils.getById(owner.getPets(), Pet.class, id));
    }

}
