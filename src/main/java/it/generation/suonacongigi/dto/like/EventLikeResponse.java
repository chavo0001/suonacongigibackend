//LIKE DTO semplice per restituire conteggio like e stato dell'utente corrente.
package it.generation.suonacongigi.dto.like;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLikeResponse {
    private Long eventId;
    private long likeCount;
    private boolean likedByCurrentUser;
}
