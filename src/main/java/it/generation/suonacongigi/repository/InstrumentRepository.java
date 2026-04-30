package it.generation.suonacongigi.repository;

import it.generation.suonacongigi.model.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}