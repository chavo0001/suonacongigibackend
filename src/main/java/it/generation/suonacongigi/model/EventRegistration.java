package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// L'entità EventRegistration rappresenta la registrazione di un utente a un evento, 
// con una relazione ManyToOne sia verso Event che verso User.

@Entity
@Table(name = "event_registrations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"}))
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class EventRegistration {

    // Il campo "id" è la chiave primaria dell'entità, generata automaticamente dal database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il campo "event" rappresenta l'evento a cui l'utente si è registrato.
    // La relazione è ManyToOne perché un evento può avere molte registrazioni, ma ogni registrazione è per un solo evento.
    // Usiamo FetchType.LAZY per evitare di caricare l'intero oggetto Event quando carichiamo una registrazione
    // e @JoinColumn per specificare il nome della colonna
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Il campo "user" rappresenta l'utente che si è registrato all'evento.
    // La relazione è ManyToOne perché un utente può avere molte registrazioni, ma ogni registrazione è per un solo utente.
    // Usiamo FetchType.LAZY per evitare di caricare l'intero oggetto User quando carichiamo una registrazione
    // e @JoinColumn per specificare il nome della colonna che contiene la chiave esterna.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Il campo "registeredAt" rappresenta la data e ora in cui l'utente si è registrato all'evento.
    // Viene impostato automaticamente al momento della creazione della registrazione, 
    // grazie a @Builder.Default che assegna il valore di LocalDateTime.now() se non viene specificato un valore.
    @Column(name = "registered_at")
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();
}