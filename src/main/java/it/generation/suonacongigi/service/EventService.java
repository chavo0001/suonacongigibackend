package it.generation.suonacongigi.service;

import it.generation.suonacongigi.dto.event.*;
import it.generation.suonacongigi.model.*;
import it.generation.suonacongigi.repository.user.UserRepository;
import it.generation.suonacongigi.repository.event.*;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<EventResponse> findAll(@Nullable String currentUsername, @Nullable String search) {
        // Se viene fornito un termine di ricerca, filtriamo gli eventi in base a titolo, descrizione o location.
        LocalDateTime now = LocalDateTime.now();

        List<Event> events;

        if (search == null || search.isBlank()) {
            events = eventRepository.findByEventDateAfterOrderByEventDateAsc(now);
        } else {
            events = eventRepository.searchFutureEvents(now, search);// Il metodo searchFutureEvents è definito nel repository e utilizza
            //  una query personalizzata per filtrare gli eventi in base al termine di ricerca.
        }


        return Objects.requireNonNull(events.stream()
                .filter(e -> canUserSeeEvent(Objects.requireNonNull(e), currentUsername))
                .map(e -> toResponse(Objects.requireNonNull(e), currentUsername))
                .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public EventResponse findById(Long id, @Nullable String currentUsername) {
        Event event = Objects.requireNonNull(eventRepository.findWithOrganizerById(Objects.requireNonNull(id))
                .orElseThrow(() -> new NoSuchElementException("Evento non trovato: " + id)));

        return toResponse(event, currentUsername);
    }

    @Transactional(readOnly = true)
    public List<String> getRegistrants(Long eventId) {
        // MECCANICA: Recupero certificato degli iscritti mappati in stringhe (username).
        Event event = getOrThrow(Objects.requireNonNull(eventId));
        return Objects.requireNonNull(registrationRepository.findAllByEventId(event.getId()).stream()
                .map(reg -> Objects.requireNonNull(reg.getUser().getUsername()))
                .collect(Collectors.toList()));
    }

    @Transactional
    public EventResponse create(EventRequest req, String username) {
        User creator = userRepository.findByUsername(Objects.requireNonNull(username))
                .orElseThrow(() -> new NoSuchElementException("Utente non trovato"));

        Event eventToSave = Event.builder()
                .title(Objects.requireNonNull(req.getTitle()))
                .description(Objects.requireNonNull(req.getDescription()))
                .eventDate(Objects.requireNonNull(req.getEventDate()))
                .location(Objects.requireNonNull(req.getLocation()))
                .maxSeats(Objects.requireNonNull(req.getMaxSeats()))
                .createdBy(creator)
                .status(Event.EventStatus.PENDING)
                .build();

        // Certifichiamo il salvataggio e la conversione
        Event saved = Objects.requireNonNull(eventRepository.save(Objects.requireNonNull(eventToSave)));

        return toResponse(saved, username);
    }

    @Transactional
    public EventResponse update(Long id, EventRequest req, String currentUsername) {
        Assert.notNull(id, "ID evento obbligatorio");
        Assert.notNull(req, "Dati aggiornamento nulli");

        // 1. Recupero l'evento esistente (Managed Entity)
        Event event = getOrThrow(id);

        // 2. Validazione Logica: non posso abbassare i posti totali sotto il numero di già iscritti
        long booked = registrationRepository.countByEventId(id);
        if (req.getMaxSeats() < booked) {
            throw new IllegalStateException("Non puoi impostare maxSeats (" + req.getMaxSeats() + 
                ") inferiore al numero di iscritti attuali (" + booked + ")");
        }

        // 3. Aggiornamento dei campi tramite Setter (per Dirty Checking)
        event.setTitle(Objects.requireNonNull(req.getTitle()));
        event.setDescription(req.getDescription());
        event.setEventDate(Objects.requireNonNull(req.getEventDate()));
        event.setLocation(Objects.requireNonNull(req.getLocation()));
        event.setMaxSeats(Objects.requireNonNull(req.getMaxSeats()));

        // 4. Salvataggio e ritorno della risposta mappata
        // Nota: save() su un oggetto managed con ID esistente scatena l'update.
        Event updated = Objects.requireNonNull(eventRepository.save(event));

        return toResponse(updated, currentUsername);
    }
    
    @Transactional
    public void register(Long eventId, String username) {
        Event event = getOrThrow(Objects.requireNonNull(eventId));
        User user = userRepository.findByUsername(Objects.requireNonNull(username))
                .orElseThrow(() -> new NoSuchElementException("Utente non trovato"));

        if (registrationRepository.existsByEventIdAndUserId(event.getId(), user.getId())) {
            throw new IllegalStateException("Già iscritto a questo evento");
        }

        if (registrationRepository.countByEventId(event.getId()) >= event.getMaxSeats()) {
            throw new IllegalStateException("Spiacenti, posti esauriti");
        }

        EventRegistration reg = EventRegistration.builder().event(event).user(user).build();

        registrationRepository.save(Objects.requireNonNull(reg));
    }

    @Transactional
    public void unregister(Long eventId, String username) {
        User user = userRepository.findByUsername(Objects.requireNonNull(username))
                .orElseThrow(() -> new NoSuchElementException("Utente non trovato"));
        
        EventRegistration reg = registrationRepository.findByEventIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new NoSuchElementException("Iscrizione non trovata"));
        
        registrationRepository.delete(Objects.requireNonNull(reg));
    }

    @Transactional
    public void delete(Long id) {
        eventRepository.delete(getOrThrow(Objects.requireNonNull(id)));
    }

    @Transactional
    public EventResponse updateStatus(Long id, String statusStr, String adminUsername) {
        Event event = getOrThrow(id);
        
        Event.EventStatus newStatus;
        try {
            newStatus = Event.EventStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status non valido: " + statusStr);
        }
        
        event.setStatus(newStatus);
        Event updated = eventRepository.save(event);
        
        return toResponse(updated, adminUsername);
    }

    private boolean canUserSeeEvent(Event event, @Nullable String username) {
        // Gli admin vedono tutto
        if (username != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && user.getRole() == User.Role.ADMIN) {
                return true;
            }
        }
        
        // Creatori vedono i propri eventi anche se non approvati
        if (username != null && event.getCreatedBy().getUsername().equals(username)) {
            return true;
        }
        
        // Gli altri vedono solo gli eventi approvati
        return event.getStatus() == Event.EventStatus.APPROVED;
    }

    private EventResponse toResponse(Event event, @Nullable String currentUsername) {
        Long id = Objects.requireNonNull(event.getId());
        long booked = registrationRepository.countByEventId(id);
        int total = Objects.requireNonNull(event.getMaxSeats());

        boolean isRegistered = currentUsername != null && userRepository.findByUsername(currentUsername)
                .map(u -> registrationRepository.existsByEventIdAndUserId(id, u.getId()))
                .orElse(false);

        return Objects.requireNonNull(EventResponse.builder()
                .id(id)
                .title(Objects.requireNonNull(event.getTitle()))
                .description(event.getDescription())
                .eventDate(Objects.requireNonNull(event.getEventDate()))
                .location(Objects.requireNonNull(event.getLocation()))
                .maxSeats(total)
                .seatsBooked(booked)
                .seatsAvailable((long) total - booked)
                .createdBy(Objects.requireNonNull(event.getCreatedBy().getUsername()))
                .registeredByCurrentUser(isRegistered)
                .status(event.getStatus().name())
                .build());
    }

    @Transactional(readOnly = true)
    public List<EventResponse> findPendingEvents(String adminUsername) {
        return eventRepository
            .findByStatusOrderByCreatedAtDesc(Event.EventStatus.PENDING)
            .stream()
            .map(e -> toResponse(e, adminUsername))
            .collect(Collectors.toList());
    }

    private Event getOrThrow(Long id) {
        return Objects.requireNonNull(eventRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Evento non trovato: " + id)));
    }
}