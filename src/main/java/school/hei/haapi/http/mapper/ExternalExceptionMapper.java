package school.hei.haapi.http.mapper;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.net.http.HttpResponse;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.model.exception.NotFoundException;

@Component
public class ExternalExceptionMapper implements Consumer<HttpResponse<String>> {
  @Override
  public void accept(HttpResponse<String> httpResponse) {
    int httpStatusCode = httpResponse.statusCode();
    // TODO, client sideexception from external apis are serverside exception for us
    if (httpStatusCode == 400) {
      throw new BadRequestException(httpResponse.body().toString());
    }
    if (httpStatusCode == 404) {
      throw new NotFoundException("Resources not found");
    }
    if (httpStatusCode == 500) {
      throw new ApiException(SERVER_EXCEPTION, "Server side error");
    }
    if (httpStatusCode == 403) {
      throw new ForbiddenException("Connexion refused please log in and try again");
    }
    if (httpStatusCode == 401) {
      throw new ForbiddenException("Unauthorized");
    }
  }
}
