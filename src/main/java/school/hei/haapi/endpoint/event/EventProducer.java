package school.hei.haapi.endpoint.event;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.event.model.TypedEvent;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.BadRequestException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Component
@Slf4j
public class EventProducer implements Consumer<List<TypedEvent>> {

  @Configuration
  public static class Conf {

    private final Region region;
    private static final int MAX_PUT_EVENT_ENTRIES = 10;

    public Conf(@Value("${aws.region}") String region) {
      this.region = Region.of(region);
    }

    @Bean
    public EventBridgeClient getEventBridgeClient() {
      return EventBridgeClient.builder().region(region).build();
    }
  }

  private final EventBridgeClient eventBridgeClient;
  private final String eventBusName;
  private final ObjectMapper om;
  private static final String EVENT_SOURCE = "school.hei.haapi";

  public EventProducer(
      EventBridgeClient eventBridgeClient,
      @Value("${aws.eventBridge.bus}") String eventBusName,
      ObjectMapper om) {
    this.eventBridgeClient = eventBridgeClient;
    this.eventBusName = eventBusName;
    this.om = om;
  }

  /**
   * Send events to EventBridge bus.
   *
   * @param events Events to publish to the configured event bus.
   */
  @Override
  public void accept(List<TypedEvent> events) {
    log.info(
        // TODO(PII): Personal Identifiable Information can be leaked here
        "Sending events={}", events);
    PutEventsResponse response = sendRequest(events);
    checkResponse(response);
  }

  private PutEventsResponse sendRequest(List<TypedEvent> events) {
    checkPayload(events);
    PutEventsRequest eventsRequest = toEventsRequest(events);
    return eventBridgeClient.putEvents(eventsRequest);
  }

  private PutEventsRequest toEventsRequest(List<TypedEvent> events) {
    return PutEventsRequest.builder()
            .entries(events.stream().map(this::toRequestEntry).collect(toUnmodifiableList()))
            .build();
  }

  private PutEventsRequestEntry toRequestEntry(TypedEvent typedEvent) {
    try {
      String eventAsString = om.writeValueAsString(typedEvent.getPayload());
      return PutEventsRequestEntry.builder()
          .source(EVENT_SOURCE)
          .detailType(typedEvent.getTypeName())
          .detail(eventAsString)
          .eventBusName(eventBusName)
          .build();
    } catch (JsonProcessingException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  private void checkResponse(PutEventsResponse response) {
    List<PutEventsResultEntry> failedEntries = new ArrayList<>();
    List<PutEventsResultEntry> successfulEntries = new ArrayList<>();

    for (PutEventsResultEntry resultEntry : response.entries()) {
      if (resultEntry.eventId() == null) {
        failedEntries.add(resultEntry);
      }
      successfulEntries.add(resultEntry);
    }

    if (!failedEntries.isEmpty()) {
      log.error("Following events were not successfully sent: {}", failedEntries);
    }
    if (!successfulEntries.isEmpty()) {
      log.info("Following events were successfully sent: {}", successfulEntries);
    }
  }

  private boolean isPayloadValid(List<TypedEvent> events) {
    PutEventsRequest eventsRequest = toEventsRequest(events);
    return eventsRequest.entries().size() <= Conf.MAX_PUT_EVENT_ENTRIES;
  }

  private void checkPayload(List<TypedEvent> events) {
    if (!isPayloadValid(events)) {
      throw new BadRequestException(
              "Request entries must be <= " + Conf.MAX_PUT_EVENT_ENTRIES);
    }
  }
}
