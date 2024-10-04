package school.hei.haapi.service.event;

import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.UpdateStudentsStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.service.PaymentService;
import school.hei.haapi.service.UserService;

@Service
@AllArgsConstructor
public class UpdateStudentsStatusService implements Consumer<UpdateStudentsStatus> {

  private final UserService userService;
  private final PaymentService paymentService;

  public void checkSuspendedStudentsStatus() {
    List<User> suspendedStudents = userService.getAllSuspendedUsers();
    for (User student : suspendedStudents) {
      paymentService.computeUserStatusAfterPayingFee(student);
    }
  }

  public void checkEnabledStudentsStatus() {
    List<User> enabledStudents = userService.getAllEnabledUsers();
    for (User student : enabledStudents) {
      paymentService.computeUserStatusAfterPayingFee(student);
    }
  }

  @Override
  public void accept(UpdateStudentsStatus updateStudentsStatus) {
    checkSuspendedStudentsStatus();
    checkEnabledStudentsStatus();
  }
}
