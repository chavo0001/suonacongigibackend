package it.generation.suonacongigi.repository;

import it.generation.suonacongigi.model.Artist; 
import org.springframework.data.jpa.repository.JpaRepository; 
import java.util.Optional;

// Il repository ArtistRepository estende JpaRepository per fornire operazioni CRUD sull'entità Artist.

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // Query Method derivato: ricerca case-insensitive
    Optional<Artist> findByNameIgnoreCase(String name);

    // Questo metodo verifica se esiste un artista con il nome specificato (case-insensitive)
    boolean existsByNameIgnoreCase(String name);
}