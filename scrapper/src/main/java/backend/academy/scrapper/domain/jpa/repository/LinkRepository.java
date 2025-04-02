package backend.academy.scrapper.domain.jpa.repository;

import backend.academy.scrapper.domain.jpa.entity.Link;
import io.lettuce.core.dynamic.annotation.Param;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    Optional<Link> findByUrl(URI url);

    @Query("SELECT l FROM Link l JOIN l.chats c WHERE c.id = :chatId")
    Set<Link> findAllByChatId(@Param("chatId") Long chatId);

    @Override
    Page<Link> findAll(Pageable pageable);
}
