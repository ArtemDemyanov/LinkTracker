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

    public boolean existsById(Long chatId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chat WHERE id = ?", Integer.class, chatId);
        return count != null && count > 0;
    }

    public void delete(Long chatId) {
        jdbcTemplate.update("DELETE FROM chat WHERE id = ?", chatId);
    }
}
