package school.hei.haapi.endpoint.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SesConf {
  private final String sesSource;
  private final String sesContact;

  public SesConf(
      @Value("${aws.ses.source}") String sesSource,
      @Value("${aws.ses.contact}") String sesContact) {
    this.sesSource = sesSource;
    this.sesContact = sesContact;
  }

  public String getSesSource() {
    return this.sesSource;
  }

  public String getSesContact() {
    return this.sesContact;
  }
}
