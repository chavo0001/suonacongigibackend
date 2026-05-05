package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * L'entità ParolaBandita rappresenta una parola che deve essere censurata dal
 * sistema.
 * Ogni riga della tabella contiene una parola proibita che sarà trasformata in
 * asterischi.
 */

@Entity
@Table(name = "parole_bandite", indexes = {
        // Indice per velocizzare le ricerche per parola (il database controlla qui
        // prima di cercare ovunque)
        @Index(name = "idx_word", columnList = "word")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParolaBandita {

    // Questo è il numero ID della parola bandita (come il numero di telefono che
    // identifica una persona)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Questa è la parola che vogliamo censurare (es: "parolaccia"). Il database non
    // permette duplicati (UNIQUE)
    @Column(nullable = false, unique = true, length = 100)
    private String word;

    // La categoria ci aiuta a capire che tipo di parola è (es: "bestemmia",
    // "insulto", "spam")
    @Column(length = 50)
    private String category;

    // La data di creazione (quando è stata aggiunta questa parola alla lista nera)
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // La data di ultimo aggiornamento (quando è stata modificata per ultima)
    @Column(name = "updated_at", insertable = false, updatable = true)
    private LocalDateTime updatedAt;
}
