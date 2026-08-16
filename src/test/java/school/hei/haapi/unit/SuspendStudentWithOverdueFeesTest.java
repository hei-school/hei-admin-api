package school.hei.haapi.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static school.hei.haapi.integration.testData.StudentTestData.axel;
import static school.hei.haapi.model.User.Status.DISABLED;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.UserManagerDao;
import school.hei.haapi.service.MpbsService;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.event.SuspendStudentsWithOverdueFeesService;

@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SuspendStudentWithOverdueFeesTest extends FacadeITMockedThirdParties {
  @MockBean UserService userService;
  @MockBean MpbsService mpbsService;
  @Autowired UserRepository userRepository;
  @Autowired UserManagerDao userManagerDao;

  private User disabledStudent;

  @BeforeEach
  void setUp() {
    var student = axel();
    student.setStatus(DISABLED);
    disabledStudent = userRepository.save(student);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteById(disabledStudent.getId());
  }

  @Test
  void suspendStudentWithOverdueFees_with_disabled_student_ko() {
    var subject =
        new SuspendStudentsWithOverdueFeesService(userManagerDao, userService, mpbsService);

    when(userService.getStudentsWithLateFee()).thenReturn(List.of(disabledStudent));
    when(mpbsService.countPendingOfStudent(any())).thenReturn(0L);

    subject.suspendStudentsWithUnpaidOrLateFee();

    var student = userRepository.findById(disabledStudent.getId()).orElseThrow();
    assertEquals(DISABLED, student.getStatus());
  }
}
