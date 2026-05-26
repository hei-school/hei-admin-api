package school.hei.haapi.conf;

import org.springframework.test.context.DynamicPropertyRegistry;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@SuppressWarnings("all")
public class BucketConf {

  void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("aws.s3.bucket", () -> "dummy-bucket");
  }
}
