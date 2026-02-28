package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.model.User.Status.DISABLED;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.UserManagerDao;
import school.hei.haapi.service.MpbsService;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.event.SuspendStudentsWithOverdueFeesService;

@Slf4j
@Testcontainers
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SuspendStudentWithOverdueFeesTest extends FacadeITMockedThirdParties {
  private static final String DISABLED_STUDENT_ID = "student_disabled";

  @MockBean UserService userService;
  @MockBean MpbsService mpbsService;
  @Autowired UserRepository userRepository;
  @Autowired UserManagerDao userManagerDao;

  @Test
  void suspendStudentWithOverdueFees_with_disabled_student_ko() {
    var disabledStudents = List.of(userRepository.findById(DISABLED_STUDENT_ID).orElse(null));
    var subject =
        new SuspendStudentsWithOverdueFeesService(userManagerDao, userService, mpbsService);

    when(userService.getStudentsWithLateFees()).thenReturn(disabledStudents);
    when(mpbsService.countPendingOfStudent(any())).thenReturn(0L);

    subject.suspendStudentsWithUnpaidOrLateFee();
    var student = userRepository.findById(DISABLED_STUDENT_ID).orElse(null);

    assertEquals(DISABLED, student.getStatus());
  }
}
