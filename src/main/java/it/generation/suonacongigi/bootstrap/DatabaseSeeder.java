/*
 * All'avvio dell'applicazione, questo componente controlla se il database
 * è già popolato. Se non lo è (o se seed-on-start=true), crea tutte le
 * tabelle e le riempie con dati di esempio.
 * Come funziona il flag seed-on-start?
 * - TRUE  = cancella e ricrea tutto il database da zero (solo in sviluppo).
 * - FALSE = se il DB ha già dati, non fa nulla e si avvia normalmente.
 * ============================================================
 */
package it.generation.suonacongigi.bootstrap;

import it.generation.suonacongigi.model.*;
import it.generation.suonacongigi.repository.*;
import it.generation.suonacongigi.repository.event.EventRegistrationRepository;
import it.generation.suonacongigi.repository.event.EventRepository;
import it.generation.suonacongigi.repository.forum.ForumCategoryRepository;
import it.generation.suonacongigi.repository.forum.ForumThreadRepository;
import it.generation.suonacongigi.repository.forum.PostRepository;
import it.generation.suonacongigi.repository.user.MusicalProfileRepository;
import it.generation.suonacongigi.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DatabaseSeeder implements CommandLineRunner {

        private final UserRepository userRepository;
        private final GenreRepository genreRepository;
        private final InstrumentRepository instrumentRepository;
        private final ArtistRepository artistRepository;
        private final MusicalProfileRepository musicalProfileRepository;
        private final EventRepository eventRepository;
        private final EventRegistrationRepository eventRegistrationRepository;
        private final ForumCategoryRepository forumCategoryRepository;
        private final ForumThreadRepository forumThreadRepository;
        private final PostRepository postRepository;
        private final ParolaBanditaRepository parolaBanditaRepository;
        private final PasswordEncoder passwordEncoder;
        private final DataSource dataSource;

        @Value("${app.db.seed-on-start:false}")
        private boolean shouldSeed;

        @Override
        public void run(String... args) {
                if (shouldSeed) {
                        System.out.println("***************************************************************");
                        System.out.println("[SUONA CON GIGI-SEED]: Inizio il seeding del database.");
                        System.out.println("***************************************************************");
                        rebuildDatabaseSchema();
                } else if (userRepository.count() > 0) {
                        System.out.println("************************************************************");
                        System.out.println("[SUONA CON GIGI-BOOTSTRAP]: Dati presenti, salto il seeding.");
                        System.out.println("************************************************************");
                }

                executeSeeding();
        }

        /*
         * rebuildDatabaseSchema(): cancella e ricrea tutto il database da zero.
         * Viene eseguito solo se seed-on-start=true.
         * Usa JDBC diretto (non JPA) per avere controllo totale sulle istruzioni DDL.
         */
        private void rebuildDatabaseSchema() {
                try (Connection conn = dataSource.getConnection();
                                Statement stmt = conn.createStatement()) {

                        String dbName = conn.getCatalog();

                        stmt.execute("DROP DATABASE IF EXISTS " + dbName);
                        stmt.execute("CREATE DATABASE " + dbName);
                        stmt.execute("USE " + dbName);

                        System.out.println("***************************************************************");
                        System.out.println("Creazione della Base Dati Iniziale ");
                        System.out.println("***************************************************************");

                        /*
                         * Tabella users: contiene tutti gli utenti registrati.
                         * MODIFICA: aggiunta la colonna "censura_attiva" (DEFAULT TRUE = filtro
                         * attivo).
                         * MODIFICA: aggiunta la colonna "status" (DEFAULT FALSE = email non
                         * verificata).
                         */
                        stmt.execute("CREATE TABLE users (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "username VARCHAR(50) NOT NULL UNIQUE, " +
                                        "email VARCHAR(100) NOT NULL UNIQUE, " +
                                        "password VARCHAR(255) NOT NULL, " +
                                        "role VARCHAR(20) NOT NULL, " +
                                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                        "status BOOLEAN NOT NULL DEFAULT FALSE, " +
                                        "censura_attiva BOOLEAN NOT NULL DEFAULT TRUE) ENGINE=InnoDB");

                        /* Tabella generi musicali */
                        stmt.execute("CREATE TABLE genres (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "name VARCHAR(100) NOT NULL UNIQUE) ENGINE=InnoDB");

                        /* Tabella strumenti musicali */
                        stmt.execute("CREATE TABLE instruments (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "name VARCHAR(100) NOT NULL UNIQUE) ENGINE=InnoDB");

                        /* Tabella artisti */
                        stmt.execute("CREATE TABLE artists (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "name VARCHAR(100) NOT NULL UNIQUE) ENGINE=InnoDB");

                        /* Tabella token di verifica email */
                        stmt.execute("CREATE TABLE verification_Token (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "user_id BIGINT NOT NULL, " +
                                        "token VARCHAR(250), " +
                                        "expires_at TIMESTAMP) ENGINE=InnoDB");

                        /* Tabella profili musicali (relazione 1:1 con users) */
                        stmt.execute("CREATE TABLE musical_profiles (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "user_id BIGINT NOT NULL UNIQUE, " +
                                        "bio TEXT, " +
                                        "CONSTRAINT fk_profile_user " +
                                        "FOREIGN KEY (user_id) " +
                                        "REFERENCES users(id) ON DELETE CASCADE) ENGINE=InnoDB");

                        /* Tabella di join profilo-genere (N:M) */
                        stmt.execute("CREATE TABLE profile_genres (" +
                                        "profile_id BIGINT NOT NULL, " +
                                        "genre_id BIGINT NOT NULL, " +
                                        "PRIMARY KEY (profile_id, genre_id), " +
                                        "CONSTRAINT fk_pg_profile " +
                                        "FOREIGN KEY (profile_id) " +
                                        "REFERENCES musical_profiles(id) ON DELETE CASCADE, " +
                                        "CONSTRAINT fk_pg_genre " +
                                        "FOREIGN KEY (genre_id) " +
                                        "REFERENCES genres(id) ON DELETE CASCADE) ENGINE=InnoDB");

                        /* Tabella di join profilo-strumento (N:M) */
                        stmt.execute("CREATE TABLE profile_instruments (" +
                                        "profile_id BIGINT NOT NULL, " +
                                        "instrument_id BIGINT NOT NULL, " +
                                        "PRIMARY KEY (profile_id, instrument_id), " +
                                        "CONSTRAINT fk_pi_profile " +
                                        "FOREIGN KEY (profile_id) " +
                                        "REFERENCES musical_profiles(id) ON DELETE CASCADE, " +
                                        "CONSTRAINT fk_pi_instrument " +
                                        "FOREIGN KEY (instrument_id) " +
                                        "REFERENCES instruments(id) ON DELETE CASCADE) ENGINE=InnoDB");

                        /* Tabella di join profilo-artista (N:M) */
                        stmt.execute("CREATE TABLE profile_artists (" +
                                        "profile_id BIGINT NOT NULL, " +
                                        "artist_id BIGINT NOT NULL, " +
                                        "PRIMARY KEY (profile_id, artist_id), " +
                                        "CONSTRAINT fk_pa_profile " +
                                        "FOREIGN KEY (profile_id) " +
                                        "REFERENCES musical_profiles(id) ON DELETE CASCADE, " +
                                        "CONSTRAINT fk_pa_artist " +
                                        "FOREIGN KEY (artist_id) " +
                                        "REFERENCES artists(id) ON DELETE CASCADE) ENGINE=InnoDB");

                        /* Tabella eventi */
                        stmt.execute("CREATE TABLE events (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "title VARCHAR(200) NOT NULL, " +
                                        "description TEXT, " +
                                        "event_date DATETIME NOT NULL, " +
                                        "location VARCHAR(255) NOT NULL, " +
                                        "max_seats INT NOT NULL, " +
                                        "created_by_id BIGINT NOT NULL, " +
                                        "CONSTRAINT fk_event_creator " +
                                        "FOREIGN KEY (created_by_id) " +
                                        "REFERENCES users(id)) ENGINE=InnoDB");

                        /* Tabella iscrizioni agli eventi */
                        stmt.execute("CREATE TABLE event_registrations (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "event_id BIGINT NOT NULL, " +
                                        "user_id BIGINT NOT NULL, " +
                                        "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                        "UNIQUE(event_id, user_id), " +
                                        "CONSTRAINT fk_reg_event " +
                                        "FOREIGN KEY (event_id) " +
                                        "REFERENCES events(id) ON DELETE CASCADE, " +
                                        "CONSTRAINT fk_reg_user " +
                                        "FOREIGN KEY (user_id) " +
                                        "REFERENCES users(id) ON DELETE CASCADE) ENGINE=InnoDB");

                        /* Tabella categorie del forum */
                        stmt.execute("CREATE TABLE forum_categories (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "name VARCHAR(100) NOT NULL UNIQUE, description TEXT) ENGINE=InnoDB");

                        /* Tabella thread del forum */
                        stmt.execute("CREATE TABLE forum_threads (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "title VARCHAR(200) NOT NULL, " +
                                        "category_id BIGINT NOT NULL, " +
                                        "author_id BIGINT NOT NULL, " +
                                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                        "CONSTRAINT fk_thread_category " +
                                        "FOREIGN KEY (category_id) " +
                                        "REFERENCES forum_categories(id) ON DELETE CASCADE, " +
                                        "CONSTRAINT fk_thread_author " +
                                        "FOREIGN KEY (author_id) " +
                                        "REFERENCES users(id)) ENGINE=InnoDB");

                        /* Tabella post del forum */
                        stmt.execute("CREATE TABLE posts (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "content TEXT NOT NULL, " +
                                        "thread_id BIGINT NOT NULL, " +
                                        "author_id BIGINT NOT NULL, " +
                                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                                        "CONSTRAINT fk_post_thread " +
                                        "FOREIGN KEY (thread_id) " +
                                        "REFERENCES forum_threads(id) ON DELETE CASCADE, " +
                                        "CONSTRAINT fk_post_author " +
                                        "FOREIGN KEY (author_id) " +
                                        "REFERENCES users(id)) ENGINE=InnoDB");

                        /*
                         * NUOVA TABELLA PAROLE BANDITE (ALLINEATA AL MODEL)
                         * Usiamo 'word' per farla combaciare con l'Entity Java.
                         * Aggiunta la colonna 'attiva' per il filtro del Repository.
                         */
                        stmt.execute("CREATE TABLE parole_bandite (" +
                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                        "word VARCHAR(100) NOT NULL UNIQUE, " +
                                        "category VARCHAR(50), " +
                                        "attiva BOOLEAN NOT NULL DEFAULT TRUE, " +
                                        "creata_il TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                                        "INDEX idx_word (word), " +
                                        "INDEX idx_attiva (attiva)) " +
                                        "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");

                        System.out.println("*******************************************************************************");
                        System.out.println("[SUONA CON GIGI-BOOTSTRAP]: Schema ricostruito. Seeding eseguito correttamente.");
                        System.out.println("*******************************************************************************");

                } catch (Exception e) {
                        throw new RuntimeException("Errore critico DDL: " + e.getMessage(), e);
                }
        }

        @Transactional
        public void executeSeeding() {
                System.out.println("***************************************************************");
                System.out.println("[SUONA CON GIGI-BOOTSTRAP] Avvio popolamento community...");
                System.out.println("***************************************************************");

                try {
                        // 1. GENERI MUSICALI (10)
                        String[] genreNames = {
                                        "Rock", "Metal", "Jazz", "Blues", "Pop",
                                        "Electronic", "Classical", "Funk", "Reggae", "Soul"
                        };
                        List<Genre> genres = new java.util.ArrayList<>();
                        for (String gn : genreNames)
                                genres.add(createGenre(gn));

                        // 2. STRUMENTI MUSICALI (8)
                        String[] instNames = {
                                        "Chitarra Elettrica", "Basso Elettrico", "Batteria",
                                        "Pianoforte", "Sassofono", "Violino", "Tromba", "Sintetizzatore"
                        };
                        List<Instrument> instruments = new java.util.ArrayList<>();
                        for (String in : instNames)
                                instruments.add(createInstrument(in));

                        // 3. ARTISTI (50)
                        String[] artistPool = {
                                        "Jimi Hendrix", "Led Zeppelin", "Pink Floyd", "Metallica", "Iron Maiden",
                                        "Miles Davis", "John Coltrane", "Bill Evans", "B.B. King", "Eric Clapton",
                                        "The Beatles", "Queen", "David Bowie", "Daft Punk", "Kraftwerk",
                                        "Mozart", "Beethoven", "Bach", "James Brown", "Stevie Wonder",
                                        "Bob Marley", "Peter Tosh", "Aretha Franklin", "Ray Charles", "Prince",
                                        "Nirvana", "Pearl Jam", "Deep Purple", "Black Sabbath", "AC/DC",
                                        "Chet Baker", "Charlie Parker", "Thelonious Monk", "Muddy Waters", "Buddy Guy",
                                        "Radiohead", "Arctic Monkeys", "The Strokes", "Depeche Mode", "New Order",
                                        "Aphex Twin", "Chemical Brothers", "Vivaldi", "Chopin", "Earth Wind & Fire",
                                        "Chaka Khan", "Marvin Gaye", "Al Green", "Red Hot Chili Peppers", "Foo Fighters"
                        };
                        List<Artist> artists = new java.util.ArrayList<>();
                        for (String an : artistPool)
                                artists.add(createArtist(an));

                        // 4. UTENTI (1 Admin + 3 User)
                        User admin = createUser("admin", "admin@suonacongigi.it", User.Role.ADMIN);
                        User rocker = createUser("mario_gibson", "mario@email.it", User.Role.USER);
                        User jazzer = createUser("elena_sax", "elena@email.it", User.Role.USER);
                        User electro = createUser("luca_synth", "luca@email.it", User.Role.USER);

                        // 5. PROFILI MUSICALI
                        createMusicalProfile(admin,
                                        "Founder della piattaforma e collezionista di vinili.",
                                        Set.of(genres.get(4), genres.get(9)),
                                        Set.of(instruments.get(3), instruments.get(5)),
                                        Set.of(artists.get(10), artists.get(22)));

                        createMusicalProfile(rocker,
                                        "Chitarrista rock vecchio stampo, cerco gente per jam session pesanti.",
                                        Set.of(genres.get(0), genres.get(1)),
                                        Set.of(instruments.get(0), instruments.get(2)),
                                        Set.of(artists.get(0), artists.get(1), artists.get(3), artists.get(29)));

                        createMusicalProfile(jazzer,
                                        "Sassofonista appassionata di jazz classico, sempre alla ricerca di nuovi groove.",
                                        Set.of(genres.get(2), genres.get(3), genres.get(9)),
                                        Set.of(instruments.get(4), instruments.get(6), instruments.get(3)),
                                        Set.of(artists.get(5), artists.get(6), artists.get(7), artists.get(22)));

                        createMusicalProfile(electro,
                                        "Produttore di musica elettronica, amo i sintetizzatori analogici.",
                                        Set.of(genres.get(5), genres.get(7)),
                                        Set.of(instruments.get(7), instruments.get(1)),
                                        Set.of(artists.get(13), artists.get(14), artists.get(40), artists.get(18)));

                        // 6. EVENTI
                        Event jam = createEvent("Summer Jam Session", "Evento aperto a tutti i generi.",
                                        LocalDateTime.now().plusMonths(1), "Parco della Musica", 50, admin);

                        createRegistration(jam, rocker);
                        createRegistration(jam, jazzer);

                        // 7. FORUM
                        ForumCategory catGen1 = createCategory("Discussioni Generali", "Chiacchiere sulla musica.");
                        ForumThread t1 = createThread("Consigli per iniziare la chitarra", catGen1, rocker);
                        createPost("Quale Gibson consigliate per il Blues?", t1, rocker);
                        createPost("Inizia con una Les Paul Studio!", t1, admin);

                        ForumCategory catGen2 = createCategory("Tendenze", "Cosa ne sarà della musica in futuro?");
                        ForumThread t2 = createThread("L'intelligenza artificiale sostituirà mai i compositori umani?",
                                        catGen2,
                                        jazzer);
                        createPost(
                                        "Domanda provocatoria: credete che un algoritmo potrà mai replicare il feeling di un assolo jazz?",
                                        t2, jazzer);
                        createPost("Secondo me scordatevelo. La musica è emozione umana, non un calcolo di probabilità.",
                                        t2,
                                        rocker);
                        createPost(
                                        "Forse l'AI sarà solo il nuovo sintetizzatore: all'inizio lo odiano tutti, poi diventa lo standard.",
                                        t2, jazzer);
                        createPost("Finché un robot non spacca una chitarra sul palco, per me non è musica! 🎸", t2,
                                        rocker);

                        /*
                         * Popoliamo il dizionario delle parolacce usato da CensuraService.
                         * Queste parole vengono caricate in cache all'avvio da
                         * CensuraService.@PostConstruct.
                         * INSERT IGNORE = se una parola esiste già, viene saltata senza errori.
                         */
                        seedParoleBandite();

                        System.out.println("***************************************************************");
                        System.out.println("[SUONA CON GIGI-BOOTSTRAP] Seeding completato con successo.");
                        System.out.println("***************************************************************");

                } catch (Exception e) {
                        System.err.println("Errore durante il Seeding: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        /*
         * seedParoleBandite(): popola il dizionario della censura.
         * Questa versione è "blindata": non crasha se la tabella non esiste ancora
         * e gestisce i duplicati senza fermare il seeding.
         */
        private void seedParoleBandite() {
                try {
                        // Lista delle parole selezionate
                        String[] parole = {
                                        "cazzo", "cazzi", "minchia", "coglione", "stronzo",
                                        "fanculo", "vaffanculo", "merdone", "bastardo", "figlio di puttana",
                                        "puttana", "troia", "merda", "merde", "cagare", "culo",
                                        "figa", "fottuto", "fottiti", "porco dio", "porca miseria",
                                        "porca madonna", "madonna", "dio cane", "dio porco",
                                        "spastico", "ritardato", "coglioni", "cazzata", "cazzate", "stronzata",
                                        "stronzate", "fottuta", "fottute", "sborra", "inculo", "minchione",
                                        "minkia", "culo", "cagata", "cagare", "puttanata", "troiata", "merdata",
                                        "merdoso", "bastardata", "figli di puttana", "figlia di puttana", "cazzone",
                                        "cazzoni", "stronzone", "stronzoni", "fottute", "fottuti", "fottuta", "fottute",
                                        "porcodio", "negro", "negri", "zingaro", "zingari", "frocio", "froci", "lesbica", "lesbiche",
                                        "ricchione", "ricchioni" 
                        };

                        int inserite = 0;
                        for (String parola : parole) {
                                if (!parolaBanditaRepository.existsByWordIgnoreCase(parola)) {
                                        parolaBanditaRepository.save(
                                                        ParolaBandita.builder()
                                                                        .word(parola)
                                                                        .category("Insulto")
                                                                        .build());
                                }
                        }

                        if (inserite > 0) {
                                System.out.println(
                                                "[CENSURA]: Dizionario popolato con " + inserite + " nuovi termini.");
                        } else {
                                System.out.println("[CENSURA]: Dizionario già aggiornato.");
                        }

                } catch (Exception e) {
                        // Questo catch esterno serve se la tabella 'banned_words' non esiste proprio.
                        // Invece di crashare l'app, stampiamo un avviso e l'app parte lo stesso.
                        System.err.println(
                                        "[AVVISO CENSURA]: Tabella non pronta, seeding rimandato: " + e.getMessage());
                }
        }

        /* ── HELPER ──────────────────────────────────────────────────────────── */

        private User createUser(String username, String email, User.Role role) {
                return userRepository.save(User.builder()
                                .username(username).email(email).role(role)
                                .password(passwordEncoder.encode("suonacongigi")).status(true).build());
        }

        private Genre createGenre(String name) {
                // Prima di salvare, controlliamo se il genere esiste già (case-insensitive)
                return genreRepository.findByNameIgnoreCase(name)
                                .orElseGet(() -> genreRepository.save(Genre.builder().name(name).build()));
        }

        private Instrument createInstrument(String name) {
                return instrumentRepository.save(Instrument.builder().name(name).build());
        }

        private Artist createArtist(String name) {
                return artistRepository.save(Artist.builder().name(name).build());
        }

        private void createMusicalProfile(User user, String bio, Set<Genre> g, Set<Instrument> i, Set<Artist> a) {
                musicalProfileRepository.save(MusicalProfile.builder()
                                .user(user).bio(bio).genres(g).instruments(i).favoriteArtists(a).build());
        }

        private Event createEvent(String title, String desc, LocalDateTime date, String loc, int seats, User creator) {
                return eventRepository.save(Event.builder()
                                .title(title).description(desc).eventDate(date)
                                .location(loc).maxSeats(seats).createdBy(creator).build());
        }

        private void createRegistration(Event event, User user) {
                eventRegistrationRepository.save(EventRegistration.builder()
                                .event(event).user(user).registeredAt(LocalDateTime.now()).build());
        }

        private ForumCategory createCategory(String name, String desc) {
                return forumCategoryRepository.save(ForumCategory.builder().name(name).description(desc).build());
        }

        private ForumThread createThread(String title, ForumCategory cat, User author) {
                return forumThreadRepository.save(ForumThread.builder()
                                .title(title).category(cat).author(author).createdAt(LocalDateTime.now()).build());
        }

        private void createPost(String content, ForumThread thread, User author) {
                postRepository.save(Post.builder()
                                .content(content).thread(thread).author(author).createdAt(LocalDateTime.now()).build());
        }
}