package it.generation.suonacongigi.service;

import it.generation.suonacongigi.dto.instrument.InstrumentRequest;
import it.generation.suonacongigi.dto.instrument.InstrumentResponse;
import it.generation.suonacongigi.model.Instrument;
import it.generation.suonacongigi.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InstrumentService {

    private final InstrumentRepository instrumentRepository;

    @Transactional(readOnly = true)
    public List<InstrumentResponse> findAll() {
        return Objects.requireNonNull(instrumentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public InstrumentResponse findById(Long id) {
        InstrumentResponse response = instrumentRepository.findById(Objects.requireNonNull(id))
                .map(this::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Strumento non trovato con ID: " + id));
        
        return Objects.requireNonNull(response);
    }

    @Transactional
    public InstrumentResponse create(InstrumentRequest req) {
        // Certifichiamo il nome prima di qualsiasi operazione
        String name = Objects.requireNonNull(req.getName());
        
        // Verifichiamo l'unicità del nome prima di creare lo strumento
        if (instrumentRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalStateException("Strumento già presente: " + name);
        }

         // Costruiamo l'entità da salvare
        Instrument instrumentToSave = Instrument.builder()
                .name(name)
                .build();

        // Salviamo l'entità e restituiamo il DTO
        Instrument saved = Objects.requireNonNull(instrumentRepository.save(Objects.requireNonNull(instrumentToSave)));
        
        // Restituiamo la risposta
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        // Certifichiamo l'oggetto restituito dall'helper
        Instrument instrument = Objects.requireNonNull(getOrThrow(id));

        // Eliminiamo l'entità
        instrumentRepository.delete(instrument);
    }

    
    private Instrument getOrThrow(Long id) {
        // Certifichiamo il ritorno di orElseThrow per @NonNullApi
        Instrument instrument = instrumentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Strumento non trovato: " + id));

        // La catena è sicura, ma certifichiamo il risultato
        return Objects.requireNonNull(instrument);
    }

    private InstrumentResponse toResponse(Instrument instrument) {
        // Certifichiamo tutti i campi necessari per costruire la risposta
        return Objects.requireNonNull(InstrumentResponse.builder()
                .id(Objects.requireNonNull(instrument.getId()))
                .name(Objects.requireNonNull(instrument.getName()))
                .build());
    }
}