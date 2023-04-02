package school.hei.haapi.endpoint.event;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.event.EventConsumer.AcknowledgeableTypedEvent;
import school.hei.haapi.endpoint.event.model.TypedUserUpserted;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class EventConsumerTest {
  EventConsumer eventConsumer;
  EventServiceInvoker eventServiceInvoker;

  static final Duration TIMEOUT = Duration.ofSeconds(3);

  @BeforeEach
  void setUp() {
    eventServiceInvoker = mock(EventServiceInvoker.class);
    eventConsumer = new EventConsumer(eventServiceInvoker);
  }

  @Test
  void event_is_ack_if_eventServiceInvoker_succeeded() {
    String email = "test+" + randomUUID() + "@hei.school";
    TypedUserUpserted userUpserted = new TypedUserUpserted(new UserUpserted().email(email));
    Runnable acknowledger = mock(Runnable.class);

    eventConsumer.accept(List.of(new AcknowledgeableTypedEvent(userUpserted, acknowledger)));

    verify(eventServiceInvoker, timeout(TIMEOUT.toMillis())).accept(userUpserted);
    verify(acknowledger, timeout(TIMEOUT.toMillis())).run();
  }

  @Test
  void event_is_not_ack_if_eventServiceInvoker_failed() {
    String email = "test+" + randomUUID() + "@hei.school";
    TypedUserUpserted userUpserted = new TypedUserUpserted(new UserUpserted().email(email));
    Runnable acknowledger = mock(Runnable.class);
    doThrow(RuntimeException.class).when(eventServiceInvoker).accept(userUpserted);

    eventConsumer.accept(List.of(new AcknowledgeableTypedEvent(userUpserted, acknowledger)));

    verify(eventServiceInvoker, timeout(TIMEOUT.toMillis())).accept(userUpserted);
    verify(acknowledger, timeout(TIMEOUT.toMillis()).times(0)).run();
  }
}