package it.generation.suonacongigi.repository.event;
 
import it.generation.suonacongigi.model.EventRegistration;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

// Il repository EventRegistrationRepository estende JpaRepository per fornire operazioni CRUD sull'entità EventRegistration.

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    // Il metodo countByEventId restituisce il numero di registrazioni per un dato evento, identificato dal suo ID.
    long countByEventId(Long eventId);

    // Il metodo existsByEventIdAndUserId verifica se esiste una registrazione per un dato evento e un dato utente, 
    // identificati rispettivamente dai loro ID. Restituisce true se esiste almeno una registrazione, altrimenti false.
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    // Il metodo findAllByEventId restituisce una lista di tutte le registrazioni per un dato evento, identificato dal suo ID.
    // L'annotazione @EntityGraph viene utilizzata per specificare che quando viene eseguita questa query,
    // deve essere caricata anche l'associazione "user" per ogni registrazione,
    // evitando così il problema N+1 query quando si accede ai dati dell'utente associato a ciascuna registrazione.
    @EntityGraph(attributePaths = {"user"})
    List<EventRegistration> findAllByEventId(Long eventId);
}