package school.hei.haapi.conf;

import org.springframework.test.context.DynamicPropertyRegistry;

public class EventConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("aws.region", () -> "dummy-region");
    registry.add("aws.sqs.queue.url", () -> "dummy-queue-url");
    registry.add("aws.eventBridge.bus", () -> "dummy-bus-url");
    registry.add("aws.bucket.name", () -> "dummy");
  }
}
