package school.hei.haapi.http.mapper;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.net.http.HttpResponse;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.exception.ApiException;

@Component
public class ExternalExceptionMapper implements Consumer<HttpResponse<String>> {
  @Override
  public void accept(HttpResponse<String> httpResponse) {
    int httpStatusCode = httpResponse.statusCode();
    if (httpStatusCode == 400) {
      throw new ApiException(SERVER_EXCEPTION, httpResponse.body().toString());
    }
    if (httpStatusCode == 404) {
      throw new ApiException(SERVER_EXCEPTION, "Resources not found");
    }
    if (httpStatusCode == 500) {
      throw new ApiException(SERVER_EXCEPTION, "Server side error");
    }
    if (httpStatusCode == 403) {
      throw new ApiException(SERVER_EXCEPTION, "Connexion refused please log in and try again");
    }
    if (httpStatusCode == 401) {
      throw new ApiException(SERVER_EXCEPTION, "Unauthorized");
    }
  }
}
