package school.hei.haapi.service.event;

import static school.hei.haapi.model.User.Status.SUSPENDED;

import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.model.UpdateStudentsStatus;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.dao.UserManagerDao;
import school.hei.haapi.service.PaymentService;
import school.hei.haapi.service.UserService;

@Service
@AllArgsConstructor
public class UpdateStudentsStatusService implements Consumer<UpdateStudentsStatus> {

  private final UserService userService;
  private final PaymentService paymentService;
  private final UserManagerDao userManagerDao;

  // If the student has no more overdue fees, their status will be set to ENABLED, otherwise it will
  // remain SUSPENDED.
  public void checkSuspendedStudentsStatus() {
    List<User> suspendedStudents = userService.getAllSuspendedUsers();
    for (User student : suspendedStudents) {
      paymentService.computeUserStatusAfterPayingFee(student);
    }
  }

  // Suspends students with overdue fees if it hasn't been done already.
  public void suspendStudentsWithUnpaidOrLateFee() {
    List<User> students = userService.getStudentsWithUnpaidOrLateFee();
    for (User student : students) {
      if (student.getStatus() != SUSPENDED) {
        userManagerDao.updateUserStatusById(SUSPENDED, student.getId());
      }
    }
  }

  @Override
  public void accept(UpdateStudentsStatus updateStudentsStatus) {
    checkSuspendedStudentsStatus();
    suspendStudentsWithUnpaidOrLateFee();
  }
}
