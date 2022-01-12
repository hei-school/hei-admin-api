package school.hei.haapi.endpoint.event;

import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.event.EventConsumer.AcknowledgeableEvent;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import static java.util.stream.Collectors.toUnmodifiableList;

@Component
@Slf4j
public class EventPoller {

  private final String queueUrl;
  private final SqsClient sqsClient;
  private final EventConsumer eventConsumer;

  private static final Duration WAIT_TIME = Duration.ofSeconds(20); // MUST be <= 20s

  public EventPoller(
      @Value("${aws.sqs.queueUrl}") String queueUrl,
      SqsClient sqsClient,
      EventConsumer eventConsumer) {
    this.queueUrl = queueUrl;
    this.sqsClient = sqsClient;
    this.eventConsumer = eventConsumer;
  }

  @Scheduled(cron = "0 * * * * *")
  public void poll() {
    ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
        .queueUrl(queueUrl)
        .waitTimeSeconds(WAIT_TIME.toSecondsPart())
        .build();

    List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();
    if (!messages.isEmpty()) {
      log.info("Events received: {}", messages); //TODO(PII)
    }
    eventConsumer.accept(messages.stream()
        .map(this::toAcknowledgeableTypedEvent)
        .collect(toUnmodifiableList()));
  }

  private AcknowledgeableEvent toAcknowledgeableTypedEvent(Message message) {
    return new AcknowledgeableEvent(
        message.body(), //TODO: map to TypedEvent
        message.receiptHandle());
  }
}
