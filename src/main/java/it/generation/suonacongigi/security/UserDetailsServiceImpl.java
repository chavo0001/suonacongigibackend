package it.generation.suonacongigi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.repository.user.UserRepository;

import java.util.Objects;

/**
 * ARCHITETTURA: The Security Bridge.
 * Questo è l'unico punto di contatto tra il Database e il motore di Security.
 * Implementando 'UserDetailsService', forniamo a Spring il "manuale" per cercare 
 * gli utenti durante il Login o la validazione del Token.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @NonNull
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. CERTIFICAZIONE LOCALE: Fail-fast all'ingresso.
        Assert.hasText(username, "Lo username/email non può essere vuoto");
        String cleanUsername = Objects.requireNonNull(username);

        // 2. LOGICA MULTI-LOGIN: Cerchiamo l'utente sia per username che per email.
        // MECCANICA: Sfruttiamo gli Optional di JPA per una ricerca fluida.
        User user = userRepository.findByUsername(cleanUsername)
                .or(() -> userRepository.findByEmail(cleanUsername))
                .orElseThrow(() -> 
                        new UsernameNotFoundException("Utente non trovato con username o email: " + cleanUsername));

        // 3. CASTING CERTIFICATO: La nostra classe User implementa UserDetails via Reflection.
        return (UserDetails) Objects.requireNonNull(user);
    }
}