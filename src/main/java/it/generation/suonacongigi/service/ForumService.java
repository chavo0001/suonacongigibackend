package it.generation.suonacongigi.service;

import it.generation.suonacongigi.dto.forum.*;
import it.generation.suonacongigi.model.*;
import it.generation.suonacongigi.repository.user.UserRepository;
import it.generation.suonacongigi.repository.forum.*;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumService {

        private final ForumCategoryRepository categoryRepository;
        private final ForumThreadRepository threadRepository;
        private final PostRepository postRepository;
        private final UserRepository userRepository;
        private final CensuraService censuraService;

        // ── CATEGORIE ────────────────────────────────────────────────

        @Transactional(readOnly = true)
        public List<CategoryResponse> getCategories() {
                return Objects.requireNonNull(categoryRepository.findAll().stream()
                                .map(this::toCategoryResponse) // usa censuraAttiva=false (utente non loggato)
                                .collect(Collectors.toList()));
        }

        // ── LISTA THREAD PER CATEGORIA ───────────────────────────────

        @Transactional(readOnly = true)
        public List<ThreadSummaryResponse> getThreadsByCategory(Long categoryId) {
                Long cleanId = Objects.requireNonNull(categoryId);
                // Endpoint pubblico: nessun utente loggato → filtro di default attivo
                return Objects.requireNonNull(threadRepository.findByCategoryIdOrderByCreatedAtDesc(cleanId)
                                .stream()
                                .map(t -> toThreadSummary(t, true)) // ← censura di default per utenti non loggati
                                .collect(Collectors.toList()));
        }

        // Versione con utente loggato (chiamata da getThreadDetail per consistenza)
        @Transactional(readOnly = true)
        public List<ThreadSummaryResponse> getThreadsByCategoryForUser(Long categoryId, @Nullable String username) {
                Long cleanId = Objects.requireNonNull(categoryId);
                boolean censuraAttiva = resolveCensura(username);
                return Objects.requireNonNull(threadRepository.findByCategoryIdOrderByCreatedAtDesc(cleanId)
                                .stream()
                                .map(t -> toThreadSummary(t, censuraAttiva))
                                .collect(Collectors.toList()));
        }

        // ── DETTAGLIO THREAD (post + titolo + categoria) ─────────────

        @Transactional(readOnly = true)
        public ThreadDetailResponse getThreadDetail(Long threadId, @Nullable String currentUsername) {
                Long cleanId = Objects.requireNonNull(threadId);
                ForumThread thread = threadRepository.findWithGraphById(cleanId)
                                .orElseThrow(() -> new NoSuchElementException("Thread non trovato"));

                // Risolviamo le preferenze dell'utente UNA SOLA VOLTA
                // e le usiamo per filtrare sia il titolo che i post.
                boolean censuraAttiva = resolveCensura(currentUsername);

                // 🔧 FIX: filtriamo anche il TITOLO del thread
                String titoloFiltrato = censuraService.filtraSeNecessario(
                                Objects.requireNonNull(thread.getTitle()),
                                censuraAttiva);

                // 🔧 FIX: filtriamo anche il NOME della categoria
                String categoriaNomeFiltrata = censuraService.filtraSeNecessario(
                                Objects.requireNonNull(thread.getCategory().getName()),
                                censuraAttiva);

                List<PostResponse> posts = Objects.requireNonNull(
                                postRepository.findByThreadIdOrderByCreatedAtAsc(cleanId).stream()
                                                .map(p -> toPostResponse(Objects.requireNonNull(p), currentUsername,
                                                                censuraAttiva))
                                                .collect(Collectors.toList()));

                return Objects.requireNonNull(ThreadDetailResponse.builder()
                                .id(thread.getId())
                                .title(titoloFiltrato) // ← titolo censurato
                                .categoryName(categoriaNomeFiltrata) // ← categoria censurata
                                .posts(posts)
                                .build());
        }

        // ── CREA THREAD ──────────────────────────────────────────────

        @Transactional
        public ThreadSummaryResponse createThread(ThreadRequest req, String username) {
                User author = getUserOrThrow(username);
                ForumCategory category = categoryRepository.findById(Objects.requireNonNull(req.getCategoryId()))
                                .orElseThrow(() -> new NoSuchElementException("Categoria non trovata"));

                ForumThread threadToSave = ForumThread.builder()
                                .title(Objects.requireNonNull(req.getTitle()))
                                .category(category)
                                .author(author)
                                .build();

                ForumThread thread = threadRepository.save(Objects.requireNonNull(threadToSave));

                Post postToSave = Post.builder()
                                .content(Objects.requireNonNull(req.getContent()))
                                .thread(thread)
                                .author(author)
                                .build();

                postRepository.save(Objects.requireNonNull(postToSave));

                // Il summary restituito dopo la creazione usa le preferenze dell'autore
                return toThreadSummary(Objects.requireNonNull(thread), author.isCensuraAttiva());
        }

        // ── AGGIUNGI POST ─────────────────────────────────────────────

        @Transactional
        public PostResponse addPost(Long threadId, PostRequest req, String username) {
                ForumThread thread = threadRepository.findById(Objects.requireNonNull(threadId))
                                .orElseThrow(() -> new NoSuchElementException("Thread non trovato"));
                User author = getUserOrThrow(username);

                // Il contenuto viene salvato nel DB SEMPRE ORIGINALE (non censurato).
                // La censura è applicata SOLO sul DTO di risposta.
                Post postToSave = Post.builder()
                                .content(Objects.requireNonNull(req.getContent()))
                                .thread(thread)
                                .author(author)
                                .build();

                Post saved = postRepository.save(Objects.requireNonNull(postToSave));
                return toPostResponse(Objects.requireNonNull(saved), username, author.isCensuraAttiva());
        }

        // ── ELIMINA POST ──────────────────────────────────────────────

        @Transactional
        public void deletePost(Long postId, String username, boolean isAdmin) {
                Post post = postRepository.findById(Objects.requireNonNull(postId))
                                .orElseThrow(() -> new NoSuchElementException("Post non trovato"));

                if (!post.getAuthor().getUsername().equals(username) && !isAdmin) {
                        throw new IllegalStateException("Azione negata: non sei l'autore");
                }

                postRepository.delete(post);
        }

        // ── ELIMINA THREAD ────────────────────────────────────────────
        @Transactional
        public void deleteThread(Long threadId, String username, boolean isAdmin) {
                ForumThread thread = threadRepository.findById(Objects.requireNonNull(threadId))
                                .orElseThrow(() -> new NoSuchElementException("Thread non trovato"));

                if (!thread.getAuthor().getUsername().equals(username) && !isAdmin) {
                        throw new IllegalStateException("Azione negata: non sei l'autore del thread");
                }

                // Eliminiamo prima tutti i post, poi il thread
                postRepository.deleteAll(
                                postRepository.findByThreadIdOrderByCreatedAtAsc(threadId));
                threadRepository.delete(thread);
        }

        // ── MAPPER PRIVATI ────────────────────────────────────────────

        // 🔧 FIX: toCategoryResponse ora filtra nome e descrizione
        private CategoryResponse toCategoryResponse(ForumCategory c) {
                Long id = Objects.requireNonNull(c.getId());

                // Le categorie sono endpoint pubblici → usiamo censura di default (true)
                // Se vuoi un endpoint autenticato per le categorie, aggiungi il parametro
                // censuraAttiva come negli altri mapper.
                String nomeFiltrato = censuraService.filtraSeNecessario(
                                Objects.requireNonNull(c.getName()), true);

                String descrizioneFiltrata = c.getDescription() != null
                                ? censuraService.filtraSeNecessario(c.getDescription(), true)
                                : null;

                return Objects.requireNonNull(CategoryResponse.builder()
                                .id(id)
                                .name(nomeFiltrato) // ← nome censurato
                                .description(descrizioneFiltrata) // ← descrizione censurata
                                .threadCount(threadRepository.countByCategoryId(id))
                                .build());
        }

        // 🔧 FIX: toThreadSummary ora accetta il flag e filtra il titolo
        private ThreadSummaryResponse toThreadSummary(ForumThread t, boolean censuraAttiva) {
                String titoloFiltrato = censuraService.filtraSeNecessario(
                                Objects.requireNonNull(t.getTitle()), censuraAttiva);

                String categoriaNomeFiltrata = censuraService.filtraSeNecessario(
                                Objects.requireNonNull(t.getCategory().getName()), censuraAttiva);

                return Objects.requireNonNull(ThreadSummaryResponse.builder()
                                .id(Objects.requireNonNull(t.getId()))
                                .title(titoloFiltrato) // ← titolo censurato
                                .authorName(Objects.requireNonNull(t.getAuthor().getUsername()))
                                .categoryName(categoriaNomeFiltrata) // ← categoria censurata
                                .createdAt(t.getCreatedAt())
                                .postCount(postRepository.countByThreadId(t.getId()))
                                .build());
        }

        // (invariato, già corretto)
        private PostResponse toPostResponse(Post p, @Nullable String currentUsername, boolean censuraAttiva) {
                String contenutoCensurato = censuraService.filtraSeNecessario(
                                Objects.requireNonNull(p.getContent()),
                                censuraAttiva);

                return Objects.requireNonNull(PostResponse.builder()
                                .id(Objects.requireNonNull(p.getId()))
                                .content(contenutoCensurato)
                                .authorName(Objects.requireNonNull(p.getAuthor().getUsername()))
                                .createdAt(p.getCreatedAt())
                                .canEdit(currentUsername != null && p.getAuthor().getUsername().equals(currentUsername))
                                .build());
        }

        private boolean resolveCensura(@Nullable String username) {
                if (username == null)
                        return true;
                return userRepository.findByUsername(username)
                                .map(User::isCensuraAttiva)
                                .orElse(true);
        }

        private User getUserOrThrow(String username) {
                User user = userRepository.findByUsername(Objects.requireNonNull(username))
                                .orElseThrow(() -> new NoSuchElementException("Utente non trovato: " + username));
                return Objects.requireNonNull(user);
        }
}