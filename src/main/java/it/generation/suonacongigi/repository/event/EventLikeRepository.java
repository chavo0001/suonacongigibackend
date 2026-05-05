//LIKE Repository dedicato alle operazioni base sui like degli eventi.
package it.generation.suonacongigi.repository.event;

import it.generation.suonacongigi.model.EventLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventLikeRepository extends JpaRepository<EventLike, Long> {
    long countByEventId(Long eventId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    Optional<EventLike> findByEventIdAndUserId(Long eventId, Long userId);
}
