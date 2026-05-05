package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// L'entità Event rappresenta un evento musicale, con campi per titolo, descrizione, data, luogo,
// numero massimo di posti e l'utente che lo ha creato.

@Entity
@Table(name = "events")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Event {
    // Il campo "id" è la chiave primaria dell'entità, generata automaticamente dal database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il campo "title" rappresenta il titolo dell'evento. È obbligatorio.
    @Column(nullable = false)
    private String title;

    // Il campo "description" rappresenta la descrizione dell'evento. Può essere lungo, quindi usiamo TEXT.
    @Column(columnDefinition = "TEXT")
    private String description;

    // Il campo "eventDate" rappresenta la data e ora dell'evento. È obbligatorio.
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    // Il campo "location" rappresenta il luogo dell'evento. È obbligatorio.
    @Column(nullable = false)
    private String location;

    // Il campo "maxSeats" rappresenta il numero massimo di posti disponibili per l'evento. È obbligatorio.
    @Column(name = "max_seats", nullable = false)
    private Integer maxSeats;

    // Il campo "createdBy" rappresenta l'utente che ha creato l'evento. È obbligatorio.
    // La relazione è ManyToOne perché un utente può creare molti eventi, ma ogni evento ha un solo creatore.
    // Usiamo FetchType.LAZY per evitare di caricare l'intero oggetto User quando carichiamo un evento,
    // e @JoinColumn per specificare il nome della colonna che contiene la chiave esterna.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
}
