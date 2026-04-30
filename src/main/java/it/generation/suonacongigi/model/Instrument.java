package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*;

// L'entità Instrument rappresenta uno strumento musicale, con un nome unico e un ID generato automaticamente.

@Entity
@Table(name = "instruments")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor @Builder
public class Instrument {
    // Il campo "id" è la chiave primaria dell'entità, generata automaticamente dal database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il campo "name" rappresenta il nome dello strumento musicale. È obbligatorio e deve essere unico.
    @Column(nullable = false, unique = true, length = 100)
    private String name;
}