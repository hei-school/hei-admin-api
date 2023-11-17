package school.hei.haapi.endpoint.event;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.email.SesConf;
import school.hei.haapi.endpoint.event.gen.LateFeeVerified;
import school.hei.haapi.model.User;
import school.hei.haapi.service.aws.SesService;
import school.hei.haapi.service.event.LateFeeVerifiedService;
import school.hei.haapi.service.event.UserUpsertedService;

class EventServiceInvokerTest {
  UserUpsertedService userUpsertedService;
  LateFeeVerifiedService lateFeeService;
  SesService sesService;
  SesConf sesConf;

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
    sesService = mock(SesService.class);
    sesConf = mock(SesConf.class);
    lateFeeService = new LateFeeVerifiedService(sesService, sesConf);
  }

  @Test
  void lateFeeService_invokes_corresponding_service() {
    lateFeeService.accept(lateFee());

    verify(sesService, times(1)).sendEmail(any(), any(), any(), any(), any());
  }
}
