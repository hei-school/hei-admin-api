package school.hei.haapi.concurrency;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static school.hei.haapi.concurrency.ThreadRenamer.getRandomSubThreadNamePrefixFrom;
import static school.hei.haapi.concurrency.ThreadRenamer.renameThread;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@Component
public class Workers<T> {
  private final ExecutorService executorService;

  public Workers() {
    this.executorService = newVirtualThreadPerTaskExecutor();
  }

  @SneakyThrows
  public List<Future<T>> invokeAll(List<Callable<T>> callables) {
    var parentThread = currentThread();
    callables =
        callables.stream()
            .map(
                c ->
                    (Callable<T>)
                        () -> {
                          renameThread(
                              currentThread(), getRandomSubThreadNamePrefixFrom(parentThread));
                          return c.call();
                        })
            .toList();
    return executorService.invokeAll(callables);
  }
}
