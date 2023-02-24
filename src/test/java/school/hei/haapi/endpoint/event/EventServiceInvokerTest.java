package school.hei.haapi.endpoint.event;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.TypedUserUpserted;
import school.hei.haapi.endpoint.event.model.gen.LateFeeChecked;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;
import school.hei.haapi.model.User;
import school.hei.haapi.service.LateFeeCheckedService;
import school.hei.haapi.service.UserUpsertedService;
import school.hei.haapi.service.aws.SesService;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EventServiceInvokerTest {
  EventServiceInvoker eventServiceInvoker;
  UserUpsertedService userUpsertedService;

  LateFeeCheckedService lateFeeCheckedService;
  SesService sesService;
  EventConf eventConf;

  @BeforeEach
  void setUp() {
    userUpsertedService = mock(UserUpsertedService.class);
    sesService = mock(SesService.class);
    eventConf = mock(EventConf.class);
    lateFeeCheckedService = new LateFeeCheckedService(sesService, eventConf);
    eventServiceInvoker = new EventServiceInvoker(userUpsertedService, lateFeeCheckedService);
  }

  @Test
  void userUpserted_invokes_corresponding_service() {
    String email = "test+" + randomUUID() + "@hei.school";
    TypedUserUpserted userUpserted = new TypedUserUpserted(new UserUpserted().email(email));

    eventServiceInvoker.accept(userUpserted);

    verify(userUpsertedService, times(1)).accept((UserUpserted) userUpserted.getPayload());
  }

  @Test
  void lateFeeChecked_invokes_corresponding_service() {
    LateFeeChecked lateFee = LateFeeChecked.builder()
        .student(
            User.builder()
                .email("test+" + randomUUID() + "@hei.school")
                .build()
        )
        .dueDatetime(Instant.parse("2023-02-08T08:30:24Z"))
        .build();

    lateFeeCheckedService.accept(lateFee);

    verify(sesService, times(1)).sendEmail(any(), any(), any(), any());
  }
}