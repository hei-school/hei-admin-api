package school.hei.haapi.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.Event;
import school.hei.haapi.model.exception.ApiException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

import static java.util.stream.Collectors.toUnmodifiableList;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

@Service
@Slf4j
public class EventProducer {

  @Configuration
  private static class Conf { //NOSONAR
    private final Region region;

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
   * Sent events to EventBridge bus
   * @param events Events to publish to the configured event bus
   * @return Identifier of the events that were successfully sent
   */
  public void produce(List<Event> events) {
    log.info(
        // TODO(PII): Personal Identifiable Information can be leaked here
        "Sending events={}", events);
    PutEventsResponse response = sendRequest(events);
    checkResponse(response);
  }

  private PutEventsResponse sendRequest(List<Event> events) {
    PutEventsRequest eventsRequest = PutEventsRequest.builder()
        .entries(events.stream().map(this::toRequestEntry).collect(toUnmodifiableList()))
        .build();
    return eventBridgeClient.putEvents(eventsRequest);
  }

  private PutEventsRequestEntry toRequestEntry(Event event) {
    String eventType = event.getClass().getTypeName();
    try {
      String eventAsString = om.writeValueAsString(event);
      return PutEventsRequestEntry.builder()
          .source(EVENT_SOURCE)
          .detailType(eventType)
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
}
