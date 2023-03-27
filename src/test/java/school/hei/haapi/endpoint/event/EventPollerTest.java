package school.hei.haapi.endpoint.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import school.hei.haapi.endpoint.event.EventConsumer.AcknowledgeableTypedEvent;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventPollerTest {
  EventPoller eventPoller;
  SqsClient sqsClient;
  EventConsumer eventConsumer;

  @BeforeEach
  void setUp() {
    sqsClient = mock(SqsClient.class);
    eventConsumer = mock(EventConsumer.class);
    eventPoller = new EventPoller(
        "queueUrl",
        sqsClient,
        new ObjectMapper(),
        eventConsumer);
  }

  @Test
  void empty_messages_not_triggers_eventConsumer() {
    ReceiveMessageResponse response = ReceiveMessageResponse.builder()
        .messages(List.of())
        .build();
    when(sqsClient.receiveMessage((ReceiveMessageRequest) any())).thenReturn(response);

    eventPoller.poll();

    verify(eventConsumer, never()).accept(any());
  }

  @Test
  void non_empty_messages_triggers_eventConsumer() {
    ReceiveMessageResponse response = ReceiveMessageResponse.builder()
        .messages(
            someMessage(UserUpserted.class),
            someMessage(Exception.class),
            someMessage(UserUpserted.class))
        .build();
    when(sqsClient.receiveMessage((ReceiveMessageRequest) any())).thenReturn(response);

    eventPoller.poll();

    ArgumentCaptor<List<AcknowledgeableTypedEvent>> captor = ArgumentCaptor.forClass(List.class);
    verify(eventConsumer, times(1)).accept(captor.capture());
    var ackTypedEvents = captor.getValue();
    assertEquals(2, ackTypedEvents.size());
    // First ackTypedEvent
    var ackTypedEvent0 = ackTypedEvents.get(0);
    var typeEvent0 = ackTypedEvent0.getTypedEvent();
    assertEquals(UserUpserted.class.getTypeName(), typeEvent0.getTypeName());
    UserUpserted userUpserted0 = (UserUpserted) typeEvent0.getPayload();
    assertFalse(userUpserted0.getUserId().isEmpty());
    assertFalse(userUpserted0.getEmail().isEmpty());
    // Second ackTypedEvent
  }

  private Message someMessage(Class<?> clazz) {
    return Message.builder()
        .body(messageBody(clazz))
        .receiptHandle(randomUUID().toString())
        .build();
  }

  private String messageBody(Class<?> clazz) {
    String eventId = randomUUID().toString();
    String userId = randomUUID().toString();
    return "{\n"
        + "    \"version\": \"0\",\n"
        + "    \"id\": \" " + eventId + "\",\n"
        + "    \"detail-type\": \"" + clazz.getTypeName() + "\",\n"
        + "    \"source\": \"school.hei.haapi\",\n"
        + "    \"account\": \"088312068315\",\n"
        + "    \"time\": \"" + now() + "\",\n"
        + "    \"region\": \"eu-west-3\",\n"
        + "    \"resources\": [],\n"
        + "    \"detail\": {\n"
        + "        \"userId\": \"" + userId + "\",\n"
        + "        \"email\": \"test+" + userId + "@hei.school\"\n"
        + "    }\n"
        + "}";
  }
}