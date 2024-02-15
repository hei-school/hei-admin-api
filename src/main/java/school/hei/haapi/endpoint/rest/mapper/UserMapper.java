package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.aws.FileService;

@Component
@AllArgsConstructor
public class UserMapper {
  private final StatusEnumMapper statusEnumMapper;
  private final SexEnumMapper sexEnumMapper;
  private final UserRepository repository;
  private final FileService fileService;

  public Student toRestStudent(User user) {
    Student restStudent = new Student();
    String profilePictureKey = user.getProfilePictureKey();
    String url =
        profilePictureKey != null ? fileService.getPresignedUrl(profilePictureKey, 86400L) : null;

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
    restStudent.setSpecializationField(user.getSpecializationField());
    restStudent.setProfilePicture(url);
    restStudent.setCoordinates(
        new Coordinates().longitude(user.getLongitude()).latitude(user.getLatitude()));

    return restStudent;
  }

  public Teacher toRestTeacher(User user) {
    Teacher teacher = new Teacher();
    String profilePictureKey = user.getProfilePictureKey();
    String url =
        profilePictureKey != null ? fileService.getPresignedUrl(profilePictureKey, 86400L) : null;

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
    teacher.setProfilePicture(url);
    teacher.setCoordinates(
        new Coordinates().longitude(user.getLongitude()).latitude(user.getLatitude()));

    return teacher;
  }

  public Manager toRestManager(User user) {
    Manager manager = new Manager();
    String profilePictureKey = user.getProfilePictureKey();
    String url =
        profilePictureKey != null ? fileService.getPresignedUrl(profilePictureKey, 86400L) : null;

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
    manager.setProfilePicture(url);
    manager.setCoordinates(
        new Coordinates().longitude(user.getLongitude()).latitude(user.getLatitude()));

    return manager;
  }

  public User toDomain(CrupdateManager manager) {
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
        .longitude(manager.getCoordinates().getLongitude())
        .latitude(manager.getCoordinates().getLatitude())
        .build();
  }

  public User toDomain(CrupdateTeacher teacher) {
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
        .longitude(teacher.getCoordinates().getLongitude())
        .latitude(teacher.getCoordinates().getLatitude())
        .build();
  }

  public User toDomain(CrupdateStudent student) {
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
        .specializationField(student.getSpecializationField())
        .longitude(student.getCoordinates().getLongitude())
        .latitude(student.getCoordinates().getLatitude())
        .build();
  }
}
