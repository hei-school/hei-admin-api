package school.hei.haapi.endpoint.event;

import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.exception.NotImplementedException;
import software.amazon.awssdk.services.sqs.SqsClient;

@Component
@AllArgsConstructor
@Slf4j
public class EventConsumer implements Consumer<List<EventConsumer.AcknowledgeableEvent>> {

  private final SqsClient sqsClient;

  @Value
  public static class AcknowledgeableEvent {
    String eventBody;
    String receiptHandle;
  }

  @Override
  public void accept(List<AcknowledgeableEvent> events) {
    log.info("Consume: {}", events);
    throw new NotImplementedException("Map to event.payload, consume, and ack if ok");
  }
}
