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
import java.util.function.Function;
import org.springframework.stereotype.Component;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@SuppressWarnings("all")
@Component
public class Workers<T> implements Function<List<Callable<T>>, List<T>> {
  private final ExecutorService executorService;

  public Workers() {
    this.executorService = newVirtualThreadPerTaskExecutor();
  }

  @Override
  public List<T> apply(List<Callable<T>> callables) {
    var parentThread = currentThread();
    callables =
        callables.stream()
            .map(
                c ->
                    (Callable<T>)
                        () -> {
                          renameThread(
                              parentThread, getRandomSubThreadNamePrefixFrom(parentThread));
                          return c.call();
                        })
            .toList();
    List<Future<T>> futures;
    try {
      futures = executorService.invokeAll(callables);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return futures.stream().map(this::handleFutureException).toList();
  }

  private T handleFutureException(Future<T> future) {
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
