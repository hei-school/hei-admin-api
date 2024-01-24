package school.hei.haapi.mail;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import school.hei.haapi.PojaGenerated;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@PojaGenerated
@Configuration
public class EmailConf {

  @Getter private final String sesSource;
  private final Region region;

  @Getter private final String sesContact;

  public EmailConf(
      @Value("${aws.ses.source}") String sesSource,
      @Value("eu-west-3") Region region,
      @Value("${aws.ses.contact}") String sesContact) {
    this.sesSource = sesSource;
    this.region = region;
    this.sesContact = sesContact;
  }

  @Bean
  public SesClient getSesClient() {
    return SesClient.builder().region(region).build();
  }
}
