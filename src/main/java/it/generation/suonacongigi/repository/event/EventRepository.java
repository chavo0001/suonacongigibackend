package it.generation.suonacongigi.repository.event;

import it.generation.suonacongigi.model.Event; 
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Il repository EventRepository estende JpaRepository per fornire operazioni CRUD sull'entità Event.

public interface EventRepository extends JpaRepository<Event, Long> {
    // Il metodo findWithOrganizerById restituisce un Optional<Event> che contiene l'evento con il dato ID,
    // se esiste, altrimenti un Optional vuoto. 
    // L'annotazione @EntityGraph viene utilizzata per specificare che quando viene eseguita questa query,
    // deve essere caricata anche l'associazione "createdBy" (l'organizzatore dell'evento), 
    // evitando così il problema N+1 query quando si accede ai dati dell'organizzatore associ
    @EntityGraph(attributePaths = {"createdBy"})
    Optional<Event> findWithOrganizerById(Long id);

    // Il metodo findByEventDateAfterOrderByEventDateAsc restituisce una lista di eventi che si svolgono dopo una certa data,
    // ordinati in ordine crescente per data dell'evento.           
    // L'annotazione @EntityGraph viene utilizzata per specificare che quando viene eseguita questa query,
    // deve essere caricata anche l'associazione "createdBy" (l'organizzatore dell'evento), 
    // evitando così il problema N+1 query quando si accede ai dati dell'organizzatore associato a ciascun evento.
    @EntityGraph(attributePaths = {"createdBy"})
    List<Event> findByEventDateAfterOrderByEventDateAsc(LocalDateTime date);
}