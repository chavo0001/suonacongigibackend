package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// L'entità ForumThread rappresenta un thread del forum, con un titolo, una categoria, un autore e una data di creazione.

@Entity
@Table(name = "forum_threads")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class ForumThread {
    // Il campo "id" è la chiave primaria dell'entità, generata automaticamente dal database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il campo "title" rappresenta il titolo del thread. È obbligatorio.
    @Column(nullable = false, length = 200)
    private String title;

    // Il campo "category" rappresenta la categoria del thread. È obbligatorio.
    // La relazione è ManyToOne perché una categoria può avere molti thread, ma ogni thread appartiene a una sola categoria.
    // Usiamo FetchType.LAZY per evitare di caricare l'intero oggetto ForumCategory 
    // quando carichiamo un thread, e @JoinColumn per specificare il nome della colonna
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ForumCategory category;

    // Il campo "author" rappresenta l'autore del thread. È obbligatorio.
    // La relazione è ManyToOne perché un utente può creare molti thread, ma ogni thread ha un solo autore.
    // Usiamo FetchType.LAZY per evitare di caricare l'intero oggetto User quando carichiamo un thread,
    // e @JoinColumn per specificare il nome della colonna che contiene la chiave esterna.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Il campo "createdAt" rappresenta la data e ora di creazione del thread.
    // Viene impostato automaticamente al momento della creazione del thread,
    // grazie a @Builder.Default che assegna il valore di LocalDateTime.now() se non viene specificato un valore.
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

