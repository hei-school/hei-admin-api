package school.hei.haapi.concurrency;

import static java.util.concurrent.Executors.newFixedThreadPool;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@Component
public class Workers<T> {
  private final ExecutorService executorService;

  public Workers(@Value("${workers.thread.number:5}") int nbThreads) {
    this.executorService = newFixedThreadPool(nbThreads);
  }

  @SneakyThrows
  public List<Future<T>> invokeAll(List<Callable<T>> callables) {
    return executorService.invokeAll(callables);
  }
}
