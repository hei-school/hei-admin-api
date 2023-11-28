package school.hei.haapi.endpoint.rest.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreateAttendanceMovement;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.UserRepository;

@Component
@AllArgsConstructor
public class CreateAttendanceValidator implements Consumer<List<CreateAttendanceMovement>> {
  private final UserRepository repository;

  public void accept(List<CreateAttendanceMovement> toCreates) {
    List<String> wrongStds = new ArrayList<>();
    toCreates.forEach(movement -> this.accept(movement, wrongStds));
  }

  public void accept(CreateAttendanceMovement createAttendanceMovement, List<String> wrongStds) {
    if (!repository.existsByRefContainingIgnoreCase(createAttendanceMovement.getStudentRef())) {
      wrongStds.add(createAttendanceMovement.getStudentRef());
    }
    ;

    if (!wrongStds.isEmpty()) {
      if (wrongStds.size() > 1) {
        throw new NotFoundException("Students with: #" + wrongStds.toString() + " are not found");
      } else {
        throw new NotFoundException("Student with: #" + wrongStds.toString() + " is not found");
      }
    }
  }
}
