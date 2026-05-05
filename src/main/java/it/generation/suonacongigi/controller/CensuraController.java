package it.generation.suonacongigi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/users/me/censura")
@RequiredArgsConstructor
@Tag(name = "Censura", description = "Toggle del filtro censure per l'utente loggato")
public class CensuraController extends BaseController {

    private final UserRepository userRepository;

    @Operation(summary = "Leggi stato filtro", description = "Restituisce TRUE se il filtro censure e attivo per l'utente corrente")
    @GetMapping
    public ResponseEntity<ApiEnvelope<Map<String, Boolean>>> getStatoCensura(
            @AuthenticationPrincipal User currentUser) {

        User user = userRepository.findByUsername(
                Objects.requireNonNull(currentUser.getUsername()))
                .orElseThrow();

        return ok(Map.of("censuraAttiva", user.isCensuraAttiva()), "Stato filtro recuperato");
    }

    @Operation(summary = "Aggiorna filtro", description = "Attiva o disattiva il filtro censure per l'utente corrente")
    @PutMapping
    public ResponseEntity<ApiEnvelope<Map<String, Boolean>>> setCensura(
            @AuthenticationPrincipal User currentUser,
            @RequestBody Map<String, Boolean> body) {

        boolean nuovoStato = Objects.requireNonNullElse(body.get("attiva"), true);

        User user = userRepository.findByUsername(
                Objects.requireNonNull(currentUser.getUsername()))
                .orElseThrow();

        user.setCensuraAttiva(nuovoStato);
        userRepository.save(user);

        String messaggio = nuovoStato
                ? "Filtro censure attivato con successo"
                : "Filtro censure disattivato";

        return ok(Map.of("censuraAttiva", nuovoStato), messaggio);
    }
}
