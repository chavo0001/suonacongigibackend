package it.generation.suonacongigi.repository.forum;

import it.generation.suonacongigi.model.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository; 
import java.util.List;

// Il repository PostRepository estende JpaRepository per fornire operazioni CRUD sull'entità Post.

public interface PostRepository extends JpaRepository<Post, Long> {

    // Il metodo findByThreadIdOrderByCreatedAtAsc restituisce una lista di post per un dato thread,
    // ordinati in base alla data di creazione in ordine crescente.
    // L'annotazione @EntityGraph viene utilizzata per specificare che quando viene eseguita questa query,
    // deve essere caricata anche l'associazione "author" per ogni post,
    // evitando così il problema N+1 query quando si accede ai dati dell'autore associato a ciascun post.
    @EntityGraph(attributePaths = {"author"})
    List<Post> findByThreadIdOrderByCreatedAtAsc(Long threadId);

    // Il metodo countByThreadId restituisce il numero di post per un dato thread, identificato dal suo ID.
    long countByThreadId(Long threadId);
}