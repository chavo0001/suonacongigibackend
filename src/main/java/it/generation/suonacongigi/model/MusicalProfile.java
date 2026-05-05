package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*; 
import java.util.HashSet;
import java.util.Set;

// L'entità MusicalProfile rappresenta il profilo musicale di un utente, 
// con relazioni a generi, strumenti e artisti preferiti.

@Entity
@Table(name = "musical_profiles")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class MusicalProfile {
    // Il campo "id" è la chiave primaria dell'entità, generata automaticamente dal database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il campo "user" rappresenta l'utente a cui appartiene il profilo musicale. È obbligatorio.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Il campo "bio" rappresenta una breve biografia dell'utente. È facoltativo e può essere lungo, quindi usiamo TEXT.
    @Column(columnDefinition = "TEXT", length = 1000)
    private String bio;

    // Il campo "genres" rappresenta i generi musicali preferiti dall'utente. È facoltativo.
    // La relazione è ManyToMany, poiché un profilo può avere più generi e un genere può essere associato a più profili.
    // La tabella di join "profile_genres" collega i profili musicali ai generi, con le colonne "profile_id" e "genre_id".
    // Usiamo @Builder.Default per inizializzare la collezione con un HashSet vuoto, evitando NullPointerException.
    @ManyToMany
    @JoinTable(
        name               = "profile_genres",
        joinColumns        = @JoinColumn(name = "profile_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    // Il campo "instruments" rappresenta gli strumenti musicali suonati dall'utente. È facoltativo.
    // La relazione è ManyToMany, poiché un profilo può avere più strumenti e uno strumento può essere associato a più profili.
    // La tabella di join "profile_instruments" collega i profili musicali agli strumenti, con le colonne "profile_id" e "instrument_id".
    // Usiamo @Builder.Default per inizializzare la collezione con un HashSet vuoto, evitando NullPointerException.
    @ManyToMany
    @JoinTable(
        name               = "profile_instruments",
        joinColumns        = @JoinColumn(name = "profile_id"),
        inverseJoinColumns = @JoinColumn(name = "instrument_id")
    )
    @Builder.Default
    private Set<Instrument> instruments = new HashSet<>();

    // Il campo "favoriteArtists" rappresenta gli artisti preferiti dall'utente. È facoltativo.
    // La relazione è ManyToMany, poiché un profilo può avere più artisti preferiti e un artista può essere associato a più profili.
    // La tabella di join "profile_artists" collega i profili musicali agli artisti, con le colonne "profile_id" e "artist_id".
    // Usiamo @Builder.Default per inizializzare la collezione con un HashSet vuoto, evitando NullPointerException.
    @ManyToMany
    @JoinTable(
        name               = "profile_artists",
        joinColumns        = @JoinColumn(name = "profile_id"),
        inverseJoinColumns = @JoinColumn(name = "artist_id")
    )
    @Builder.Default
    private Set<Artist> favoriteArtists = new HashSet<>();
}