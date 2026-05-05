package it.generation.suonacongigi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.generation.suonacongigi.dto.common.ApiEnvelope;
import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/users/me/censura")
@RequiredArgsConstructor
@Tag(name = "Censura", description = "Toggle del filtro censure per l'utente loggato")
public class CensuraController extends BaseController {

        private final UserRepository userRepository;

        // Restituisce lo stato attuale del toggle.
        @Operation(summary = "Leggi stato filtro", description = "Restituisce TRUE se il filtro censure è attivo per l'utente corrente")
        @GetMapping
        public ResponseEntity<ApiEnvelope<Map<String, Boolean>>> getStatoCensura(
                        @AuthenticationPrincipal User currentUser) {

                User user = userRepository.findByUsername(
                                Objects.requireNonNull(currentUser.getUsername()))
                                .orElseThrow();

                // Risponde con: { "censuraAttiva": true } oppure { "censuraAttiva": false }
                return ok(Map.of("censuraAttiva", user.isCensuraAttiva()), "Stato filtro recuperato");
        }

        // "Cambia il mio filtro"
        // Body JSON: { "attiva": true } oppure { "attiva": false }
        @Operation(summary = "Aggiorna filtro", description = "Attiva o disattiva il filtro censure per l'utente corrente")
        @PutMapping
        public ResponseEntity<ApiEnvelope<Map<String, Boolean>>> setCensura(
                        @AuthenticationPrincipal User currentUser,
                        @RequestBody Map<String, Boolean> body) {

                // Prendiamo il valore "attiva" dal body (o TRUE se non specificato)
                boolean nuovoStato = Objects.requireNonNullElse(body.get("attiva"), true);

                // Carichiamo l'utente, aggiorniamo il campo, salviamo
                User user = userRepository.findByUsername(
                                Objects.requireNonNull(currentUser.getUsername()))
                                .orElseThrow();

                user.setCensuraAttiva(nuovoStato);
                userRepository.save(user);

                // Messaggio per il frontend
                String messaggio = nuovoStato
                                ? "🛡️ Filtro censure attivato con successo"
                                : "🔓 Filtro censure disattivato";

                return ok(Map.of("censuraAttiva", nuovoStato), messaggio);
        }
}
