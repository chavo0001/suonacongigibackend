package it.generation.suonacongigi.dto.forum;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumSearchResultResponse {
    private Long threadId;
    private String title;
    private String categoryName;
    private String authorName;
    private LocalDateTime createdAt;
    private long postCount;

    private Long matchedPostId;
    private String snippet;
    private String matchType;
}
