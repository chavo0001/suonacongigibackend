package it.generation.suonacongigi.repository.event;

import it.generation.suonacongigi.model.EventLike;
import it.generation.suonacongigi.model.User;
import it.generation.suonacongigi.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface EventLikeRepository extends JpaRepository<EventLike, Long> {
    
    // Questo ci servirà per capire se l'utente ha GIÀ messo like (per evitare doppioni)
    Optional<EventLike> findByUserAndEvent(User user, Event event);
    
    // Questo ci servirà per contare i like di un evento
    long countByEvent(Event event);

    // SEZIONE: Recupera tutti i Like messi da un utente specifico tramite il suo ID
    List<EventLike> findByUserId(Long userId);
}
