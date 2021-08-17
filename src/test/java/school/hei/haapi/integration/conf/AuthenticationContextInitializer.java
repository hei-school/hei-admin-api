package school.hei.haapi.integration.conf;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;

public class AuthenticationContextInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final PostgreSQLContainer<? extends PostgreSQLContainer<?>> postgreSQLContainer;
  private static final String FLYWAY_TESTDATA_PATH = "classpath:/db/testdata";

  static {
    postgreSQLContainer =
        new PostgreSQLContainer<>("postgres:13.2")
            .withDatabaseName("it-db")
            .withUsername("sa")
            .withPassword("sa");
    postgreSQLContainer.start();
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
        applicationContext,
        "server.port=8081",
        "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
        "spring.datasource.username=" + postgreSQLContainer.getUsername(),
        "spring.datasource.password=" + postgreSQLContainer.getPassword(),
        "spring.flyway.locations=classpath:/db/migration," + FLYWAY_TESTDATA_PATH);
  }
}
