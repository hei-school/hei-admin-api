package school.hei.haapi.endpoint.event.model;

import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class SmsCampaignDispatchRequestedTest {
  ObjectMapper om = new ObjectMapper();

  @Test
  void sms_campaign_dispatch_requested() throws JsonProcessingException {
    var event = new SmsCampaignDispatchRequested("campaign1");

    var s = om.writeValueAsString(event);
    var dese = om.readValue(s, SmsCampaignDispatchRequested.class);

    assertEquals(event, dese);
    assertEquals("campaign1", event.getCampaignId());
    assertEquals(ofMinutes(5), event.maxConsumerDuration());
    assertEquals(ofMinutes(1), event.maxConsumerBackoffBetweenRetries());
  }
}
