package school.hei.haapi.service.event;

import static java.lang.Thread.sleep;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.haapi.PojaGenerated;
import school.hei.haapi.endpoint.event.model.DurablyFallibleUuidCreated1;

@PojaGenerated
@Service
@AllArgsConstructor
@Slf4j
public class DurablyFallibleUuidCreated1Service implements Consumer<DurablyFallibleUuidCreated1> {
  private final UuidCreatedService uuidCreatedService;

  @SneakyThrows
  @Override
  public void accept(DurablyFallibleUuidCreated1 durablyFallibleUuidCreated1) {
    sleep(durablyFallibleUuidCreated1.getWaitDurationBeforeConsumingInSeconds() * 1_000L);
    if (durablyFallibleUuidCreated1.shouldFail()) {
      throw new RuntimeException("Oops, random fail!");
    }

    uuidCreatedService.accept(durablyFallibleUuidCreated1.getUuidCreated());
  }
}
