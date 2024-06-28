package school.hei.haapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.notEntity.OcsData;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Map;

import static org.springframework.http.HttpMethod.POST;
import static school.hei.haapi.endpoint.rest.security.AuthProvider.getPrincipal;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;
import static school.hei.haapi.service.utils.DataFormatterUtils.instantToOcsDateFormat;

@Service
@Slf4j
public class OwnCloudService {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final UriComponents baseUrl;
    private final String username;
    private final String password;

    public OwnCloudService(ObjectMapper objectMapper, RestTemplate restTemplate, @Value("${OWNCLOUD_BASE_URL}") String baseUrl, @Value("${OWNCLOUD_USERNAME}") String username, @Value("${OWNCLOUD_PASSWORD}") String password) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.baseUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/ocs/v1.php/apps/files_sharing/api/v1/shares")
                .queryParam("path={path}")
                .queryParam("name={name}")
                .queryParam("shareType=3")
                .queryParam("permissions={permissions}")
                .queryParam("expireDate={expireDate}")
                .queryParam("format=json")
                .queryParam("publicUpload=false")
                .build();
        this.password = password;
        this.username = username;
    }


    public URI getURI(String path, String name, Integer permissions) {
        Map<String, Object> uriVariables = Map.of(
                "path", path,
                "name", name,
                "permissions", permissions,
                "expireDate", instantToOcsDateFormat(Instant.now().plus(1, ChronoUnit.DAYS))
        );
        return baseUrl.expand(uriVariables).toUri();
    }

    public OcsData createShareLink(String name, String path){
        User currentUser = getPrincipal().getUser();
        Integer permission = switch (currentUser.getRole()){
            case STUDENT, TEACHER -> 1;
            case MANAGER -> 31;
        };
        log.info(username, password);
        HttpHeaders headers = new HttpHeaders();
        String auth = username + ":" + password;
        String authHeader = "Basic " + new String(Base64.getEncoder().encode(auth.getBytes(StandardCharsets.US_ASCII)));
        headers.set("Authorization", authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(null,headers);
            try {
                ResponseEntity<String> response = restTemplate.exchange(getURI(path, name, permission), POST, entity, String.class);
                JsonNode node = objectMapper.readTree(response.getBody()).get("ocs").get("data");

                return objectMapper.convertValue(node, OcsData.class);
            } catch (IOException e) {
                throw new ApiException(SERVER_EXCEPTION, e);
        }
    }
}
