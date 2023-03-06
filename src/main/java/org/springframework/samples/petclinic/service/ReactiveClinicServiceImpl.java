package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.r2dbc.OwnerRepository;
import org.springframework.samples.petclinic.r2dbc.PetRepository;
import org.springframework.samples.petclinic.r2dbc.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ReactiveClinicServiceImpl implements ReactiveClinicService {

    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final VisitRepository visitRepository;

    public ReactiveClinicServiceImpl(OwnerRepository ownerRepository, PetRepository petRepository, VisitRepository visitRepository) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.visitRepository = visitRepository;
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    public Mono<Pet> findPetById(int id) {
        return petRepository.findById(id);
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    public Flux<Pet> findAllPets() {
        return petRepository.findAll();
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager")
    public Mono<Pet> savePet(Pet pet) {
        return petRepository.save(pet);
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    public Mono<Visit> findVisitById(int visitId) {
        return visitRepository.findById(visitId);
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    public Flux<Visit> findAllVisits() {
        return visitRepository.findAll();
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager")
    public Mono<Visit> saveVisit(Visit visit) {
        return visitRepository.save(visit);
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    public Mono<Owner> findOwnerById(int id) {
        return ownerRepository.findById(id);
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    public Flux<Owner> findAllOwners() {
        return ownerRepository.findAll();
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager")
    public Mono<Owner> saveOwner(Owner owner) {
        return ownerRepository.save(owner);
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    public Flux<Owner> findOwnerByLastName(String lastName) {
        return ownerRepository.findByLastName(lastName);
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    public Flux<PetType> findAllPetTypes() {
        return null;
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager", readOnly = true)
    public Flux<PetType> findPetTypes() {
        return null;
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager")
    public Mono<PetType> savePetType(PetType petType) {
        return null;
    }

    @Override
    @Transactional(transactionManager = "connectionFactoryTransactionManager")
    public Mono<Void> deletePetType(PetType petType) {
        return null;
    }
}
