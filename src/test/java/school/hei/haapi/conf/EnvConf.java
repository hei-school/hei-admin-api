package school.hei.haapi.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EnvConf {
  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("env", () -> "test");
    registry.add("sentry.dsn", () -> "https://public@sentry.example.com/1");
    registry.add("aws.ses.source", () -> "dummy");
    registry.add("aws.ses.contact", () -> "dummy");
    registry.add("aws.cognito.userPool.id", () -> "dummy");
    registry.add("aws.s3.bucket", () -> "dummy");
  }
}
