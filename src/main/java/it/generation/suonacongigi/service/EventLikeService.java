package it.generation.suonacongigi.service;

import it.generation.suonacongigi.model.*;
import it.generation.suonacongigi.repository.event.*;
import it.generation.suonacongigi.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.generation.suonacongigi.dto.event.EventResponse;
import java.util.List;
import java.util.stream.Collectors;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventLikeService {

    private final EventLikeRepository likeRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public String toggleLike(Long eventId, String username) {
        // 1. Recupero l'evento (usando lo stile del tuo EventService)
        Event event = eventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new NoSuchElementException("Evento non trovato: " + eventId));

        // 2. Recupero l'utente
        User user = userRepository.findByUsername(Objects.requireNonNull(username))
                .orElseThrow(() -> new NoSuchElementException("Utente non trovato: " + username));

        // 3. Logica Toggle: se esiste lo elimino, se non esiste lo creo
        Optional<EventLike> existingLike = likeRepository.findByUserAndEvent(user, event);

        if (existingLike.isPresent()) {
            likeRepository.delete(Objects.requireNonNull(existingLike.get()));
            return "Like rimosso con successo";
        } else {
            EventLike newLike = Objects.requireNonNull(EventLike.builder()
                    .user(user)
                    .event(event)
                    .build());
            
            likeRepository.save(newLike);
            return "Like aggiunto con successo";
        }
    }

    @Transactional(readOnly = true)
    public long getLikeCount(Long eventId) {
        Event event = eventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new NoSuchElementException("Evento non trovato: " + eventId));
        return likeRepository.countByEvent(event);
    }

    //SEZIONE LIKE
    @Transactional(readOnly = true)
    public List<EventResponse> getLikedEvents(String username) {
        // 1. Recupero l'utente
        User user = userRepository.findByUsername(Objects.requireNonNull(username))
                .orElseThrow(() -> new NoSuchElementException("Utente non trovato: " + username));

        // 2. Recupero tutti i like di questo utente
        List<EventLike> likes = likeRepository.findByUserId(user.getId());

        // 3. Trasformo la lista di Like in una lista di EventResponse
        // 3. Trasformo la lista di Like in una lista di EventResponse
        return Objects.requireNonNull(likes.stream()
                .map(like -> {
                    Event e = Objects.requireNonNull(like.getEvent());
                    return EventResponse.builder()
                            .id(e.getId())
                            .title(e.getTitle())
                            .eventDate(e.getEventDate())
                            .location(e.getLocation())
                            .description(e.getDescription())
                            .createdBy(e.getCreatedBy().getUsername())
                            .build();
                })
                .collect(Collectors.toList()), "La lista degli eventi con like non può essere nulla");
    }

    //LIKED
    @Transactional(readOnly = true)
    public List<Long> getLikedEventIds(String username) {
        // 1. Recupero l'utente
        User user = userRepository.findByUsername(Objects.requireNonNull(username))
                .orElseThrow(() -> new NoSuchElementException("Utente non trovato: " + username));

        // 2. Recupero i like e trasformo la lista di oggetti EventLike in una lista di Long (gli ID degli eventi)
        return Objects.requireNonNull(
                likeRepository.findByUserId(user.getId())
                    .stream()
                    .map(like -> like.getEvent().getId())
                    .collect(Collectors.toList()), 
                "La lista degli ID dei like non può essere nulla"
        );
    }

    
}
