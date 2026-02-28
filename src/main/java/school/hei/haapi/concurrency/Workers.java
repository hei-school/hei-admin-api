package school.hei.haapi.concurrency;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static school.hei.haapi.concurrency.ThreadRenamer.getRandomSubThreadNamePrefixFrom;
import static school.hei.haapi.concurrency.ThreadRenamer.renameThread;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@SuppressWarnings("all")
@Component
public class Workers {
  private final ExecutorService executorService;

  public Workers() {
    this.executorService = newVirtualThreadPerTaskExecutor();
  }

  @SneakyThrows
  public List<Void> invokeAll(List<Callable<Void>> callables) {
    var parentThread = currentThread();
    callables =
        callables.stream()
            .map(
                c ->
                    (Callable<Void>)
                        () -> {
                          renameThread(
                              parentThread, getRandomSubThreadNamePrefixFrom(parentThread));
                          return c.call();
                        })
            .toList();
    List<Future<Void>> futures = executorService.invokeAll(callables);
    return futures.stream().map(this::handleFutureException).toList();
  }

  private Void handleFutureException(Future<Void> future) {
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
