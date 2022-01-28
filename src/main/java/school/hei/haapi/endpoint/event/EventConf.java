package school.hei.haapi.endpoint.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class EventConf {
  private final Region region;
  public final static Integer MAX_EVENT_REQUEST_ENTRY = 10;

  public EventConf(@Value("${aws.region}") String region) {
    this.region = Region.of(region);
  }

  @Bean
  public SqsClient getSqsClient() {
    return SqsClient.builder().region(region).build();
  }
}