package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*;

// L'entità Artist rappresenta un artista musicale, con un nome unico e un ID generato automaticamente.

@Entity
@Table(name = "artists")  
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Artist {
    
    // Il campo "id" è la chiave primaria dell'entità, generata automaticamente dal database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il campo "name" rappresenta il nome dell'artista. È obbligatorio e deve essere unico.
    @Column(nullable = false, unique = true) 
    private String name;
}