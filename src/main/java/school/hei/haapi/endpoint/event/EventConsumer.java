package school.hei.haapi.endpoint.event;

import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.event.model.TypedEvent;
import school.hei.haapi.model.exception.NotImplementedException;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@AllArgsConstructor
@Slf4j
public class EventConsumer implements Consumer<List<EventConsumer.AcknowledgeableTypedEvent>> {

  private final SqsClient sqsClient;

  @Value
  public static class AcknowledgeableTypedEvent {
    TypedEvent typedEvent;
    String receiptHandle;
  }

  @Override
  public void accept(List<AcknowledgeableTypedEvent> acknowledgeableTypedEvents) {
    log.info("Consume: {}", acknowledgeableTypedEvents);
    throw new NotImplementedException("Map to event.payload, consume, and ack if ok");
  }
}
