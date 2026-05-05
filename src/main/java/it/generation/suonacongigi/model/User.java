package it.generation.suonacongigi.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

//  L'entità User rappresenta un utente del sistema, con un username, email, password, ruolo e altre informazioni personali.

@Entity
@Table(name = "users")
@Getter 
@Setter 
@Builder
@NoArgsConstructor 
@AllArgsConstructor
public class User implements UserDetails {
    // Il campo "id" è la chiave primaria dell'entità, generata automaticamente dal database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Il campo "username" rappresenta il nome utente dell'utente. 
    // È obbligatorio, deve essere unico e ha una lunghezza massima di 50 caratteri.
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Il campo "email" rappresenta l'indirizzo email dell'utente. 
    // È obbligatorio, deve essere unico e ha una lunghezza massima di 100 caratteri.   
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Il campo "password" rappresenta la password dell'utente. 
    // È obbligatorio e ha una lunghezza massima di 255 caratteri (per consentire l'uso di hash complessi).
    @Column(nullable = false, length = 255)
    private String password;

    // Il campo "role" rappresenta il ruolo dell'utente (USER o ADMIN).
    // È obbligatorio e viene memorizzato come stringa nel database grazie a @Enumerated(EnumType.STRING).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // Il campo "musicalProfile" rappresenta il profilo musicale associato all'utente.
    // La relazione è OneToOne, poiché ogni utente ha un solo profilo musicale 
    // e ogni profilo musicale è associato a un solo utente.
    // Usiamo mappedBy per indicare che la relazione è gestita dal campo "user" nella classe MusicalProfile,
    // cascade = CascadeType.ALL per propagare tutte le operazioni (persist, merge, remove, refresh) 
    // dall'utente al profilo musicale,
    // e fetch = FetchType.LAZY per caricare il profilo musicale solo quando viene effettivamente richiesto, 
    // evitando di caricarlo automaticamente ogni volta che carichiamo un utente.
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MusicalProfile musicalProfile;

    // Il campo "createdAt" rappresenta la data e ora di creazione dell'utente.
    // Viene impostato automaticamente al momento della creazione dell'utente,
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // L'enum Role rappresenta i possibili ruoli di un utente: USER e ADMIN.
    public enum Role {
        USER, ADMIN
    }

   // Implementazione dei metodi dell'interfaccia UserDetails per l'integrazione con Spring Security.
   // Il metodo getAuthorities() restituisce una collezione di oggetti GrantedAuthority 
   // che rappresentano i ruoli dell'utente.
   // In questo caso, mappiamo il ruolo interno (USER o ADMIN) nel formato standard "ROLE_USER" o "ROLE_ADMIN" 
   // richiesto dai filtri AOP di Spring Security (Aspect-Oriented Programming) per la gestione delle autorizzazioni.
   // ad esempio AOP può essere usato per annotare i metodi con @PreAuthorize("hasRole('ADMIN')") 
   // per consentire l'accesso solo agli utenti con ruolo ADMIN.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mappiamo il ruolo interno nel formato standard (GrantedAuthority) richiesto dai filtri AOP.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    // I metodi isAccountNonExpired(), isAccountNonLocked(), isCredentialsNonExpired() e isEnabled()
    // restituiscono true per indicare che l'account è attivo e non scaduto. 
    // In un'applicazione reale, potremmo implementare logiche più complesse per gestire lo stato dell'account.
    @Override public boolean isAccountNonExpired()      { return true; }
    @Override public boolean isAccountNonLocked()       { return true; }
    @Override public boolean isCredentialsNonExpired()  { return true; }
    @Override public boolean isEnabled()                { return true; }
}