package school.hei.haapi.service;

import static org.springframework.http.HttpMethod.POST;
import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import school.hei.haapi.endpoint.OwnCloudConf;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.notEntity.OcsData;

@Service
@Slf4j
@AllArgsConstructor
public class OwnCloudService {

  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;
  private final OwnCloudConf conf;

  public OcsData createShareLink(String name, String path) {
    User currentUser = getPrincipal().getUser();
    Integer permission =
        switch (currentUser.getRole()) {
          case STUDENT, TEACHER -> 1;
          case MANAGER -> 31;
        };

    log.info(conf.getUsername(), conf.getPassword());
    HttpHeaders headers = new HttpHeaders();
    String auth = conf.getUsername() + ":" + conf.getPassword();
    String authHeader =
        "Basic " + new String(Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII)));
    headers.set("Authorization", authHeader);
    HttpEntity<Void> entity = new HttpEntity<>(null, headers);
    try {
      ResponseEntity<String> response =
          restTemplate.exchange(conf.getURI(path, name, permission), POST, entity, String.class);
      JsonNode node = objectMapper.readTree(response.getBody()).get("ocs").get("data");

      return objectMapper.convertValue(node, OcsData.class);
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }
}
