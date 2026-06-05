package school.hei.haapi.service;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.UserActivity;
import school.hei.haapi.repository.UserActivityRepository;

@Service
@RequiredArgsConstructor
public class UserActivityService {
  private final UserActivityRepository repository;

  public void save(
      String userId, String userEmail, String endpoint, String httpMethod, String requestBody) {
    repository.save(
        UserActivity.builder()
            .id(randomUUID().toString())
            .userId(userId)
            .userEmail(userEmail)
            .endpoint(endpoint)
            .httpMethod(httpMethod)
            .requestBody(requestBody)
            .createdAt(now())
            .build());
  }
}
