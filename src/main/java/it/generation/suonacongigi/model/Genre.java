package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*;

// L'entità Genre rappresenta un genere musicale, con un nome unico e un ID generato automaticamente.

@Entity
@Table(name = "genres")  
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class Genre {
    // Il campo "id" è la chiave primaria dell'entità, generata automaticamente dal database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il campo "name" rappresenta il nome del genere musicale. È obbligatorio e deve essere unico.
    @Column(nullable = false, unique = true, length = 50)
    private String name;
}