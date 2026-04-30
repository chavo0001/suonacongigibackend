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
                .map(this::toThreadSummary)
                .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public ThreadDetailResponse getThreadDetail(Long threadId, @Nullable String currentUsername) {
        Long cleanId = Objects.requireNonNull(threadId);
        ForumThread thread = threadRepository.findWithGraphById(cleanId)
                .orElseThrow(() -> new NoSuchElementException("Thread non trovato"));

        List<PostResponse> posts = Objects.requireNonNull(postRepository.findByThreadIdOrderByCreatedAtAsc(cleanId).stream()
                .map(p -> toPostResponse(Objects.requireNonNull(p), currentUsername))
                .collect(Collectors.toList()));

        return Objects.requireNonNull(ThreadDetailResponse.builder()
                .id(thread.getId())
                .title(Objects.requireNonNull(thread.getTitle()))
                .categoryName(Objects.requireNonNull(thread.getCategory().getName()))
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

        return toThreadSummary(Objects.requireNonNull(thread));
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

        return toPostResponse(Objects.requireNonNull(saved), username);
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

    // --- Mapper Certificati (Strict Null Safety) ---

    private CategoryResponse toCategoryResponse(ForumCategory c) {
        Long id = Objects.requireNonNull(c.getId());
        return Objects.requireNonNull(CategoryResponse.builder()
                .id(id)
                .name(Objects.requireNonNull(c.getName()))
                .description(c.getDescription())
                .threadCount(threadRepository.countByCategoryId(id))
                .build());
    }

    private ThreadSummaryResponse toThreadSummary(ForumThread t) {
        return Objects.requireNonNull(ThreadSummaryResponse.builder()
                .id(Objects.requireNonNull(t.getId()))
                .title(Objects.requireNonNull(t.getTitle()))
                .authorName(Objects.requireNonNull(t.getAuthor().getUsername()))
                .categoryName(Objects.requireNonNull(t.getCategory().getName()))
                .createdAt(t.getCreatedAt())
                .postCount(postRepository.countByThreadId(t.getId()))
                .build());
    }

    private PostResponse toPostResponse(Post p, @Nullable String currentUsername) {
        return Objects.requireNonNull(PostResponse.builder()
                .id(Objects.requireNonNull(p.getId()))
                .content(Objects.requireNonNull(p.getContent()))
                .authorName(Objects.requireNonNull(p.getAuthor().getUsername()))
                .createdAt(p.getCreatedAt())
                .canEdit(currentUsername != null && p.getAuthor().getUsername().equals(currentUsername))
                .build());
    }

    private User getUserOrThrow(String username) {
        // Certifichiamo il ritorno di orElseThrow per @NonNullApi
        User user = userRepository.findByUsername(Objects.requireNonNull(username))
                .orElseThrow(() -> new NoSuchElementException("Utente non trovato: " + username));

        // La catena è sicura, ma certifichiamo il risultato
        return Objects.requireNonNull(user);
    }
}