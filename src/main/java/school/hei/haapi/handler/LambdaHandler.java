package school.hei.haapi.handler;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sentry.Sentry;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import school.hei.haapi.PojaApplication;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
public class LambdaHandler implements RequestStreamHandler {
  private static final SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse>
      handler;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

  static {
    try {
      handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(PojaApplication.class);
    } catch (ContainerInitializationException e) {
      throw new RuntimeException("Initialization of Spring Boot Application failed", e);
    }
  }

  @Override
  public void handleRequest(InputStream input, OutputStream output, Context context)
      throws IOException {
    var inputStream = OBJECT_MAPPER.readValue(input, HttpApiV2ProxyRequest.class);
    try {
      var response = handler.proxy(inputStream, context);
      OBJECT_MAPPER.writeValue(output, response);
      output.flush();
    } catch (Exception e) {
      Sentry.captureException(e);
      throw e;
    } finally {
      Sentry.flush(Duration.ofSeconds(5).toMillis());
    }
  }
}
