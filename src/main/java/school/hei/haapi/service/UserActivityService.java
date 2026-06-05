package school.hei.haapi.service;

import java.time.Instant;
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
            .userId(userId)
            .userEmail(userEmail)
            .endpoint(endpoint)
            .httpMethod(httpMethod)
            .requestBody(requestBody)
            .createdAt(Instant.now())
            .build());
  }
}
