package school.hei.haapi.endpoint.rest.mapper;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.EnableStatus;
import school.hei.haapi.endpoint.rest.model.Manager;
import school.hei.haapi.endpoint.rest.model.Sex;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import java.util.List;

@Component
public class UserMapper {

  public Student toRestStudent(User user) {
    Student restStudent = new Student();
    restStudent.setId(user.getId());

    restStudent.setFirstName(user.getFirstName());
    restStudent.setLastName(user.getLastName());
    restStudent.setEmail(user.getEmail());
    restStudent.setRef(user.getRef());
    restStudent.setStatus(toRestUserStatus(user.getStatus()));
    restStudent.setPhone(user.getPhone());
    restStudent.setEntranceDatetime(user.getEntranceDatetime());
    restStudent.setBirthDate(user.getBirthDate());
    restStudent.setSex(user.getSex() == null ? null : Sex.fromValue(user.getSex().toString()));
    restStudent.setAddress(user.getAddress());

    return restStudent;
  }

  public Teacher toRestTeacher(User user) {
    Teacher teacher = new Teacher();
    teacher.setId(user.getId());

    teacher.setFirstName(user.getFirstName());
    teacher.setLastName(user.getLastName());
    teacher.setEmail(user.getEmail());
    teacher.setRef(user.getRef());
    teacher.setStatus(toRestUserStatus(user.getStatus()));
    teacher.setPhone(user.getPhone());
    teacher.setEntranceDatetime(user.getEntranceDatetime());
    teacher.setBirthDate(user.getBirthDate());
    teacher.setSex(user.getSex() == null ? null : Sex.fromValue(user.getSex().toString()));
    teacher.setAddress(user.getAddress());

    return teacher;
  }

  public Manager toRestManager(User user) {
    Manager manager = new Manager();
    manager.setId(user.getId());

    manager.setFirstName(user.getFirstName());
    manager.setLastName(user.getLastName());
    manager.setEmail(user.getEmail());
    manager.setRef(user.getRef());
    manager.setStatus(toRestUserStatus(user.getStatus()));
    manager.setPhone(user.getPhone());
    manager.setEntranceDatetime(user.getEntranceDatetime());
    manager.setBirthDate(user.getBirthDate());
    manager.setSex(Sex.fromValue(user.getSex().toString()));
    manager.setAddress(user.getAddress());

    return manager;
  }

  public User toDomain(Teacher teacher) {
    return User.builder()
        .role(User.Role.TEACHER)
        .id(teacher.getId())
        .firstName(teacher.getFirstName())
        .lastName(teacher.getLastName())
        .email(teacher.getEmail())
        .ref(teacher.getRef())
        .status(toDomainUserStatus(teacher.getStatus()))
        .phone(teacher.getPhone())
        .entranceDatetime(teacher.getEntranceDatetime())
        .birthDate(teacher.getBirthDate())
        .sex(teacher.getSex() == null ? null : User.Sex.valueOf(teacher.getSex().toString()))
        .address(teacher.getAddress())
        .build();
  }

  public User toDomain(Student student) {
    return User.builder()
        .role(User.Role.STUDENT)
        .id(student.getId())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .email(student.getEmail())
        .ref(student.getRef())
        .status(toDomainUserStatus(student.getStatus()))
        .phone(student.getPhone())
        .entranceDatetime(student.getEntranceDatetime())
        .birthDate(student.getBirthDate())
        .sex(student.getSex() == null ? null : User.Sex.valueOf(student.getSex().toString()))
        .address(student.getAddress())
        .build();
  }

  private User.Status toDomainUserStatus(EnableStatus status) {
    List<EnableStatus> expectedStatus = Arrays.stream(EnableStatus.values()).toList();
    switch (status) {
      case ACTIVE:
        return User.Status.ENABLED;
      case LEFT:
        return User.Status.DISABLED;
      default: throw new BadRequestException("Status must be type of: " + expectedStatus.toString());
    }
  }

  private EnableStatus toRestUserStatus(User.Status status) {
    List<User.Status> expectedStatus = Arrays.stream(User.Status.values()).toList();
    switch (status) {
      case ENABLED:
        return EnableStatus.ACTIVE;
      case DISABLED:
        return EnableStatus.LEFT;
      default: throw new BadRequestException("Status must be type of: " + expectedStatus.toString());
    }
  }
}
