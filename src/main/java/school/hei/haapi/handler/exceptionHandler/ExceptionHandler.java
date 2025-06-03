package school.hei.haapi.handler.exceptionHandler;

import school.hei.haapi.PojaGenerated;

@PojaGenerated
public interface ExceptionHandler<R> {
  R handle(Throwable throwable);
}
