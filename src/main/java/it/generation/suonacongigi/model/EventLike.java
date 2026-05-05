package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Nuova entità per gestire i "Like" agli eventi.
 * Questa classe funge da tabella di collegamento tra User ed Event
 * senza modificare le classi originali.
 */
@Entity
@Table(name = "event_likes")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class EventLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // L'utente che mette il like
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // L'evento che riceve il like
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Data in cui è stato messo il like (opzionale, ma utile)
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
}
