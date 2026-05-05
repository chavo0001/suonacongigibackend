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

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return Objects.requireNonNull(categoryRepository.findAll().stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public List<ThreadSummaryResponse> getThreadsByCategory(Long categoryId) {
        Long cleanId = Objects.requireNonNull(categoryId);
        return Objects.requireNonNull(threadRepository.findByCategoryIdOrderByCreatedAtDesc(cleanId)
                .stream()
                .map(t -> toThreadSummary(t, true))
                .collect(Collectors.toList()));
    }
    
    // Cerca nel forum usando una parola chiave inserita dall'utente.
    // Restituisce risultati dettagliati, indicando se il match è nel titolo o nel contenuto di un post.
    @Transactional(readOnly = true)
    public List<ForumSearchResultResponse> searchThreads(String search) {
        String cleanSearch = Objects.requireNonNull(search).trim();

        if (cleanSearch.isBlank()) {
            return List.of();
        }

        List<ForumSearchResultResponse> titleMatches = threadRepository.searchThreads(cleanSearch)
                .stream()
                .filter(thread -> thread.getTitle().toLowerCase().contains(cleanSearch.toLowerCase()))
                .map(thread -> toSearchResult(thread, null, thread.getTitle(), "TITLE", true))
                .collect(Collectors.toList());

        List<ForumSearchResultResponse> postMatches = postRepository.searchPosts(cleanSearch)
                .stream()
                .map(post -> toSearchResult(post.getThread(), post.getId(), post.getContent(), "POST", true))
                .collect(Collectors.toList());

        titleMatches.addAll(postMatches);

        return titleMatches;
    }



    @Transactional(readOnly = true)
    public ThreadDetailResponse getThreadDetail(Long threadId, @Nullable String currentUsername) {
        Long cleanId = Objects.requireNonNull(threadId);
        ForumThread thread = threadRepository.findWithGraphById(cleanId)
                .orElseThrow(() -> new NoSuchElementException("Thread non trovato"));

        boolean censuraAttiva = resolveCensura(currentUsername);

        List<PostResponse> posts = Objects.requireNonNull(postRepository.findByThreadIdOrderByCreatedAtAsc(cleanId).stream()
                .map(p -> toPostResponse(Objects.requireNonNull(p), currentUsername, censuraAttiva))
                .collect(Collectors.toList()));

        return Objects.requireNonNull(ThreadDetailResponse.builder()
                .id(thread.getId())
                .title(censuraService.filtraSeNecessario(Objects.requireNonNull(thread.getTitle()), censuraAttiva))
                .categoryName(censuraService.filtraSeNecessario(
                        Objects.requireNonNull(thread.getCategory().getName()), censuraAttiva))
                .posts(posts)
                .build());
    }

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

        return toThreadSummary(Objects.requireNonNull(thread), author.isCensuraAttiva());
    }

    @Transactional
    public PostResponse addPost(Long threadId, PostRequest req, String username) {
        ForumThread thread = threadRepository.findById(Objects.requireNonNull(threadId))
                .orElseThrow(() -> new NoSuchElementException("Thread non trovato"));
        User author = getUserOrThrow(username);

        Post postToSave = Post.builder()
                .content(Objects.requireNonNull(req.getContent()))
                .thread(thread)
                .author(author)
                .build();

        Post saved = postRepository.save(Objects.requireNonNull(postToSave));

        return toPostResponse(Objects.requireNonNull(saved), username, author.isCensuraAttiva());
    }

    @Transactional
    public void deletePost(Long postId, String username, boolean isAdmin) {
        Post post = postRepository.findById(Objects.requireNonNull(postId))
                .orElseThrow(() -> new NoSuchElementException("Post non trovato"));

        if (!post.getAuthor().getUsername().equals(username) && !isAdmin) {
            throw new IllegalStateException("Azione negata: non sei l'autore");
        }

        postRepository.delete(post);
    }

    @Transactional
    public void deleteThread(Long threadId, String username, boolean isAdmin) {
        ForumThread thread = threadRepository.findById(Objects.requireNonNull(threadId))
                .orElseThrow(() -> new NoSuchElementException("Thread non trovato"));

        if (!thread.getAuthor().getUsername().equals(username) && !isAdmin) {
            throw new IllegalStateException("Azione negata: non sei l'autore del thread");
        }

        postRepository.deleteAll(postRepository.findByThreadIdOrderByCreatedAtAsc(threadId));
        threadRepository.delete(thread);
    }

    // --- Mapper Certificati (Strict Null Safety) ---

    private ForumSearchResultResponse toSearchResult(
            ForumThread thread, Long matchedPostId, String snippet, String matchType, boolean censuraAttiva) {
        return Objects.requireNonNull(ForumSearchResultResponse.builder()
            .threadId(Objects.requireNonNull(thread.getId()))
            .title(censuraService.filtraSeNecessario(Objects.requireNonNull(thread.getTitle()), censuraAttiva))
            .categoryName(censuraService.filtraSeNecessario(
                    Objects.requireNonNull(thread.getCategory().getName()), censuraAttiva))
            .authorName(Objects.requireNonNull(thread.getAuthor().getUsername()))
            .createdAt(thread.getCreatedAt())
            .postCount(postRepository.countByThreadId(thread.getId()))
            .matchedPostId(matchedPostId)
            .snippet(censuraService.filtraSeNecessario(snippet, censuraAttiva))
            .matchType(matchType)
            .build());
    }


    private CategoryResponse toCategoryResponse(ForumCategory c) {
        Long id = Objects.requireNonNull(c.getId());
        return Objects.requireNonNull(CategoryResponse.builder()
                .id(id)
                .name(censuraService.filtraSeNecessario(Objects.requireNonNull(c.getName()), true))
                .description(c.getDescription() != null
                        ? censuraService.filtraSeNecessario(c.getDescription(), true)
                        : null)
                .threadCount(threadRepository.countByCategoryId(id))
                .build());
    }

    private ThreadSummaryResponse toThreadSummary(ForumThread t, boolean censuraAttiva) {
        return Objects.requireNonNull(ThreadSummaryResponse.builder()
                .id(Objects.requireNonNull(t.getId()))
                .title(censuraService.filtraSeNecessario(Objects.requireNonNull(t.getTitle()), censuraAttiva))
                .authorName(Objects.requireNonNull(t.getAuthor().getUsername()))
                .categoryName(censuraService.filtraSeNecessario(
                        Objects.requireNonNull(t.getCategory().getName()), censuraAttiva))
                .createdAt(t.getCreatedAt())
                .postCount(postRepository.countByThreadId(t.getId()))
                .build());
    }

    private PostResponse toPostResponse(Post p, @Nullable String currentUsername, boolean censuraAttiva) {
        return Objects.requireNonNull(PostResponse.builder()
                .id(Objects.requireNonNull(p.getId()))
                .content(censuraService.filtraSeNecessario(Objects.requireNonNull(p.getContent()), censuraAttiva))
                .authorName(Objects.requireNonNull(p.getAuthor().getUsername()))
                .createdAt(p.getCreatedAt())
                .canEdit(currentUsername != null && p.getAuthor().getUsername().equals(currentUsername))
                .build());
    }

    private boolean resolveCensura(@Nullable String username) {
        if (username == null) {
            return true;
        }
        return userRepository.findByUsername(username)
                .map(User::isCensuraAttiva)
                .orElse(true);
    }

    private User getUserOrThrow(String username) {
        // Certifichiamo il ritorno di orElseThrow per @NonNullApi
        User user = userRepository.findByUsername(Objects.requireNonNull(username))
                .orElseThrow(() -> new NoSuchElementException("Utente non trovato: " + username));

        // La catena è sicura, ma certifichiamo il risultato
        return Objects.requireNonNull(user);
    }
}
