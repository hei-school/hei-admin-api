package school.hei.haapi.conf;

import org.springframework.test.context.DynamicPropertyRegistry;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
public class EmailConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("aws.ses.source", () -> "dummy-ses-source");
  }
}
