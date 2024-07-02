package school.hei.haapi.endpoint.event.consumer;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import school.hei.haapi.PojaGenerated;
import school.hei.haapi.endpoint.event.consumer.model.TypedEvent;

@PojaGenerated
@AllArgsConstructor
@Component
@Slf4j
public class EventServiceInvoker implements Consumer<TypedEvent> {

  private final ApplicationContext applicationContext;

  @SneakyThrows
  @Override
  public void accept(TypedEvent typedEvent) {
    var typeName = typedEvent.typeName();
    var eventClasses = getAllClasses("school.hei.haapi.endpoint.event.model");
    for (var clazz : eventClasses) {
      if (clazz.getTypeName().equals(typeName)) {
        var serviceClazz = Class.forName(getEventService(typeName));
        var acceptMethod = serviceClazz.getMethod("accept", clazz);
        acceptMethod.invoke(applicationContext.getBean(serviceClazz), typedEvent.payload());
        return;
      }
    }

    throw new RuntimeException("Unexpected type for event=" + typedEvent);
  }

  private String getEventService(String eventClazzName) {
    var typeNameAsArray = eventClazzName.split("\\.");
    return "school.hei.haapi.service.event."
        + typeNameAsArray[typeNameAsArray.length - 1]
        + "Service";
  }

  private Set<Class<?>> getAllClasses(String packageName) {
    var reflections = new Reflections(packageName, Scanners.SubTypes.filterResultsBy(s -> true));
    return new HashSet<>(reflections.getSubTypesOf(Object.class));
  }
}
