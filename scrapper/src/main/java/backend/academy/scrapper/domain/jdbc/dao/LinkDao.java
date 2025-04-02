package backend.academy.scrapper.domain.jdbc.dao;

import backend.academy.scrapper.domain.jdbc.dto.Link;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LinkDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public LinkDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public void save(Link link) {
        jdbcTemplate.update(
                "INSERT INTO link (url, filter, created_at, checked_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?) ON CONFLICT (url) DO NOTHING",
                link.url(),
                link.filter(),
                link.createdAt(),
                link.checkedAt(),
                link.updatedAt());
    }

    public Link findByUrl(String url) {
        return jdbcTemplate.queryForObject("SELECT * FROM link WHERE url = ?", this::mapRowToLink, url);
    }

    public Link findById(Long id) {
        return jdbcTemplate.queryForObject("SELECT * FROM link WHERE id = ?", this::mapRowToLink, id);
    }

    public void delete(Link link) {
        jdbcTemplate.update("DELETE FROM link WHERE id = ?", link.id());
    }

    public void update(Link link) {
        jdbcTemplate.update(
                "UPDATE link SET updated_at = ?, checked_at = ? WHERE id = ?",
                link.updatedAt(),
                link.checkedAt(),
                link.id());
    }

    public List<Link> findByChatId(Long chatId) {
        String sql = "SELECT l.* FROM link l JOIN chat_link cl ON l.id = cl.link_id WHERE cl.chat_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToLink, chatId);
    }

    private Link mapRowToLink(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
        return new Link(
                rs.getLong("id"),
                rs.getString("url"),
                rs.getString("filter"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("checked_at", OffsetDateTime.class),
                rs.getObject("updated_at", OffsetDateTime.class));
    }

    public List<Link> findBatch(int offset, int batchSize) {
        String sql = "SELECT * FROM link ORDER BY id LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, this::mapRowToLink, batchSize, offset);
    }

    public List<Link> findAllByChatIdAndTags(Long chatId, Set<String> tags) {
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }

        String sql = String.format("SELECT l.* FROM link l " + "JOIN chat_link cl ON l.id = cl.link_id "
                + "JOIN link_tag lt ON l.id = lt.link_id "
                + "JOIN tag t ON lt.tag_id = t.id "
                + "WHERE cl.chat_id = :chatId "
                + "AND lt.chat_id = :chatId "
                + "AND t.name IN (:tags) "
                + "GROUP BY l.id");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("chatId", chatId);
        params.addValue("tags", tags);

        return namedParameterJdbcTemplate.query(sql, params, this::mapRowToLink);
    }

    public void delete(Long linkId) {
        jdbcTemplate.update("DELETE FROM link WHERE id = ?", linkId);
    }
}
