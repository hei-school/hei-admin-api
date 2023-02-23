package school.hei.haapi.endpoint.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class EventConf {
  private final Region region;
  private final String sesSource;

  public EventConf(@Value("${aws.region}") String region,
                   @Value("aws.ses.source") String sesSource) {
    this.region = Region.of(region);
    this.sesSource = sesSource;
  }

  @Bean
  public SqsClient getSqsClient() {
    return SqsClient.builder()
        .region(region)
        .build();
  }

  @Bean
  public SesClient getSesClient() {
    return SesClient.builder()
        .region(region)
        .build();
  }

  public String getSesSource() {
    return this.sesSource;
  }

}