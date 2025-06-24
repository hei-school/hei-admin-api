package school.hei.haapi.handler;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import school.hei.haapi.PojaApplication;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@SuppressWarnings("all")
public class LambdaHandler implements RequestStreamHandler {
  private static final SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse>
      handler;

  static {
    try {
      handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(PojaApplication.class);
    } catch (ContainerInitializationException e) {
      throw new RuntimeException("Initialization of Spring Boot Application failed", e);
    }
  }

  @Override
  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
      throws IOException {
    handler.proxyStream(inputStream, outputStream, context);
  }
}
