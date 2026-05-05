package it.generation.suonacongigi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import it.generation.suonacongigi.model.VerificationToken;

public interface TokenRepository extends JpaRepository<VerificationToken,Long>{

    Optional<VerificationToken> findByToken(String token);  

    @Override
    @NonNull
    <S extends VerificationToken> S save(@NonNull S entity);
    
}
