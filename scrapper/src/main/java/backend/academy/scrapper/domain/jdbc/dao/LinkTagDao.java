package backend.academy.scrapper.domain.jdbc.dao;

import backend.academy.scrapper.domain.jdbc.dto.LinkTag;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LinkTagDao {

    private final JdbcTemplate jdbcTemplate;

    public LinkTagDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(LinkTag linkTag) {
        jdbcTemplate.update(
                "INSERT INTO link_tag (link_id, tag_id, chat_id) VALUES (?, ?, ?)",
                linkTag.linkId(),
                linkTag.tagId(),
                linkTag.chatId());
    }

    public Set<String> findTagsByLinkId(Long linkId) {
        String sql = "SELECT t.name FROM tag t JOIN link_tag lt ON t.id = lt.tag_id WHERE lt.link_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, String.class, linkId));
    }

    public List<LinkTag> findByLinkId(Long linkId) {
        return jdbcTemplate.query("SELECT * FROM link_tag WHERE link_id = ?", this::mapRowToLinkTag, linkId);
    }

    public void delete(LinkTag linkTag) {
        jdbcTemplate.update("DELETE FROM link_tag WHERE id = ?", linkTag.id());
    }

    private LinkTag mapRowToLinkTag(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
        return new LinkTag(rs.getLong("id"), rs.getLong("link_id"), rs.getLong("tag_id"), rs.getLong("chat_id"));
    }

    public void deleteByChatId(Long chatId) {
        jdbcTemplate.update("DELETE FROM link_tag WHERE chat_id = ?", chatId);
    }
}
