package it.generation.suonacongigi.service;

import it.generation.suonacongigi.dto.artist.ArtistResponse;
import it.generation.suonacongigi.dto.genre.GenreResponse;
import it.generation.suonacongigi.dto.instrument.InstrumentResponse;
import it.generation.suonacongigi.dto.user.MusicalProfileRequest;
import it.generation.suonacongigi.dto.user.MusicalProfileResponse; 
import it.generation.suonacongigi.dto.user.UserResponse;
import it.generation.suonacongigi.model.*;
import it.generation.suonacongigi.repository.*;
import it.generation.suonacongigi.repository.user.MusicalProfileRepository;
import it.generation.suonacongigi.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MusicalProfileRepository profileRepository;
    private final GenreRepository genreRepository;
    private final InstrumentRepository instrumentRepository;
    private final ArtistRepository artistRepository;

    @Transactional(readOnly = true)
    public UserResponse getMyProfile(String username) {
        return toResponse(getOrThrow(username));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return Objects.requireNonNull(userRepository.findAllByOrderByUsernameAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @Transactional
    public UserResponse updateMusicalProfile(String username, MusicalProfileRequest req) {
        Assert.hasText(username, "Username obbligatorio");
        Assert.notNull(req, "Richiesta nulla");

        User user = getOrThrow(username);
        
        // Pattern: Find-or-Create
        MusicalProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    MusicalProfile newProfile = MusicalProfile.builder().user(user).build();
                    user.setMusicalProfile(newProfile);
                    return newProfile;
                });

        // Aggiornamento Bio (Managed dal MusicalProfile)
        profile.setBio(req.getBio());

        // Aggiornamento Collezioni Many-to-Many
        updateCollections(profile, req);
        
        profileRepository.save(profile);
        
        return toResponse(user);
    }

    public void updateCollections(MusicalProfile profile, MusicalProfileRequest req) {
        if (req.getGenreIds() != null) {
            syncCollection(Objects.requireNonNull(req.getGenreIds()), Objects.requireNonNull(genreRepository), Objects.requireNonNull(profile.getGenres()));
        }
        if (req.getInstrumentIds() != null) {
            syncCollection(Objects.requireNonNull(req.getInstrumentIds()), Objects.requireNonNull(instrumentRepository), Objects.requireNonNull(profile.getInstruments()));
        }
        if (req.getArtistIds() != null) {
            syncCollection(Objects.requireNonNull(req.getArtistIds()), Objects.requireNonNull(artistRepository), Objects.requireNonNull(profile.getFavoriteArtists()));
        }
    }

    private <T, ID> void syncCollection(Set<ID> ids, JpaRepository<T, ID> repo, Set<T> targetSet) {
        targetSet.clear();
        if (ids != null && !ids.isEmpty()) {
            List<T> entities = repo.findAllById(ids);
            targetSet.addAll(entities);
        }
    }

    private UserResponse toResponse(User user) {
        MusicalProfileResponse mpr = profileRepository.findByUserId(user.getId())
                .map(this::toMusicalProfileResponse)
                .orElseThrow(() -> new IllegalStateException("Profilo mancante per: " + user.getUsername()));

        return Objects.requireNonNull(UserResponse.builder()
                .id(Objects.requireNonNull(user.getId()))
                .username(Objects.requireNonNull(user.getUsername()))
                .email(Objects.requireNonNull(user.getEmail()))
                .role(Objects.requireNonNull(user.getRole().name()))
                .musicalProfile(mpr)
                .build());
    }

    private MusicalProfileResponse toMusicalProfileResponse(MusicalProfile p) {
        return Objects.requireNonNull(MusicalProfileResponse.builder()
                .bio(p.getBio()) // Mapping della Bio nella risposta
                .genres(p.getGenres().stream().map(this::toGenreResponse).collect(Collectors.toSet()))
                .instruments(p.getInstruments().stream().map(this::toInstrumentResponse).collect(Collectors.toSet()))
                .favoriteArtists(p.getFavoriteArtists().stream().map(this::toArtistResponse).collect(Collectors.toSet()))
                .build());
    }

    // Metodi helper di mapping...
    private GenreResponse toGenreResponse(Genre g) {
        return Objects.requireNonNull(GenreResponse.builder().id(g.getId()).name(g.getName()).build());
    }

    private InstrumentResponse toInstrumentResponse(Instrument i) {
        return Objects.requireNonNull(InstrumentResponse.builder().id(i.getId()).name(i.getName()).build());
    }

    private ArtistResponse toArtistResponse(Artist a) {
        return Objects.requireNonNull(ArtistResponse.builder().id(a.getId()).name(a.getName()).build());
    }

    private User getOrThrow(String username) {
        return Objects.requireNonNull(userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Utente non trovato: " + username)));
    }
}