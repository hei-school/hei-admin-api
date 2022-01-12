package school.hei.haapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HaApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(HaApiApplication.class, args);
  }
}
