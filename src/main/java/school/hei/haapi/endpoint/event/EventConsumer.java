package school.hei.haapi.endpoint.event;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.event.model.TypedEvent;

import static java.util.concurrent.Executors.newFixedThreadPool;

@Component
@Slf4j
public class EventConsumer implements Consumer<List<EventConsumer.AcknowledgeableTypedEvent>> {

  @AllArgsConstructor
  public static class AcknowledgeableTypedEvent {
    @Getter
    private final TypedEvent typedEvent;
    private final Runnable acknowledger;

    public void ack() {
      acknowledger.run();
    }
  }

  private final Executor executor;
  private static final int MAX_THREADS = 10;

  private final EventServiceInvoker eventServiceInvoker;

  public EventConsumer(EventServiceInvoker eventServiceInvoker) {
    this.executor = newFixedThreadPool(MAX_THREADS);
    this.eventServiceInvoker = eventServiceInvoker;
  }

  @Override
  public void accept(List<AcknowledgeableTypedEvent> ackTypedEvents) {
    for (AcknowledgeableTypedEvent ackTypedEvent : ackTypedEvents) {
      executor.execute(() -> {
        eventServiceInvoker.accept(ackTypedEvent.getTypedEvent());
        ackTypedEvent.ack();
      });
    }
  }
}
