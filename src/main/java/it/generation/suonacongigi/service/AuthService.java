package it.generation.suonacongigi.service;

import it.generation.suonacongigi.dto.auth.AuthResponse;
import it.generation.suonacongigi.dto.auth.LoginRequest;
import it.generation.suonacongigi.dto.auth.RegisterRequest; 
import it.generation.suonacongigi.dto.user.MusicalProfileRequest;
import it.generation.suonacongigi.model.MusicalProfile;
import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.repository.user.UserRepository;
import it.generation.suonacongigi.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import java.util.Objects;

/**
 * ARCHITETTURA: Security Orchestrator.
 * Questo service coordina l'AuthenticationManager (Engine) e la persistenza.
 * Rilevante: @Transactional garantisce l'atomicità tra salvataggio utente e 
 * generazione dello stato iniziale nel Database.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        Assert.notNull(req, "La richiesta non può essere nulla");
        
        // 1. Estrazione dati
        String username = Objects.requireNonNull(req.getUsername());
        String email    = Objects.requireNonNull(req.getEmail());
        String password = Objects.requireNonNull(req.getPassword());
        MusicalProfileRequest profileReq = Objects.requireNonNull(req.getMusicalProfile());

        // 2. Controlli unicità
        if (userRepository.existsByUsername(username))
            throw new IllegalArgumentException("Username già in uso");
        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email già registrata");

        // 3. Creazione User (con password hashata e ruolo di default)
        User userToSave = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(User.Role.USER)
                .build();

        // 4. Creazione MusicalProfile (con dati opzionali)
        MusicalProfile profile = Objects.requireNonNull(MusicalProfile.builder())
                .user(userToSave)
                .bio(profileReq.getBio() != null ? profileReq.getBio() : "")
                .build();
        
        // Colleghiamo le due entità (bidirezionale)
        userToSave.setMusicalProfile(profile);

        // 5. Aggiornamento collezioni ManyToMany
        userService.updateCollections(Objects.requireNonNull(profile), profileReq);

        // 6. Salvataggio e risposta
        User savedUser = userRepository.save(userToSave);
        String token   = Objects.requireNonNull(jwtUtil.generateToken(savedUser));

        return toAuthResponse(savedUser, token);
    }

    public AuthResponse login(LoginRequest req) {
        Assert.notNull(req, "La richiesta non può essere nulla");
        
        String username = Objects.requireNonNull(req.getUsername());
        String password = Objects.requireNonNull(req.getPassword());

        // MECCANICA: authManager delega a UserDetailsService (Reflection) la ricerca dell'utente
        // e al PasswordEncoder la verifica dell'hash.
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        Object principal = auth.getPrincipal();
        Assert.notNull(principal, "Principal nullo");
        
        if (!(principal instanceof User user)) {
            throw new IllegalStateException("Il principal autenticato non è un'istanza di User");
        }

        String token = Objects.requireNonNull(jwtUtil.generateToken(user));

        return toAuthResponse(user, token);
    }

    private AuthResponse toAuthResponse(User user, String token) {
        // Mapping certificato: trasformazione verso il contratto pubblico.
        return Objects.requireNonNull(AuthResponse.builder()
                .token(Objects.requireNonNull(token))
                .username(Objects.requireNonNull(user.getUsername()))
                .role(Objects.requireNonNull(user.getRole()).name())
                .build());
    }
}