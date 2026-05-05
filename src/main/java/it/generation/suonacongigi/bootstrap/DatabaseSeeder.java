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

/**
 * ARCHITETTURA: Infrastructure Bootstrap.
 * Questo componente bypassa l'astrazione di Hibernate (JPA) per agire direttamente 
 * tramite JDBC. È un esempio di "Controllo Totale": prima di far partire l'ORM, 
 * resettiamo l'ambiente fisico per garantire il determinismo dei test.
 */
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
    private final EmailTemplateRepository emailTemplateRepository;

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
            return;
        }

        executeSeeding();
    }

    /**
     * MECCANICA: DDL Execution via JDBC.
     * Utilizziamo SQL puro per definire i vincoli. L'uso sistematico di CONSTRAINT 
     * permette di mappare errori del DB a nomi logici comprensibili nel codice Java.
     */
    private void rebuildDatabaseSchema() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            String dbName = conn.getCatalog();
            
            stmt.execute("DROP DATABASE IF EXISTS " + dbName);
            stmt.execute("CREATE DATABASE "         + dbName);
            stmt.execute("USE "                     + dbName);

            System.out.println("***************************************************************");
            System.out.println("Creazione della Base Dati Iniziale ");
            System.out.println("***************************************************************");

            // Utenti
            stmt.execute("CREATE TABLE users ("          +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "email VARCHAR(100) NOT NULL UNIQUE, "   +
                "password VARCHAR(255) NOT NULL, "       +
                "role VARCHAR(20) NOT NULL, "            +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "+
                "status BOOLEAN, " +
                "censura_attiva BOOLEAN NOT NULL DEFAULT TRUE) ENGINE=InnoDB");

            // Dominio dei Generi Musicali
            stmt.execute("CREATE TABLE genres ("         + 
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " + 
                "name VARCHAR(100) NOT NULL UNIQUE) ENGINE=InnoDB");

            // Dominio degli Strumenti Musicali
            stmt.execute("CREATE TABLE instruments ("    + 
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " + 
                "name VARCHAR(100) NOT NULL UNIQUE) ENGINE=InnoDB");

            // Dominio degli Artisti
            stmt.execute("CREATE TABLE artists ("        + 
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " + 
                "name VARCHAR(100) NOT NULL UNIQUE) ENGINE=InnoDB");

            // VerificationToken 
            stmt.execute("CREATE TABLE verification_Token ("          +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id VARCHAR(50) NOT NULL UNIQUE, " +
                "token VARCHAR(250), " +
                "expires_at TIMESTAMP) ENGINE=InnoDB");   
                
                
            // EmailTemplate
            stmt.execute("CREATE TABLE email_template ("          +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(50) NOT NULL UNIQUE, " +
                "sender VARCHAR(100) NOT NULL, " +
                "subject VARCHAR(100) NOT NULL, " +
                "content TEXT) ENGINE=InnoDB");     

            // Profili Musicali (Relazioni 1:1 e N:M)
            stmt.execute("CREATE TABLE musical_profiles (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, "   +
                "user_id BIGINT NOT NULL UNIQUE, "         +
                "bio TEXT, "                               +
                "CONSTRAINT fk_profile_user "              +  
                "FOREIGN KEY (user_id) "                   +  
                "REFERENCES users(id) ON DELETE CASCADE) ENGINE=InnoDB");
            
            // Tabella di Join Profilo-Genere N:M
            stmt.execute("CREATE TABLE profile_genres ("              +
                "profile_id BIGINT NOT NULL, "                        + 
                "genre_id BIGINT NOT NULL, "                          +
                "PRIMARY KEY (profile_id, genre_id), "                +
                "CONSTRAINT fk_pg_profile "                           + 
                "FOREIGN KEY (profile_id) "                           + 
                "REFERENCES musical_profiles(id) ON DELETE CASCADE, " +
                "CONSTRAINT fk_pg_genre "                             + 
                "FOREIGN KEY (genre_id) "                             + 
                "REFERENCES genres(id) ON DELETE CASCADE) ENGINE=InnoDB");

            // Tabella di Join Profilo-Strumento N:M
            stmt.execute("CREATE TABLE profile_instruments ("         +
                "profile_id BIGINT NOT NULL, "                        + 
                "instrument_id BIGINT NOT NULL, "                     +
                "PRIMARY KEY (profile_id, instrument_id), "           +
                "CONSTRAINT fk_pi_profile "                           + 
                "FOREIGN KEY (profile_id) "                           + 
                "REFERENCES musical_profiles(id) ON DELETE CASCADE, " +
                "CONSTRAINT fk_pi_instrument "                        + 
                "FOREIGN KEY (instrument_id) "                        + 
                "REFERENCES instruments(id) ON DELETE CASCADE) ENGINE=InnoDB");

            // Tabella di Join Profilo-Artista N:M
            stmt.execute("CREATE TABLE profile_artists ("             +
                "profile_id BIGINT NOT NULL, "                        + 
                "artist_id BIGINT NOT NULL, "                         +
                "PRIMARY KEY (profile_id, artist_id), "               +
                "CONSTRAINT fk_pa_profile "                           + 
                "FOREIGN KEY (profile_id) "                           + 
                "REFERENCES musical_profiles(id) ON DELETE CASCADE, " +
                "CONSTRAINT fk_pa_artist "                            + 
                "FOREIGN KEY (artist_id) "                            + 
                "REFERENCES artists(id) ON DELETE CASCADE) ENGINE=InnoDB");

            // Eventi
            stmt.execute("CREATE TABLE events ("             +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " + 
                "title VARCHAR(200) NOT NULL, "          + 
                "description TEXT, "                     +
                "event_date DATETIME NOT NULL, "         + 
                "location VARCHAR(255) NOT NULL, "       + 
                "max_seats INT NOT NULL, "               +
                "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " +
                "created_by_id BIGINT NOT NULL, "        +
                "CONSTRAINT fk_event_creator "           + 
                "FOREIGN KEY (created_by_id) "           + 
                "REFERENCES users(id)) ENGINE=InnoDB");
            
            // Registrazioni Eventi 
            stmt.execute("CREATE TABLE event_registrations ("         +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, "              + 
                "event_id BIGINT NOT NULL, "                          + 
                "user_id BIGINT NOT NULL, "                           +
                "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE(event_id, user_id), "                         +
                "CONSTRAINT fk_reg_event "                            + 
                "FOREIGN KEY (event_id) "                             + 
                "REFERENCES events(id) ON DELETE CASCADE, "           +
                "CONSTRAINT fk_reg_user "                             + 
                "FOREIGN KEY (user_id) "                              + 
                "REFERENCES users(id) ON DELETE CASCADE) ENGINE=InnoDB");

            // Forum
            stmt.execute("CREATE TABLE forum_categories (" + 
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " + 
                "name VARCHAR(100) NOT NULL UNIQUE, description TEXT) ENGINE=InnoDB");

            // Thread del Forum 
            stmt.execute("CREATE TABLE forum_threads ("               +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, "              + 
                "title VARCHAR(200) NOT NULL, "                       + 
                "category_id BIGINT NOT NULL, "                       + 
                "author_id BIGINT NOT NULL, "                         +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "    +
                "CONSTRAINT fk_thread_category "                      + 
                "FOREIGN KEY (category_id) "                          + 
                "REFERENCES forum_categories(id) ON DELETE CASCADE, " +
                "CONSTRAINT fk_thread_author "                        + 
                "FOREIGN KEY (author_id) "                            + 
                "REFERENCES users(id)) ENGINE=InnoDB");

            // Post del Forum
            stmt.execute("CREATE TABLE posts ("                    +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, "           + 
                "content TEXT NOT NULL, "                          + 
                "thread_id BIGINT NOT NULL, "                      +
                "author_id BIGINT NOT NULL, "                      +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_post_thread "                       + 
                "FOREIGN KEY (thread_id) "                         + 
                "REFERENCES forum_threads(id) ON DELETE CASCADE, " +
                "CONSTRAINT fk_post_author "                       + 
                "FOREIGN KEY (author_id) "                         + 
                "REFERENCES users(id)) ENGINE=InnoDB");

            stmt.execute("CREATE TABLE parole_bandite (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "word VARCHAR(100) NOT NULL UNIQUE, " +
                "category VARCHAR(50), " +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "INDEX idx_word (word)) " +
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
            for (String gn : genreNames) genres.add(createGenre(gn));

            // 2. STRUMENTI MUSICALI (8)
            String[] instNames = {
                "Chitarra Elettrica", "Basso Elettrico", "Batteria", 
                "Pianoforte", "Sassofono", "Violino", "Tromba", "Sintetizzatore"
            };
            List<Instrument> instruments = new java.util.ArrayList<>();
            for (String in : instNames) instruments.add(createInstrument(in));

            // 3. ARTISTI (50) - Generazione programmatica per varietà
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
            for (String an : artistPool) artists.add(createArtist(an));

            // 4. UTENTI (1 Admin + 3 User)
            User admin   = createUser("admin",        "admin@suonacongigi.it", User.Role.ADMIN);
            User rocker  = createUser("mario_gibson", "mario@email.it",        User.Role.USER);
            User jazzer  = createUser("elena_sax",    "elena@email.it",        User.Role.USER);
            User electro = createUser("luca_synth",   "luca@email.it",         User.Role.USER);

            // 5. PROFILI MUSICALI (Associazione relazioni Many-to-Many)
            // Admin Profile: Pop, Soul - Pianoforte, Voce - The Beatles, Aretha Franklin
            createMusicalProfile(admin, 
                "Founder della piattaforma e collezionista di vinili.",
                Set.of(genres.get(4), genres.get(9)), 
                Set.of(instruments.get(3), instruments.get(5)), 
                Set.of(artists.get(10), artists.get(22))
            );

            // Rocker: Rock, Metal, Punk - Chitarra, Batteria - Hendrix, Zeppelin, Metallica, AC/DC
            createMusicalProfile(rocker, 
                "Chitarrista rock vecchio stampo, cerco gente per jam session pesanti.",
                Set.of(genres.get(0), genres.get(1)), 
                Set.of(instruments.get(0), instruments.get(2)), 
                Set.of(artists.get(0), artists.get(1), artists.get(3), artists.get(29))
            );

            // Jazzer: Jazz, Blues, Soul - Sassofono, Tromba, Piano - Davis, Coltrane, Evans, Franklin
            createMusicalProfile(jazzer, 
                "Sassofonista appassionata di jazz classico, sempre alla ricerca di nuovi groove.",
                Set.of(genres.get(2), genres.get(3), genres.get(9)), 
                Set.of(instruments.get(4), instruments.get(6), instruments.get(3)), 
                Set.of(artists.get(5), artists.get(6), artists.get(7), artists.get(22))
            );

            // Electro: Electronic, Funk - Sintetizzatore, Basso - Daft Punk, Kraftwerk, Aphex Twin, James Brown
            createMusicalProfile(electro, 
                "Produttore di musica elettronica, amo i sintetizzatori analogici.",
                Set.of(genres.get(5), genres.get(7)), 
                Set.of(instruments.get(7), instruments.get(1)), 
                Set.of(artists.get(13), artists.get(14), artists.get(40), artists.get(18))
            );

            // 6. EVENTI & FORUM (Per rendere il DB "vivo")
            Event jam = createEvent("Summer Jam Session", "Evento aperto a tutti i generi.", 
                LocalDateTime.now().plusMonths(1), "Parco della Musica", 50, admin, Event.EventStatus.APPROVED);
            
            createRegistration(jam, rocker);
            createRegistration(jam, jazzer);

            ForumCategory catGen1 = createCategory("Discussioni Generali", "Chiacchiere sulla musica.");
            ForumThread t1 = createThread("Consigli per iniziare la chitarra", catGen1, rocker);
            createPost("Quale Gibson consigliate per il Blues?", t1, rocker);
            createPost("Inizia con una Les Paul Studio!", t1, admin);

            ForumCategory catGen2 = createCategory("Tendenze", "Cosa ne sarà della musica in futuro?");
            ForumThread t2 = createThread("L'intelligenza artificiale sostituirà mai i compositori umani?", catGen2, jazzer);
            createPost("Domanda provocatoria: credete che un algoritmo potrà mai replicare il feeling e l'imprevedibilità di un assolo jazz registrato dal vivo?", t2, jazzer);
            createPost("Secondo me scordatevelo. Manca il sudore, le corde rotte e l'anima. La musica è emozione umana, non un calcolo di probabilità su una scala pentatonica.", t2, rocker);
            createPost("Vero, però ho provato dei software che generano progressioni armoniche incredibili. Forse l'AI sarà solo il 'nuovo sintetizzatore': all'inizio lo odiano tutti, poi diventa lo standard.", t2, jazzer);
            createPost("Finché un robot non spacca una chitarra sul palco dopo un feedback assordante, per me non è musica! 🎸", t2, rocker);

            seedParoleBandite();
            creatEmailTemplate("verification", "<h2>Benvenuto!</h2> <p>Clicca sul link qui sotto per completare la registrazione:</p> <a href=\"{{link}}\" style=\"color: blue; font-weight: bold;\">Verifica il tuo account</a> <p>Hai 24 ore per completare la verifica.</p>", "noreply@suonacongigi.it", "Conferma la tua mail");




            System.out.println("***************************************************************");
            System.out.println("[SUONA CON GIGI-BOOTSTRAP] Seeding completato: 62 anagrafiche, 4 utenti e 3 profili creati.");
            System.out.println("***************************************************************");
        } catch (Exception e) {
            System.err.println("Errore durante il Seeding: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // HELPER per la creazione delle entità User, per mantenere il codice DRY e leggibile. 
    // Ogni metodo incapsula la logica di salvataggio e gestione delle relazioni.
    private void seedParoleBandite() {
        try {
            String[] parole = {
                    "cazzo", "cazzi", "minchia", "coglione", "stronzo",
                    "fanculo", "vaffanculo", "merdone", "bastardo", "figlio di puttana",
                    "puttana", "troia", "merda", "merde", "cagare", "culo",
                    "figa", "fottuto", "fottiti", "porco dio", "porca miseria",
                    "porca madonna", "madonna", "dio cane", "dio porco",
                    "spastico", "ritardato", "coglioni", "cazzata", "cazzate", "stronzata",
                    "stronzate", "fottuta", "fottute", "sborra", "inculo", "minchione",
                    "minkia", "cagata", "puttanata", "troiata", "merdata",
                    "merdoso", "bastardata", "figli di puttana", "figlia di puttana", "cazzone",
                    "cazzoni", "stronzone", "stronzoni", "fottuti",
                    "porcodio", "negro", "negri", "zingaro", "zingari", "frocio", "froci",
                    "ricchione", "ricchioni", "Generation", "Italy", "Stupidotto", "Brutto",
                    "Brutta", "Brutti", "Brutte", "Scemo", "Scema", "Scemi", "Sceme",
                    "Idiota", "Idiotessa", "Idioti", "Idiotesse", "Cretino", "Cretina", "Cretini",
                    "Cretine", "Deficiente", "Deficientessa", "Deficienti",
                    "Imbecille", "Imbecilla", "Imbecilli", "Pazzo", "Pazza", "Pazzi",
                    "Pazze", "Matto", "Matta", "Matti", "Matte", "Scemo del villaggio",
                    "Scema del villaggio", "Scemi del villaggio", "Sceme del villaggio",
                    "Testa di cazzo", "Testa di minchia", "Testa di stronzo", "Testa di coglione",
                    "Testa di merda", "Testa di cazzi", "Testa di minchioni", "Testa di stronzoni",
                    "Testa di coglioni", "Testa di merdoso", "Figlio di puttanata",
                    "Figlio di troia", "Figlio di merda", "Figlio di bastardata",
                    "Figlio di figli di puttana", "Figlio di figlia di puttana",
                    "Cazzo di cane", "Cazzo di porco", "Cazzo di dio", "Cazzo di madonna",
                    "Cazzo di merda", "Vaffanculo di cane", "Vaffanculo di porco",
                    "Vaffanculo di dio", "stupido", "stupida", "stupidi", "stupide",
                    "brutto", "brutta", "brutti", "brutte", "scemo", "scema", "scemi",
                    "sceme", "idiota", "idiotessa", "idioti", "idiotesse", "cretino",
                    "cretina", "cretini", "cretine", "deficiente", "deficientessa",
                    "deficienti", "imbecille", "imbecilla", "imbecilli", "pazzo",
                    "pazza", "pazzi", "pazze", "matto", "matta", "matti", "matte",
                    "scemo del villaggio", "scema del villaggio", "scemi del villaggio",
                    "sceme del villaggio", "testa di cazzo", "testa di minchia",
                    "testa di stronzo", "testa di coglione", "testa di merda",
                    "testa di cazzi", "testa di minchioni", "testa di stronzoni",
                    "testa di coglioni", "testa di merdoso", "figlio di puttanata",
                    "figlio di troia", "figlio di merda", "figlio di bastardata",
                    "figlio di figli di puttana", "figlio di figlia di puttana",
                    "cazzo di cane", "cazzo di porco", "cazzo di dio", "cazzo di madonna",
                    "cazzo di merda", "vaffanculo di cane", "vaffanculo di porco",
                    "vaffanculo di dio", "vaffanculo di madonna", "vaffanculo di merda"
            };

            int inserite = 0;
            for (String parola : parole) {
                if (!parolaBanditaRepository.existsByWordIgnoreCase(parola)) {
                    parolaBanditaRepository.save(
                            ParolaBandita.builder()
                                    .word(parola)
                                    .category("Insulto")
                                    .build());
                    inserite++;
                }
            }

            if (inserite > 0) {
                System.out.println("[CENSURA]: Dizionario popolato con " + inserite + " nuovi termini.");
            } else {
                System.out.println("[CENSURA]: Dizionario gia aggiornato.");
            }
        } catch (Exception e) {
            System.err.println("[AVVISO CENSURA]: Tabella non pronta, seeding rimandato: " + e.getMessage());
        }
    }

    private User createUser(String username, String email, User.Role role) {
        return userRepository.save(User.builder()
                .username(username).email(email).role(role)
                .password(passwordEncoder.encode("suonacongigi")).status(true).build());
    }

    // HELPER per la creazione delle entità Genre
    private Genre createGenre(String name) {
        return genreRepository.save(Genre.builder().name(name).build());
    }

    // HELPER per la creazione delle entità Instrument
    private Instrument createInstrument(String name) {
        return instrumentRepository.save(Instrument.builder().name(name).build());
    }

    // HELPER per la creazione delle entità Artist
    private Artist createArtist(String name) {
        return artistRepository.save(Artist.builder().name(name).build());
    }

    // HELPER per la creazione del MusicalProfile, che gestisce le relazioni N:M con generi, strumenti e artisti
    private void createMusicalProfile(User user, String bio, Set<Genre> g, Set<Instrument> i, Set<Artist> a) {
        musicalProfileRepository.save(MusicalProfile.builder()
                .user(user).bio(bio).genres(g).instruments(i).favoriteArtists(a).build());
    }

    // HELPER per la creazione degli Eventi, che gestisce la relazione con l'utente creatore
    private Event createEvent(String title, String desc, LocalDateTime date, String loc, int seats, User creator, Event.EventStatus status) {
        return eventRepository.save(Event.builder()
                .title(title).description(desc).eventDate(date)
                .location(loc).maxSeats(seats).createdBy(creator).status(status).build());
    }

    // HELPER per la creazione delle Registrazioni agli Eventi, che gestisce la relazione con evento e utente
    private void createRegistration(Event event, User user) {
        eventRegistrationRepository.save(EventRegistration.builder()
                .event(event).user(user).registeredAt(LocalDateTime.now()).build());
    }

    // HELPER per la creazione delle categorie del Forum
    private ForumCategory createCategory(String name, String desc) {
        return forumCategoryRepository.save(ForumCategory.builder().name(name).description(desc).build());
    }

    // HELPER per la creazione dei Thread del Forum, che gestisce la relazione con categoria e autore
    private ForumThread createThread(String title, ForumCategory cat, User author) {
        return forumThreadRepository.save(ForumThread.builder()
                .title(title).category(cat).author(author).createdAt(LocalDateTime.now()).build());
    }

    // HELPER per la creazione dei Post del Forum, che gestisce la relazione con thread e autore
    private void createPost(String content, ForumThread thread, User author) {
        postRepository.save(Post.builder()
                .content(content).thread(thread).author(author).createdAt(LocalDateTime.now()).build());
    }

    private void creatEmailTemplate(String name, String content, String sender, String subject){
        emailTemplateRepository.save(EmailTemplate.builder()
                .name(name).sender(sender).subject(subject).content(content).build());
    }

}
