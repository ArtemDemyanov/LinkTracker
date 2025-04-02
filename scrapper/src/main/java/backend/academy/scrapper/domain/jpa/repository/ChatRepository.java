package backend.academy.scrapper.domain.jpa.repository;

import backend.academy.scrapper.domain.jpa.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {}
