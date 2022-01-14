package school.hei.haapi.endpoint.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.model.TypedUserUpserted;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;
import school.hei.haapi.service.UserUpsertedService;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EventServiceInvokerTest {
  EventServiceInvoker eventServiceInvoker;
  UserUpsertedService userUpsertedService;

  @BeforeEach
  void setUp() {
    userUpsertedService = mock(UserUpsertedService.class);
    eventServiceInvoker = new EventServiceInvoker(userUpsertedService);
  }

  @Test
  void userUpserted_invokes_corresponding_service() {
    String email = "test+" + randomUUID() + "@hei.school";
    TypedUserUpserted userUpserted = new TypedUserUpserted(new UserUpserted().email(email));

    eventServiceInvoker.accept(userUpserted);

    verify(userUpsertedService, times(1)).accept((UserUpserted) userUpserted.getPayload());
  }
}