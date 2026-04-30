package it.generation.suonacongigi.service;

import it.generation.suonacongigi.dto.genre.GenreRequest;
import it.generation.suonacongigi.dto.genre.GenreResponse;
import it.generation.suonacongigi.model.Genre;
import it.generation.suonacongigi.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public List<GenreResponse> findAll() {
        // Avvolgiamo il risultato finale in Objects.requireNonNull per soddisfare @NonNullApi
        return Objects.requireNonNull(genreRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public GenreResponse findById(Long id) {
        // La catena .map().orElseThrow() è sicura, ma per il compilatore l'uscita deve essere certificata come @NonNull
        GenreResponse response = genreRepository.findById(Objects.requireNonNull(id))
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Genere non trovato: " + id));
        return Objects.requireNonNull(response);
    }

    @Transactional
    public GenreResponse create(GenreRequest req) {
        // Certifichiamo req.getName() prima di passarlo al repository
        String name = Objects.requireNonNull(req.getName());

        // Verifichiamo l'unicità del nome prima di creare il genere
        if (genreRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalStateException("Genere già presente: " + name);
        }

        // Costruiamo l'entità da salvare
        Genre genreToSave = Genre.builder()
                .name(name)
                .build();

        // Certifichiamo il salvataggio e la conversione
        Genre saved = Objects.requireNonNull(genreRepository.save(Objects.requireNonNull(genreToSave)));

        // Restituiamo la risposta
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        // Certifichiamo l'oggetto restituito dall'helper
        Genre genre = Objects.requireNonNull(getOrThrow(id));
        genreRepository.delete(genre);
    }

    private Genre getOrThrow(Long id) {
        // Certifichiamo il ritorno di orElseThrow per @NonNullApi
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Genere non trovato: " + id));

        // La catena è sicura, ma certifichiamo il risultato
        return Objects.requireNonNull(genre);
    }

    private GenreResponse toResponse(Genre genre) {
        // Certifichiamo tutti i campi necessari per costruire la risposta
        return Objects.requireNonNull(GenreResponse.builder()
                .id(Objects.requireNonNull(genre.getId()))
                .name(Objects.requireNonNull(genre.getName()))
                .build());
    }

}