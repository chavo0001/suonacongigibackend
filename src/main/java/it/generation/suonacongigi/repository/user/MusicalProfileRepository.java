package it.generation.suonacongigi.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import it.generation.suonacongigi.model.MusicalProfile;
import java.util.Optional;

/**
 * ARCHITETTURA: Data Access Layer.
 * Astrae l'accesso alla tabella musical_profiles.
 */
public interface MusicalProfileRepository extends JpaRepository<MusicalProfile, Long> {
    Optional<MusicalProfile> findByUserId(Long userId);
}