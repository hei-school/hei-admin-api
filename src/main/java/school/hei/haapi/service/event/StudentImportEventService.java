package school.hei.haapi.service.event;

import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.StudentImportEvent;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.service.UserService;

@Service
@AllArgsConstructor
public class StudentImportEventService implements Consumer<StudentImportEvent> {
  private final UserMapper userMapper;
  private final UserService userService;

  @Override
  public void accept(StudentImportEvent event) {}
}
