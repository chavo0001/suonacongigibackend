package it.generation.suonacongigi.repository.forum;

import it.generation.suonacongigi.model.ForumCategory;
import org.springframework.data.jpa.repository.JpaRepository; 

// Il repository ForumCategoryRepository estende JpaRepository per fornire operazioni CRUD sull'entità ForumCategory.

public interface ForumCategoryRepository extends JpaRepository<ForumCategory, Long> {
    // non ci sono metodi personalizzati specifici per la categoria del forum 
}