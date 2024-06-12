package school.hei.haapi.endpoint.event.consumer;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.PojaGenerated;
import school.hei.haapi.concurrency.Workers;
import school.hei.haapi.endpoint.event.consumer.model.ConsumableEvent;

@PojaGenerated
@Component
@Slf4j
public class EventConsumer implements Consumer<List<ConsumableEvent>> {
  private final Workers<Void> workers;
  private final EventServiceInvoker eventServiceInvoker;

  public EventConsumer(Workers<Void> workers, EventServiceInvoker eventServiceInvoker) {
    this.workers = workers;
    this.eventServiceInvoker = eventServiceInvoker;
  }

  @Override
  public void accept(List<ConsumableEvent> ackEvents) {
    workers.invokeAll(ackEvents.stream().map(this::toCallable).collect(toList()));
  }

  private Callable<Void> toCallable(ConsumableEvent ackEvent) {
    return () -> {
      eventServiceInvoker.accept(ackEvent.getEvent());
      ackEvent.ack();
      return null;
    };
  }
}
