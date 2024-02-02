package school.hei.haapi.endpoint.event;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import school.hei.haapi.conf.FacadeIT;
import school.hei.haapi.endpoint.event.gen.CheckAttendanceTriggered;
import school.hei.haapi.endpoint.event.gen.SendLateFeesEmailTriggered;
import school.hei.haapi.endpoint.event.gen.UpdateFeesStatusToLateTriggered;

public class GenClassesDeserializerTest extends FacadeIT {

  // TODO: Bodyless Gen classes must be PUBLIC and have AllArgsConstructor
  private static final String EMPTY_DETAIL_AS_STRING = "{}";
  @Autowired ObjectMapper subject;

  private static final List<Class> emptyGenClasses =
      List.of(
          CheckAttendanceTriggered.class,
          SendLateFeesEmailTriggered.class,
          UpdateFeesStatusToLateTriggered.class);

  @Test
  void can_deserialize_from_empty_detail_body() {
    emptyGenClasses.stream()
        .map(Class::getTypeName)
        .forEach(
            typeName -> {
              assertDoesNotThrow(
                  () -> {
                    Map<String, Object> body = emptyDetailedMapFrom(typeName);
                    subject.convertValue(body.get("detail"), Class.forName(typeName));
                  });
            });

    assertDoesNotThrow(
        () -> {
          String typeName = ClassWithAllArgsConstructorAnnotationAndBuilder.class.getTypeName();
          Map<String, Object> body = emptyDetailedMapFrom(typeName);
          subject.convertValue(body.get("detail"), Class.forName(typeName));
        });
  }

  private Map<String, Object> emptyDetailedMapFrom(String typename) throws JsonProcessingException {
    TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
    return subject.readValue(
        "{\"detail-type\":\"" + typename + "\", \"detail\":" + EMPTY_DETAIL_AS_STRING + "}",
        typeRef);
  }

  @Test
  void cannot_deserialize_from_constructorless_class() {
    assertThrows(
        IllegalAccessError.class,
        () -> {
          Map<String, Object> body =
              emptyDetailedMapFrom(AllArgsConstructorLessClassWithBuilder.class.getTypeName());
          subject.convertValue(body.get("detail"), AllArgsConstructorLessClassWithBuilder.class);
        });
  }
}
