package it.generation.suonacongigi.repository.forum;

import it.generation.suonacongigi.model.ForumThread;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository; 
import java.util.List;
import java.util.Optional;

// Il repository ForumThreadRepository estende JpaRepository per fornire operazioni CRUD sull'entità ForumThread.

public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {

    // Il metodo findByCategoryIdOrderByCreatedAtDesc restituisce una lista di thread di un dato
    // categoria, ordinati in base alla data di creazione in ordine decrescente.
    // L'annotazione @EntityGraph viene utilizzata per specificare che quando viene eseguita questa query,
    // devono essere caricate anche le associazioni "author" e "category" per ogni thread,
    // evitando così il problema N+1 query quando si accede ai dati dell'autore e della categoria associati a ciascun thread.
    @EntityGraph(attributePaths = {"author", "category"})
    List<ForumThread> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);

    // Il metodo findWithGraphById restituisce un Optional<ForumThread> che contiene il thread con il dato ID,
    // se esiste, altrimenti un Optional vuoto.
    // L'annotazione @EntityGraph viene utilizzata per specificare che quando viene eseguita questa query,
    // devono essere caricate anche le associazioni "author" e "category" per il thread,
    // evitando così il problema N+1 query quando si accede ai dati dell'autore e della categoria associati al thread.
    @EntityGraph(attributePaths = {"author", "category"})
    Optional<ForumThread> findWithGraphById(Long id);

    // Il metodo countByCategoryId restituisce il numero di thread per una data categoria, identificata dal suo ID.
    long countByCategoryId(Long categoryId);
}