package it.generation.suonacongigi.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.Objects;

// Questa classe contiene metodi di utilità per la gestione degli oggetti Pageable, 
// che rappresentano le richieste di paginazione nelle API REST.

public class PageableUtils {

    // Metodo statico per sanificare un oggetto Pageable.
    public static Pageable sanitize(Pageable pageable, int maxPageSize) {
        // NULL SAFETY: verifichiamo che il Pageable non sia null. 
        // Se lo è, lanciamo un'eccezione con un messaggio chiaro.
        Pageable clean = Objects.requireNonNull(pageable, "Pageable object cannot be null");

        // ANTI-DoS: limitiamo la dimensione massima della pagina per evitare richieste eccessive.
        // Denial of Service (DoS) è un attacco in cui un malintenzionato cerca di rendere 
        // un servizio indisponibile sovraccaricandolo con richieste eccessive.
        if (clean.getPageSize() > maxPageSize) {
            return PageRequest.of(
                clean.getPageNumber(), 
                maxPageSize, 
                clean.getSort()
            );
        }
        
        return clean;
    }
}