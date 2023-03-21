package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import main.java.school.hei.haapi.service.utils.UpdateStudentscourse;
import main.java.school.hei.haapi.service.utils.updateStudentscourse;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.TypedUserUpserted;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.validator.UserValidator;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.UserManagerDao;
import school.hei.haapi.repository.CourseRepository;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final EventProducer eventProducer;
  private final UserValidator userValidator;
  private final CourseRepository courseRepository;

  private final UserManagerDao userManagerDao;

  public User getById(String userId) {
    return userRepository.getById(userId);
  }

  public User getByEmail(String email) {
    return userRepository.getByEmail(email);
  }

  @Transactional
  public List<User> saveAll(List<User> users) {
    userValidator.accept(users);
    List<User> savedUsers = userRepository.saveAll(users);
    eventProducer.accept(users.stream()
        .map(this::toTypedEvent)
        .collect(toUnmodifiableList()));
    return savedUsers;
  }

  private TypedUserUpserted toTypedEvent(User user) {
    return new TypedUserUpserted(
        new UserUpserted()
            .userId(user.getId())
            .email(user.getEmail()));
  }

  public List<User> getByRole(User.Role role, PageFromOne page, BoundedPageSize pageSize) {
    return getByCriteria(role, "", "", "", page, pageSize);
  }

  public List<User> getByCriteria(
      User.Role role, String firstName, String lastName, String ref,
      PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(ASC, "ref"));
    return userManagerDao.findByCriteria(
           role, ref, firstName, lastName, pageable);
  }

  public List<Course> updateStudentcourse(List<UpdateStudentscourse> plainbody,String student_id) {
    return plainbody.stream().map((requestBody)->{
        User variableValueForUserFromDatabaseAcciredFromRequestBody = userRepository.getByUserId(student_id);
        // returnedList.add(variableValueForUserFromDataBaseAcciredFromRequestBody);
          userRepository.save(variableValueForUserFromDataBaseAcciredFromRequestBody.Builder()
            .setCourseStatus(requestBody.getStatus())
            .build();
          )
        }
      );
      return courseRepository.getByCourseId(plainbody.courseId)
  }
}
