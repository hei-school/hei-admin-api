package school.hei.haapi.endpoint.event;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.conf.BucketConf;
import school.hei.haapi.endpoint.event.gen.LateFeeVerified;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
import school.hei.haapi.service.event.LateFeeVerifiedService;
import school.hei.haapi.service.event.UserUpsertedService;

class EventServiceInvokerTest {
  UserUpsertedService userUpsertedService;
  LateFeeVerifiedService lateFeeService;
  Mailer sesService;
  @MockBean BucketConf bucketConf;

  static User randomStudent() {
    return User.builder()
        .lastName("user")
        .firstName("random")
        .email("test+" + randomUUID() + "@hei.school")
        .build();
  }

  static LateFeeVerified lateFee() {
    return LateFeeVerified.builder()
        .student(randomStudent())
        .remainingAmount(25_000)
        .dueDatetime(Instant.parse("2023-02-08T08:30:24Z"))
        .build();
  }

  @BeforeEach
  void setUp() {
    userUpsertedService = mock(UserUpsertedService.class);
    sesService = mock(Mailer.class);
    lateFeeService = new LateFeeVerifiedService(sesService);
  }

  @Test
  void lateFeeService_invokes_corresponding_service() {
    lateFeeService.accept(lateFee());

    verify(sesService, times(1)).accept(any());
  }
}
