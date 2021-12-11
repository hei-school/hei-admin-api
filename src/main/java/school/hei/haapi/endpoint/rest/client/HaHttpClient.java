package school.hei.haapi.endpoint.rest.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import school.hei.haapi.endpoint.model.StudentResource;
import school.hei.haapi.endpoint.model.TeacherResource;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.ApiException.ExceptionType;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.ForbiddenException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.exception.NotImplementedException;
import school.hei.haapi.model.exception.TooManyRequestsException;

public class HaHttpClient {

  private static final String AUTHORIZATION = "Authorization";
  private static final String BEARER = "Bearer ";
  private static final String CONTENT_TYPE = "Content-Type";
  private static final String CONTENT_TYPE_JSON_UTF8 = "application/json; charset=utf-8";
  private static final Duration HTTP_REQUEST_TIMEOUT = Duration.ofMinutes(2);
  private final String baseUrl;
  private final HttpClient httpClient;
  private final HttpRequest.Builder requestBuilder;
  private final ObjectMapper om;

  public HaHttpClient(String baseUrl, String authorization) {
    this(HttpClient.newBuilder().build(), baseUrl, authorization);
  }

  public HaHttpClient(HttpClient httpClient, String baseUrl, String authorization) {
    this.baseUrl = baseUrl.charAt(baseUrl.length() - 1) == '/' ? baseUrl : (baseUrl + '/');
    this.httpClient = httpClient;
    this.requestBuilder =
        HttpRequest.newBuilder()
            .header(AUTHORIZATION, BEARER + authorization)
            .timeout(HTTP_REQUEST_TIMEOUT);
    this.om = new ObjectMapper();
  }

  public StudentResource getStudentById(String userId) {
    return get("students/" + userId, StudentResource.class);
  }

  public TeacherResource getTeacherById(String userId) {
    return get("teachers/" + userId, TeacherResource.class);
  }

  /*
   * HTTP UTILS
   */

  @SneakyThrows
  private <R> R get(String resourceUri, Class<R> clazz) {
    HttpResponse<String> httpResponse = get(resourceUri);
    return parseHttpResponse(httpResponse, clazz);
  }

  @SneakyThrows
  private HttpResponse<String> get(String resourceUri) {
    return httpClient.send(
        newRequestBuilder().uri(URI.create(baseUrl + resourceUri)).GET().build(),
        HttpResponse.BodyHandlers.ofString());
  }

  @SneakyThrows
  private <R> List<R> getList(String resourceUri, Class<R> clazz) {
    HttpResponse<String> httpResponse = get(resourceUri);
    return parseHttpListResponse(httpResponse, clazz);
  }

  @SneakyThrows
  private <Q, R> R post(String resourceUri, Q body, Class<R> clazz) {
    HttpResponse<String> httpResponse = genericCall(resourceUri, body, "POST");
    return parseHttpResponse(httpResponse, clazz);
  }

  @SneakyThrows
  private <Q, R> List<R> postList(String resourceUri, List<Q> body, Class<R> clazz) {
    HttpResponse<String> httpResponse = genericCall(resourceUri, body, "POST");
    return parseHttpListResponse(httpResponse, clazz);
  }

  @SneakyThrows
  private <Q, R> R patch(String resourceUri, Q body, Class<R> clazz) {
    HttpResponse<String> httpResponse = genericCall(resourceUri, body, "PATCH");
    return parseHttpResponse(httpResponse, clazz);
  }

  @SneakyThrows
  private <Q> HttpResponse<String> genericCall(String resourceUri, Q body, String method) {
    return httpClient.send(
        newRequestBuilder()
            .uri(URI.create(baseUrl + resourceUri))
            .header(CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8)
            .method(method, HttpRequest.BodyPublishers.ofString(om.writeValueAsString(body)))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }

  @SneakyThrows
  private <T> T parseHttpResponse(HttpResponse<String> httpResponse, Class<T> clazz) {
    checkError(httpResponse);
    return om.readValue(httpResponse.body(), clazz);
  }

  @SneakyThrows
  private <R> List<R> parseHttpListResponse(HttpResponse<String> httpResponse, Class<R> clazz) {
    checkError(httpResponse);
    CollectionType listType =
        // TypeReference does not work
        om.getTypeFactory().constructCollectionType(List.class, clazz);
    return om.readValue(httpResponse.body(), listType);
  }

  private void checkError(HttpResponse<String> httpResponse) {
    HttpStatus status = HttpStatus.valueOf(httpResponse.statusCode());
    String body = httpResponse.body();
    String message = String.format("Error response: code=%s, body=%s", status, body);
    if (status.value() == HttpStatus.BAD_REQUEST.value()) {
      throw new BadRequestException(message);
    } else if (status.value() == HttpStatus.FORBIDDEN.value()) {
      throw new ForbiddenException(message);
    } else if (status.value() == HttpStatus.NOT_FOUND.value()) {
      throw new NotFoundException(message);
    } else if (status.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
      throw new TooManyRequestsException(message);
    } else {
      if (status.is4xxClientError()) {
        throw new ApiException(message, ExceptionType.CLIENT_EXCEPTION);
      }
    }
    if (status.value() == HttpStatus.NOT_IMPLEMENTED.value()) {
      throw new NotImplementedException(message);
    } else {
      if (status.is5xxServerError()) {
        throw new ApiException(message, ExceptionType.SERVER_EXCEPTION);
      }
    }
  }

  private HttpRequest.Builder newRequestBuilder() {
    return requestBuilder.copy();
  }
}
