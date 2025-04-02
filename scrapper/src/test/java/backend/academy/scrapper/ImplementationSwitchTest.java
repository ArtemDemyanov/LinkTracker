package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.ORMLinkService;
import backend.academy.scrapper.service.SQLLinkService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

public class ImplementationSwitchTest {

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {"app.app.access-type=ORM", "spring.jpa.hibernate.ddl-auto=validate"})
    class OrmImplementationTest extends AbstractIntegrationTest {

        @Autowired
        private LinkService linkService;

        @Test
        void shouldUseOrmImplementation() {
            assertTrue(linkService instanceof ORMLinkService);
        }
    }

    @Nested
    @SpringBootTest
    @TestPropertySource(properties = {"app.app.access-type=SQL", "spring.jpa.hibernate.ddl-auto=none"})
    class SqlImplementationTest extends AbstractIntegrationTest {

        @Autowired
        private LinkService linkService;

        @Test
        void shouldUseSqlImplementation() {
            assertTrue(linkService instanceof SQLLinkService);
        }
    }
}
