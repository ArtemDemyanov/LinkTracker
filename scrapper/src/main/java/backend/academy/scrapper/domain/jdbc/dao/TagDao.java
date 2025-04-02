package backend.academy.scrapper.domain.jdbc.dao;

import backend.academy.scrapper.domain.jdbc.dto.Tag;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TagDao {

    private final JdbcTemplate jdbcTemplate;

    public TagDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(Tag tag) {
        jdbcTemplate.update("INSERT INTO tag (name) VALUES (?) ON CONFLICT DO NOTHING", tag.name());
    }

    public Tag findByName(String name) {
        return jdbcTemplate.queryForObject("SELECT * FROM tag WHERE name = ?", this::mapRowToTag, name);
    }

    private Tag mapRowToTag(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
        return new Tag(rs.getLong("id"), rs.getString("name"));
    }

    public Set<String> findAllTagsByChatId(Long chatId) {
        String sql =
                "SELECT DISTINCT t.name FROM tag t " + "JOIN link_tag lt ON t.id = lt.tag_id " + "WHERE lt.chat_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, String.class, chatId));
    }

    public Set<String> findTagsByLinkAndChat(Long linkId, Long chatId) {
        String sql = "SELECT t.name FROM tag t " + "JOIN link_tag lt ON t.id = lt.tag_id "
                + "WHERE lt.link_id = ? AND lt.chat_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, String.class, linkId, chatId));
    }
}
