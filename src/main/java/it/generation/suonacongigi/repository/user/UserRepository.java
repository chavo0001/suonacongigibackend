package it.generation.suonacongigi.repository.user;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository; 
import org.springframework.lang.NonNull;
import it.generation.suonacongigi.model.User;
import java.util.List;
import java.util.Optional;

/**
 * ARCHITETTURA: Data Access Layer - User Domain.
 * Questa interfaccia combina la semplicità della Query Derivation con 
 * la potenza delle query JPQL ottimizzate.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    @Override
    @NonNull
    <S extends User> S save(@NonNull S entity);

    @Override
    @NonNull
    Optional<User> findById(@NonNull Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);

    @EntityGraph(attributePaths = {
        "musicalProfile", 
        "musicalProfile.genres", 
        "musicalProfile.instruments", 
        "musicalProfile.favoriteArtists"
    })
    List<User> findAllByOrderByUsernameAsc();
}