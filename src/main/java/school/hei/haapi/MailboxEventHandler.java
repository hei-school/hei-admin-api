package school.hei.haapi;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.getenv;
import static java.lang.Thread.currentThread;
import static school.hei.haapi.concurrency.ThreadRenamer.renameWorkerThread;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import school.hei.haapi.endpoint.EndpointConf;
import school.hei.haapi.endpoint.event.EventConf;
import school.hei.haapi.endpoint.event.consumer.EventConsumer;
import school.hei.haapi.endpoint.event.consumer.model.ConsumableEvent;
import school.hei.haapi.endpoint.event.consumer.model.ConsumableEventTyper;
import software.amazon.awssdk.regions.Region;

@Slf4j
@PojaGenerated
public class MailboxEventHandler implements RequestHandler<SQSEvent, String> {

  public static final String SPRING_SERVER_PORT_FOR_RANDOM_VALUE = "0";
  private final ConsumableEventTyper consumableEventTyper =
      new ConsumableEventTyper(
          new EndpointConf().objectMapper(), new EventConf(Region.of(getenv("AWS_REGION"))));

  @Override
  public String handleRequest(SQSEvent event, Context context) {
    renameWorkerThread(currentThread());
    log.info("Received: event={}, awsReqId={}", event, context.getAwsRequestId());
    List<SQSMessage> messages = event.getRecords();
    consumableEventTyper
        .apply(messages)
        .forEach(ConsumableEvent::newRandomVisibilityTimeout); // note(init-visibility)
    log.info("SQS messages: {}", messages);

    var applicationContext = applicationContext();
    getRuntime()
        .addShutdownHook(
            // in case, say, the execution timed out
            // TODO: no, we have no control over when AWS shuts the JVM down
            //   Best is to regularly check whether we are nearing end of allowedTime,
            //   in which case we close resources before timing out.
            //   Frontal functions might have the same issue also.
            new Thread(() -> onHandled(applicationContext)));

    var eventConsumer = applicationContext.getBean(EventConsumer.class);
    var messageConverter = applicationContext.getBean(ConsumableEventTyper.class);

    eventConsumer.accept(messageConverter.apply(messages));

    onHandled(applicationContext);
    return "ok";
  }

  private void onHandled(ConfigurableApplicationContext applicationContext) {
    try {
      var hikariDatasource = applicationContext.getBean(HikariDataSource.class);
      hikariDatasource.close();

      applicationContext.close();
    } catch (Exception ignored) {
    }
  }

  private ConfigurableApplicationContext applicationContext(String... args) {
    SpringApplication application = new SpringApplication(PojaApplication.class);
    application.setDefaultProperties(
        Map.of(
            "spring.flyway.enabled", "false", "server.port", SPRING_SERVER_PORT_FOR_RANDOM_VALUE));
    application.setAdditionalProfiles("worker");
    return application.run(args);
  }
}
