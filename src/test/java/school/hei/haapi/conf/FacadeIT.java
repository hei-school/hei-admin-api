package school.hei.haapi.conf;

import static java.lang.Runtime.getRuntime;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Slf4j
public class FacadeIT {

  private static final PostgresConf POSTGRES_CONF = new PostgresConf();

  @BeforeAll
  static void beforeAll() {
    POSTGRES_CONF.start();
    getRuntime()
        // Do _not_ stop postgresTest in afterAll as it is shared between multiple subclasses of
        // FacadeTest.
        // Doing so might cause some subclasses to stop it while other ones are still using it!
        .addShutdownHook(new Thread(POSTGRES_CONF::stop));
  }

  @SneakyThrows
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    POSTGRES_CONF.configureProperties(registry);

    new EventConf().configureProperties(registry);
    new BucketConf().configureProperties(registry);
    new EmailConf().configureProperties(registry);

    registry.add("sentry.dsn", () -> "https://public@sentry.example.com/1");
    registry.add("sentry.environment", () -> "dummy");

    try {
      var envConfClazz = Class.forName("school.hei.haapi.conf.EnvConf");
      var envConfConfigureProperties =
          envConfClazz.getDeclaredMethod("configureProperties", DynamicPropertyRegistry.class);
      var envConf = envConfClazz.getConstructor().newInstance();
      envConfConfigureProperties.invoke(envConf, registry);
    } catch (ClassNotFoundException e) {
      log.warn("EnvConf missing: no project-specific test env vars will be set");
    }
  }
}
