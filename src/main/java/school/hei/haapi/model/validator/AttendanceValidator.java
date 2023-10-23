package school.hei.haapi.model.validator;


import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.StudentAttendance;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.UserRepository;

@Component
@AllArgsConstructor
public class AttendanceValidator implements Consumer<StudentAttendance> {
  private final UserRepository userRepository;

  public void accept(List<StudentAttendance> studentAttendances) {
    studentAttendances.forEach(this::accept);
  }

  @Override
  public void accept(StudentAttendance studentAttendance) {
    Set<String> violationMessages = new HashSet<>();
    if (studentAttendance.getStudent() == null) {
      violationMessages.add("Student is mandatory");
    }
    if (!violationMessages.isEmpty()) {
      String formatedViolationMessages = violationMessages.stream()
          .map(String::toString)
          .collect(Collectors.joining(". "));
      throw new BadRequestException(formatedViolationMessages);
    }
  }
}