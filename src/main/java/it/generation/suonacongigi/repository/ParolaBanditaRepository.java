package it.generation.suonacongigi.repository;

import it.generation.suonacongigi.model.ParolaBandita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ParolaBanditaRepository extends JpaRepository<ParolaBandita, Long> {

    @Query("SELECT p.word FROM ParolaBandita p ORDER BY LENGTH(p.word) DESC")
    List<String> findAllParoleAttive();

    boolean existsByWordIgnoreCase(String word);
}
