package school.hei.haapi.integration.conf;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class AbstractContextInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:13.2")
                    .withDatabaseName("it-db")
                    .withUsername("sa")
                    .withPassword("sa");
    postgresContainer.start();

    String flywayTestdataPath = "classpath:/db/testdata";
    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext,
            "server.port=" + this.getServerPort(),
            "cognito.userPool.url=https://dummy.com",
            "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
            "spring.datasource.username=" + postgresContainer.getUsername(),
            "spring.datasource.password=" + postgresContainer.getPassword(),
            "spring.flyway.locations=classpath:/db/migration," + flywayTestdataPath);
  }

  public abstract int getServerPort();
}
