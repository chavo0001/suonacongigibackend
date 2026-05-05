//LIKE Configurazione aggiuntiva per creare la tabella dei like senza modificare application.properties.
package it.generation.suonacongigi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@RequiredArgsConstructor
public class EventLikeTableInitializer {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    ApplicationRunner eventLikeTableCreator() {
        return args -> jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS event_likes (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    event_id BIGINT NOT NULL,
                    user_id BIGINT NOT NULL,
                    liked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_event_likes_event_user (event_id, user_id),
                    CONSTRAINT fk_event_likes_event
                        FOREIGN KEY (event_id) REFERENCES events(id)
                        ON DELETE CASCADE,
                    CONSTRAINT fk_event_likes_user
                        FOREIGN KEY (user_id) REFERENCES users(id)
                        ON DELETE CASCADE
                )
                """);
    }
}
