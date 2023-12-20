package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Manager;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.Teacher;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;

@Component
@AllArgsConstructor
public class UserMapper {
  private final StatusEnumMapper statusEnumMapper;
  private final SexEnumMapper sexEnumMapper;
  private final UserRepository repository;

  public Student toRestStudent(User user) {
    Student restStudent = new Student();
    restStudent.setId(user.getId());

    restStudent.setFirstName(user.getFirstName());
    restStudent.setLastName(user.getLastName());
    restStudent.setEmail(user.getEmail());
    restStudent.setRef(user.getRef());
    restStudent.setStatus(statusEnumMapper.toRestStatus(user.getStatus()));
    restStudent.setPhone(user.getPhone());
    restStudent.setEntranceDatetime(user.getEntranceDatetime());
    restStudent.setBirthDate(user.getBirthDate());
    restStudent.setSex(sexEnumMapper.toRestSexEnum(user.getSex()));
    restStudent.setAddress(user.getAddress());
    restStudent.setNic(user.getNic());
    restStudent.setBirthPlace(user.getBirthPlace());
    restStudent.setSpecializationChannel(user.getSpecializationChannel());

    return restStudent;
  }

  public Teacher toRestTeacher(User user) {
    Teacher teacher = new Teacher();
    teacher.setId(user.getId());

    teacher.setFirstName(user.getFirstName());
    teacher.setLastName(user.getLastName());
    teacher.setEmail(user.getEmail());
    teacher.setRef(user.getRef());
    teacher.setStatus(statusEnumMapper.toRestStatus(user.getStatus()));
    teacher.setPhone(user.getPhone());
    teacher.setEntranceDatetime(user.getEntranceDatetime());
    teacher.setBirthDate(user.getBirthDate());
    teacher.setSex(sexEnumMapper.toRestSexEnum(user.getSex()));
    teacher.setAddress(user.getAddress());
    teacher.setBirthPlace(user.getBirthPlace());
    teacher.setNic(user.getNic());

    return teacher;
  }

  public Manager toRestManager(User user) {
    Manager manager = new Manager();
    manager.setId(user.getId());

    manager.setFirstName(user.getFirstName());
    manager.setLastName(user.getLastName());
    manager.setEmail(user.getEmail());
    manager.setRef(user.getRef());
    manager.setStatus(statusEnumMapper.toRestStatus(user.getStatus()));
    manager.setPhone(user.getPhone());
    manager.setEntranceDatetime(user.getEntranceDatetime());
    manager.setBirthDate(user.getBirthDate());
    manager.setSex(sexEnumMapper.toRestSexEnum(user.getSex()));
    manager.setAddress(user.getAddress());
    manager.setBirthPlace(user.getBirthPlace());
    manager.setNic(user.getNic());

    return manager;
  }

  public User toDomain(Manager manager) {
    return User.builder()
        .role(User.Role.MANAGER)
        .id(manager.getId())
        .firstName(manager.getFirstName())
        .lastName(manager.getLastName())
        .email(manager.getEmail())
        .ref(manager.getRef())
        .status(statusEnumMapper.toDomainStatus(manager.getStatus()))
        .phone(manager.getPhone())
        .entranceDatetime(manager.getEntranceDatetime())
        .birthDate(manager.getBirthDate())
        .sex(sexEnumMapper.toDomainSexEnum(manager.getSex()))
        .address(manager.getAddress())
        .nic(manager.getNic())
        .birthPlace(manager.getBirthPlace())
        .build();
  }

  public User toDomain(Teacher teacher) {
    return User.builder()
        .role(User.Role.TEACHER)
        .id(teacher.getId())
        .firstName(teacher.getFirstName())
        .lastName(teacher.getLastName())
        .email(teacher.getEmail())
        .ref(teacher.getRef())
        .status(statusEnumMapper.toDomainStatus(teacher.getStatus()))
        .phone(teacher.getPhone())
        .entranceDatetime(teacher.getEntranceDatetime())
        .birthDate(teacher.getBirthDate())
        .sex(sexEnumMapper.toDomainSexEnum(teacher.getSex()))
        .address(teacher.getAddress())
        .nic(teacher.getNic())
        .birthPlace(teacher.getBirthPlace())
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
        .status(statusEnumMapper.toDomainStatus(student.getStatus()))
        .phone(student.getPhone())
        .entranceDatetime(student.getEntranceDatetime())
        .birthDate(student.getBirthDate())
        .sex(sexEnumMapper.toDomainSexEnum(student.getSex()))
        .address(student.getAddress())
        .birthPlace(student.getBirthPlace())
        .nic(student.getNic())
        .specializationChannel(student.getSpecializationChannel())
        .build();
  }
}
