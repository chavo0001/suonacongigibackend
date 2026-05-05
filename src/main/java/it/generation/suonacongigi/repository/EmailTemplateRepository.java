package it.generation.suonacongigi.repository;

import it.generation.suonacongigi.model.EmailTemplate;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


// Repository interface for Genre entity, provides CRUD operations and custom queries.

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    //
    Optional<EmailTemplate> findByName(String name);
    
    //
    boolean existsByNameIgnoreCase(String name);
}