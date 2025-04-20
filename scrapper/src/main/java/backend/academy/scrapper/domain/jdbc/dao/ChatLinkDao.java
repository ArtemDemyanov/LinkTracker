package backend.academy.scrapper.domain.jdbc.dao;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ChatLinkDao {

    private final JdbcTemplate jdbcTemplate;

    public ChatLinkDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Long chatId, Long linkId) {
        jdbcTemplate.update(
                "INSERT INTO chat_link (chat_id, link_id) VALUES (?, ?) ON CONFLICT DO NOTHING", chatId, linkId);
    }

    public void delete(Long chatId, Long linkId) {
        jdbcTemplate.update("DELETE FROM chat_link WHERE chat_id = ? AND link_id = ?", chatId, linkId);
    }

    public List<Long> findChatIdsByLinkId(Long linkId) {
        return jdbcTemplate.queryForList("SELECT chat_id FROM chat_link WHERE link_id = ?", Long.class, linkId);
    }
}
