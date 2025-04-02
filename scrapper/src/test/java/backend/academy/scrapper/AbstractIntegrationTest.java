package backend.academy.scrapper;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public abstract class AbstractIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("scrapper_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/master.xml");
    }

    @BeforeAll
    static void beforeAll() throws Exception {
        applyMigrations();
    }

    private static void applyMigrations() throws Exception {
        Connection connection =
                DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

        Database database =
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

        // Navigate up from scrapper module to project root
        File scrapperDir = new File(System.getProperty("user.dir"));
        File projectRoot = scrapperDir.getParentFile();
        File migrationsDir = new File(projectRoot, "migrations");

        System.out.println("Project root: " + projectRoot.getAbsolutePath());
        System.out.println("Migrations directory: " + migrationsDir.getAbsolutePath());

        if (!migrationsDir.exists()) {
            throw new IllegalStateException(
                    "Migrations directory not found. Expected at: " + migrationsDir.getAbsolutePath()
                            + "\nProject root: "
                            + projectRoot.getAbsolutePath() + "\nCurrent working dir: "
                            + scrapperDir.getAbsolutePath());
        }

        Liquibase liquibase = new Liquibase("master.xml", new FileSystemResourceAccessor(migrationsDir), database);

        liquibase.update("");
        connection.close();
    }
}
