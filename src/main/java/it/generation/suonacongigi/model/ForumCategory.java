package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*; 

// L'entità ForumCategory rappresenta una categoria del forum, con un nome unico e una descrizione opzionale.

@Entity
@Table(name = "forum_categories")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class ForumCategory {
    // Il campo "id" è la chiave primaria dell'entità, generata automaticamente dal database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il campo "name" rappresenta il nome della categoria. È obbligatorio e deve essere unico.
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // Il campo "description" rappresenta la descrizione della categoria. È facoltativo. 
    // Può essere lungo, quindi usiamo TEXT.
    @Column(columnDefinition = "TEXT")
    private String description;
}

