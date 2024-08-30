package school.hei.haapi.endpoint.rest.mapper;

import static school.hei.haapi.endpoint.rest.mapper.FileInfoMapper.ONE_DAY_DURATION_AS_LONG;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.model.User;
import school.hei.haapi.model.WorkDocument;
import school.hei.haapi.service.GroupService;
import school.hei.haapi.service.UserService;
import school.hei.haapi.service.WorkDocumentService;
import school.hei.haapi.service.aws.FileService;
import school.hei.haapi.service.utils.IsStudentRepeatingYear;

@Component
@AllArgsConstructor
public class UserMapper {
  private final WorkDocumentService workDocumentService;
  private final StatusEnumMapper statusEnumMapper;
  private final SexEnumMapper sexEnumMapper;
  private final FileService fileService;
  private final GroupService groupService;
  private final GroupMapper groupMapper;
  private final UserService service;
  private final IsStudentRepeatingYear isStudentRepeatingYear;

  public UserIdentifier toIdentifier(User user) {
    return new UserIdentifier()
        .id(user.getId())
        .ref(user.getRef())
        .nic(user.getNic())
        .lastName(user.getLastName())
        .firstName(user.getFirstName())
        .email(user.getEmail());
  }

  public Student toRestStudent(User user) {
    Student restStudent = new Student();
    Optional<WorkDocument> studentLastWorkDocument =
        workDocumentService.findLastWorkDocumentByStudentId(user.getId());
    String profilePictureKey = user.getProfilePictureKey();
    String url =
        profilePictureKey != null
            ? fileService.getPresignedUrl(profilePictureKey, ONE_DAY_DURATION_AS_LONG)
            : null;

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
    restStudent.groups(
        groupService.getByUserId(user.getId()).stream().map(groupMapper::toRest).toList());
    restStudent.setCoordinates(
        new Coordinates().longitude(user.getLongitude()).latitude(user.getLatitude()));
    restStudent.setHighSchoolOrigin(user.getHighSchoolOrigin());
    restStudent.setWorkStudyStatus(
        workDocumentService.defineStudentWorkStatusFromWorkDocumentDetails(
            studentLastWorkDocument));
    restStudent.setProfessionalExperience(
        workDocumentService.defineStudentProfessionalExperienceStatus(studentLastWorkDocument));
    restStudent.setCommitmentBeginDate(
        workDocumentService.defineStudentCommitmentBegin(studentLastWorkDocument));
    restStudent.setCommitmentEndDate(
        workDocumentService.defineStudentCommitmentEnd(studentLastWorkDocument));
    restStudent.setIsRepeatingYear(isStudentRepeatingYear.apply(user));
    return restStudent;
  }

  public Teacher toRestTeacher(User user) {
    Teacher teacher = new Teacher();
    String profilePictureKey = user.getProfilePictureKey();
    String url =
        profilePictureKey != null
            ? fileService.getPresignedUrl(profilePictureKey, ONE_DAY_DURATION_AS_LONG)
            : null;

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
    teacher.setHighSchoolOrigin(user.getHighSchoolOrigin());
    return teacher;
  }

  public Manager toRestManager(User user) {
    Manager manager = new Manager();
    String profilePictureKey = user.getProfilePictureKey();
    String url =
        profilePictureKey != null
            ? fileService.getPresignedUrl(profilePictureKey, ONE_DAY_DURATION_AS_LONG)
            : null;

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
    manager.setHighSchoolOrigin(user.getHighSchoolOrigin());
    return manager;
  }

  public Monitor toRestMonitor(User user) {
    Monitor monitor = new Monitor();
    String profilePictureKey = user.getProfilePictureKey();
    String url =
        profilePictureKey != null
            ? fileService.getPresignedUrl(profilePictureKey, ONE_DAY_DURATION_AS_LONG)
            : null;

    monitor.setId(user.getId());
    monitor.setFirstName(user.getFirstName());
    monitor.setLastName(user.getLastName());
    monitor.setEmail(user.getEmail());
    monitor.setRef(user.getRef());
    monitor.setStatus(statusEnumMapper.toRestStatus(user.getStatus()));
    monitor.setPhone(user.getPhone());
    monitor.setEntranceDatetime(user.getEntranceDatetime());
    monitor.setBirthDate(user.getBirthDate());
    monitor.setSex(sexEnumMapper.toRestSexEnum(user.getSex()));
    monitor.setAddress(user.getAddress());
    monitor.setBirthPlace(user.getBirthPlace());
    monitor.setNic(user.getNic());
    monitor.setProfilePicture(url);
    monitor.setCoordinates(
        new Coordinates().longitude(user.getLongitude()).latitude(user.getLatitude()));
    monitor.setHighSchoolOrigin(user.getHighSchoolOrigin());
    return monitor;
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
        .highSchoolOrigin(manager.getHighSchoolOrigin())
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
        .highSchoolOrigin(teacher.getHighSchoolOrigin())
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
        .highSchoolOrigin(student.getHighSchoolOrigin())
        .build();
  }

  public User toDomain(CrupdateMonitor monitor) {
    return User.builder()
        .role(User.Role.MONITOR)
        .id(monitor.getId())
        .firstName(monitor.getFirstName())
        .lastName(monitor.getLastName())
        .email(monitor.getEmail())
        .ref(monitor.getRef())
        .status(statusEnumMapper.toDomainStatus(monitor.getStatus()))
        .phone(monitor.getPhone())
        .entranceDatetime(monitor.getEntranceDatetime())
        .birthDate(monitor.getBirthDate())
        .sex(sexEnumMapper.toDomainSexEnum(monitor.getSex()))
        .address(monitor.getAddress())
        .nic(monitor.getNic())
        .birthPlace(monitor.getBirthPlace())
        .longitude(monitor.getCoordinates().getLongitude())
        .latitude(monitor.getCoordinates().getLatitude())
        .highSchoolOrigin(monitor.getHighSchoolOrigin())
        .build();
  }
}
