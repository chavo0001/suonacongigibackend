package it.generation.suonacongigi.service;

import it.generation.suonacongigi.dto.artist.ArtistRequest;
import it.generation.suonacongigi.dto.artist.ArtistResponse;
import it.generation.suonacongigi.model.Artist;
import it.generation.suonacongigi.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; 
import java.util.NoSuchElementException;
import java.util.Objects; 

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    @Transactional(readOnly = true)
    public Page<ArtistResponse> findAll(Pageable pageable) {
        // La trasformazione avviene pigramente all'interno della struttura Page
        return artistRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ArtistResponse findById(Long id) {
        // La catena .map().orElseThrow() è sicura, ma per il compilatore l'uscita deve essere certificata come @NonNull
        ArtistResponse response = artistRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Artista non trovato con ID: " + id));
        
        return Objects.requireNonNull(response);
    }

    @Transactional
    public ArtistResponse create(ArtistRequest req) {
        // Certifichiamo req.getName() prima di passarlo al repository
        String name = Objects.requireNonNull(req.getName());

        // Verifichiamo l'unicità del nome prima di creare l'artista
        if (artistRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Artista già presente: " + name);
        }

        // Costruiamo l'entità da salvare
        Artist artistToSave = Artist.builder()
                .name(name)
                .build();

        // Certifichiamo il salvataggio e la conversione
        Artist saved = Objects.requireNonNull(artistRepository.save(Objects.requireNonNull(artistToSave)));
        
        // Restituiamo la risposta
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        // Certifichiamo l'oggetto restituito dall'helper
        Artist artist = Objects.requireNonNull(getOrThrow(id));
        artistRepository.delete(artist);
    }

    private ArtistResponse toResponse(Artist artist) {
        // Certifichiamo ogni campo estratto per evitare i warning "unchecked"
        return Objects.requireNonNull(ArtistResponse.builder()
                .id(Objects.requireNonNull(artist.getId()))
                .name(Objects.requireNonNull(artist.getName()))
                .build());
    }

    private Artist getOrThrow(Long id) {
        // Certifichiamo il ritorno di orElseThrow per @NonNullApi
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Artista non trovato: " + id));

        // La catena è sicura, ma certifichiamo il risultato
        return Objects.requireNonNull(artist);
    }
}