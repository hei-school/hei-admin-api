package school.hei.haapi.endpoint.event;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.PojaGenerated;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.endpoint.event.gen.UuidCreated;
import school.hei.haapi.file.BucketConf;
import school.hei.haapi.repository.DummyUuidRepository;

@PojaGenerated
class EventConsumerIT extends FacadeIT {

  @Autowired EventConsumer subject;
  @Autowired DummyUuidRepository dummyUuidRepository;
  @Autowired ObjectMapper om;
  @MockBean BucketConf bucketConf;

  @Test
  void uuid_created_is_persisted() throws InterruptedException, JsonProcessingException {
    var uuid = randomUUID().toString();
    var uuidCreated = UuidCreated.builder().uuid(uuid).build();
    var payloadReceived = om.readValue(om.writeValueAsString(uuidCreated), UuidCreated.class);

    subject.accept(
        List.of(
            new EventConsumer.AcknowledgeableTypedEvent(
                new EventConsumer.TypedEvent(
                    "school.hei.haapi.endpoint.event.gen.UuidCreated", payloadReceived),
                () -> {})));

    Thread.sleep(2_000);
    var saved = dummyUuidRepository.findById(uuid).orElseThrow();
    assertEquals(uuid, saved.getId());
  }
}
