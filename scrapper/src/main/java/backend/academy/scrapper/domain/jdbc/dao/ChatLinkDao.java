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

    public List<Long> findLinkIdsByChatId(Long chatId) {
        return jdbcTemplate.queryForList("SELECT link_id FROM chat_link WHERE chat_id = ?", Long.class, chatId);
    }

    public List<Long> findChatIdsByLinkId(Long linkId) {
        return jdbcTemplate.queryForList("SELECT chat_id FROM chat_link WHERE link_id = ?", Long.class, linkId);
    }

    public void deleteByChatId(Long chatId) {
        jdbcTemplate.update("DELETE FROM chat_link WHERE chat_id = ?", chatId);
    }

    public boolean existsByLinkId(Long linkId) {
        Integer count =
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM chat_link WHERE link_id = ?", Integer.class, linkId);
        return count != null && count > 0;
    }
}
