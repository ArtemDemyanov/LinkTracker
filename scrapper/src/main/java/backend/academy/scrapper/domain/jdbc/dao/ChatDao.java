package backend.academy.scrapper.domain.jdbc.dao;

import backend.academy.scrapper.domain.jdbc.dto.Chat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ChatDao {

    private final JdbcTemplate jdbcTemplate;

    public ChatDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Chat chat) {
        jdbcTemplate.update("INSERT INTO chat (id) VALUES (?) ON CONFLICT DO NOTHING", chat.id());
    }

    public void delete(Long chatId) {
        jdbcTemplate.update("DELETE FROM chat WHERE id = ?", chatId);
    }
}
