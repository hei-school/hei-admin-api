package school.hei.haapi.endpoint.event;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.event.model.TypedEvent;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;

import static java.util.concurrent.Executors.newFixedThreadPool;

@Component
@Slf4j
public class EventConsumer implements Consumer<List<EventConsumer.AcknowledgeableTypedEvent>> {

  private final Executor executor;
  private static final int MAX_THREADS = 10;

  // For acknowledging
  private final String queueUrl;
  private final SqsClient sqsClient;

  private final EventServiceInvoker eventServiceInvoker;

  public EventConsumer(
      @Value("${aws.sqs.queueUrl}") String queueUrl,
      SqsClient sqsClient,
      EventServiceInvoker eventServiceInvoker) {
    this.executor = newFixedThreadPool(MAX_THREADS);
    this.queueUrl = queueUrl;
    this.sqsClient = sqsClient;
    this.eventServiceInvoker = eventServiceInvoker;
  }

  @lombok.Value
  public static class AcknowledgeableTypedEvent {
    TypedEvent typedEvent;
    String receiptHandle;
  }

  @Override
  public void accept(List<AcknowledgeableTypedEvent> ackTypedEvents) {
    for (AcknowledgeableTypedEvent ackTypedEvent : ackTypedEvents) {
      executor.execute(() -> {
        eventServiceInvoker.accept(ackTypedEvent.getTypedEvent());
        sqsClient.deleteMessage(DeleteMessageRequest.builder() // ack
            .queueUrl(queueUrl)
            .receiptHandle(ackTypedEvent.getReceiptHandle())
            .build());
      });
    }
  }
}
