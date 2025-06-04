package school.hei.haapi.handler;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;
import school.hei.haapi.PojaApplication;
import school.hei.haapi.PojaGenerated;
import school.hei.haapi.handler.exceptionHandler.ExceptionHandler;
import school.hei.haapi.handler.exceptionHandler.ExceptionHandlerImpl;
import school.hei.haapi.handler.model.ResponseEvent.LambdaUrlResponseEvent;
import school.hei.haapi.handler.model.requestEvent.LambdaUrlRequestEvent;
import school.hei.haapi.handler.model.wrapper.HttpServletRequestWrapper;
import school.hei.haapi.handler.model.wrapper.HttpServletResponseWrapper;

@PojaGenerated
@SuppressWarnings("all")
public class LambdaHandler
    implements RequestHandler<LambdaUrlRequestEvent, LambdaUrlResponseEvent> {

  @Getter
  private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  private static final String SERVER_PORT = "0";

  private final RequestMappingHandlerAdapter handlerAdapter;
  private final RequestMappingHandlerMapping handlerMapping;
  private final ExceptionHandler<LambdaUrlResponseEvent> exceptionHandler;

  public LambdaHandler() {
    ConfigurableApplicationContext context = applicationContext();
    this.handlerAdapter = context.getBean(RequestMappingHandlerAdapter.class);
    this.handlerMapping = context.getBean(RequestMappingHandlerMapping.class);
    this.exceptionHandler = defaultExceptionHandler();
  }

  @Override
  public LambdaUrlResponseEvent handleRequest(LambdaUrlRequestEvent event, Context context) {
    try {
      var request = new HttpServletRequestWrapper(event);
      ServletRequestPathUtils.parseAndCache(request);

      var headers = toMultiValueHeaders(event.getHeaders());

      var responseOutputStream = new ByteArrayOutputStream();
      HttpServletResponseWrapper response =
          new HttpServletResponseWrapper(responseOutputStream, headers);

      var executionChain = handlerMapping.getHandler(request);
      if (executionChain == null) {
        throw new RuntimeException("No handler found for request " + request.getRequestURI());
      }

      var handler = executionChain.getHandler();
      handlerAdapter.handle(request, response, handler);

      var responseBody = responseOutputStream.toString(UTF_8);
      return new LambdaUrlResponseEvent(
          response.getStatus(), flattenHeaders(headers), responseBody);
    } catch (Exception e) {
      return exceptionHandler.handle(e);
    }
  }

  private ExceptionHandler<LambdaUrlResponseEvent> defaultExceptionHandler() {
    return new ExceptionHandlerImpl();
  }

  private ConfigurableApplicationContext applicationContext() {
    var application = new SpringApplication(PojaApplication.class);
    application.setDefaultProperties(Map.of("server.port", SERVER_PORT));
    return application.run();
  }

  private Map<String, String> flattenHeaders(Map<String, List<String>> headers) {
    return headers != null
        ? headers.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, entry -> join(",", entry.getValue())))
        : new HashMap<>();
  }

  private Map<String, List<String>> toMultiValueHeaders(Map<String, String> singleValueHeaders) {
    return singleValueHeaders != null
        ? singleValueHeaders.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, entry -> List.of(entry.getValue())))
        : new HashMap<>();
  }
}
