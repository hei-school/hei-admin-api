package school.hei.haapi.handler.exceptionHandler;

import school.hei.haapi.PojaGenerated;

@PojaGenerated
@SuppressWarnings("all")
public interface ExceptionHandler<R> {
  R handle(Throwable throwable);
}
