package school.hei.haapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.OK;

import school.hei.haapi.conf.FacadeIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

class PingIT extends FacadeIT {
  @Autowired private TestRestTemplate restTemplate;

  @Test
  void ping_ok() {
    var response = restTemplate.getForEntity("/ping", String.class);

    assertEquals(OK, response.getStatusCode());
    assertEquals("pong", response.getBody());
  }
}