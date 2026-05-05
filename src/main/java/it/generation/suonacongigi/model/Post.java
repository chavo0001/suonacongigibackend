package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

// L'entità Post rappresenta un post all'interno di un thread del forum, 
// con un contenuto testuale, un autore e una data di creazione.  

@Entity
@Table(name = "posts")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Post {
    // Il campo "id" è la chiave primaria dell'entità, generata automaticamente dal database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il campo "content" rappresenta il contenuto del post. È obbligatorio e può essere lungo, quindi usiamo TEXT.
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Il campo "thread" rappresenta il thread a cui appartiene il post. È obbligatorio.
    // La relazione è ManyToOne perché un thread può avere molti post, ma ogni post appartiene a un solo thread.
    // Usiamo FetchType.LAZY per evitare di caricare l'intero oggetto ForumThread quando carichiamo un post
    // e @JoinColumn per specificare il nome della colonna
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private ForumThread thread;

    // Il campo "author" rappresenta l'autore del post. È obbligatorio.
    // La relazione è ManyToOne perché un utente può scrivere molti post, ma ogni post ha un solo autore.
    // Usiamo FetchType.LAZY per evitare di caricare l'intero oggetto User quando carichiamo un post, 
    // e @JoinColumn per specificare il nome della colonna che contiene la chiave esterna.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Il campo "createdAt" rappresenta la data e ora di creazione del post.
    // Viene impostato automaticamente al momento della creazione del post,
    // grazie a @Builder.Default che assegna il valore di LocalDateTime.now() se non viene specificato un valore.
    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}