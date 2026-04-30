package it.generation.suonacongigi.repository;

import it.generation.suonacongigi.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Repository interface for Genre entity, provides CRUD operations and custom queries.

public interface GenreRepository extends JpaRepository<Genre, Long> {
    //
    Optional<Genre> findByNameIgnoreCase(String name);
    
    //
    boolean existsByNameIgnoreCase(String name);
}