package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static school.hei.haapi.integration.testData.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.testData.CourseTestData.prog1;
import static school.hei.haapi.integration.testData.GroupTestData.g1;
import static school.hei.haapi.integration.testData.TeacherTestData.toky;
import static school.hei.haapi.model.User.Role.TEACHER;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.UserManagerDao;

class UserManagerDaoTest extends FacadeITMockedThirdParties {
  @Autowired private UserManagerDao subject;
  @Autowired private UserRepository userRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private GroupRepository groupRepository;
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;

  private User teacher;
  private Course course;
  private Group group;
  private CourseAssignment assignment;

  @BeforeEach
  void setUp() {
    teacher = userRepository.save(toky());
    course = courseRepository.save(prog1());
    group = groupRepository.save(g1());
    assignment =
        courseAssignmentRepository.save(createCourseAssignment(course, teacher, List.of(group)));
  }

  @AfterEach
  void tearDown() {
    courseAssignmentRepository.deleteById(assignment.getId());
    groupRepository.deleteById(group.getId());
    courseRepository.deleteById(course.getId());
    userRepository.deleteById(teacher.getId());
  }

  @Test
  void filter_user_ok() {
    var teachers =
        subject.findByCriteria(
            TEACHER,
            teacher.getRef(),
            teacher.getFirstName(),
            teacher.getLastName(),
            PageRequest.of(0, 10),
            teacher.getStatus(),
            teacher.getSex(),
            null,
            null,
            course.getId(),
            null,
            null,
            null);

    assertEquals(1, teachers.size());
    assertEquals(teacher.getId(), teachers.getFirst().getId());
  }
}
