package school.hei.haapi.service;

import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static school.hei.haapi.endpoint.rest.model.WorkStudyStatus.*;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Sex.F;
import static school.hei.haapi.model.User.Sex.M;
import static school.hei.haapi.model.User.Status.*;
import static school.hei.haapi.service.aws.FileService.getFormattedBucketKey;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.UserUpserted;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.UserValidator;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.UserManagerDao;
import school.hei.haapi.service.aws.FileService;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
  private final UserRepository userRepository;
  private final EventProducer eventProducer;
  private final UserValidator userValidator;
  private final UserManagerDao userManagerDao;
  private final FileService fileService;
  private final MultipartFileConverter fileConverter;
  private final GroupRepository groupRepository;

  public void uploadUserProfilePicture(MultipartFile profilePictureAsMultipartFile, String userId) {
    User user = findById(userId);
    File savedProfilePicture = fileConverter.apply(profilePictureAsMultipartFile);
    String bucketKey =
        getFormattedBucketKey(user, "PROFILE_PICTURE")
            + fileService.getFileExtension(profilePictureAsMultipartFile);
    user.setProfilePictureKey(bucketKey);
    userRepository.save(user);
    fileService.uploadObjectToS3Bucket(bucketKey, savedProfilePicture);
  }

  @Transactional
  public void suspendStudentById(String suspendedStudentId) {
    userRepository.updateUserStatusById(SUSPENDED, suspendedStudentId);
  }

  public User updateUser(User user, String userId) {
    User toUpdate = refreshUserById(userId, user);
    return userRepository.save(toUpdate);
  }

  private User refreshUserById(String userId, User refreshedUser) {
    User userToRefresh = findById(userId);

    userToRefresh.setAddress(refreshedUser.getAddress());
    userToRefresh.setBirthDate(refreshedUser.getBirthDate());
    userToRefresh.setFirstName(refreshedUser.getFirstName());
    userToRefresh.setLastName(refreshedUser.getLastName());
    userToRefresh.setSex(refreshedUser.getSex());
    userToRefresh.setPhone(refreshedUser.getPhone());
    userToRefresh.setNic(refreshedUser.getNic());
    userToRefresh.setBirthPlace(refreshedUser.getBirthPlace());
    userToRefresh.setLongitude(refreshedUser.getLongitude());
    userToRefresh.setLatitude(refreshedUser.getLatitude());
    userToRefresh.setEntranceDatetime(refreshedUser.getEntranceDatetime());
    userToRefresh.setStatus(refreshedUser.getStatus());
    userToRefresh.setSpecializationField(refreshedUser.getSpecializationField());
    userToRefresh.setHighSchoolOrigin(refreshedUser.getHighSchoolOrigin());
    return userToRefresh;
  }

  public User findById(String userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException("User with id: " + userId + " not found"));
  }

  public User getByEmail(String email) {
    return userRepository.getByEmail(email);
  }

  @Transactional
  public List<User> saveAll(List<User> users) {
    userValidator.accept(users);
    // TODO: do not nullify profile picture here
    List<User> savedUsers = userRepository.saveAll(users);
    eventProducer.accept(
        users.stream().map(this::toUserUpsertedEvent).collect(toUnmodifiableList()));
    return savedUsers;
  }

  private UserUpserted toUserUpsertedEvent(User user) {
    return new UserUpserted().userId(user.getId()).email(user.getEmail());
  }

  public List<User> getByRole(
      User.Role role,
      PageFromOne page,
      BoundedPageSize pageSize,
      User.Status status,
      User.Sex sex) {
    return getByCriteria(role, "", "", "", page, pageSize, status, sex);
  }

  public List<User> getAll() {
    return userRepository.findAll();
  }

  public List<User> getByRoleAndStatus(User.Role role, User.Status status) {
    return userRepository.findAllByRoleAndStatus(role, status);
  }

  public List<User> getAllEnabledUsers() {
    return userRepository.findAllByStatus(ENABLED);
  }

  public List<User> getByCriteria(
      User.Role role,
      String firstName,
      String lastName,
      String ref,
      PageFromOne page,
      BoundedPageSize pageSize,
      User.Status status,
      User.Sex sex) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));
    return userManagerDao.findByCriteria(
        role, ref, firstName, lastName, pageable, status, sex, null, null, null, null, null);
  }

  public List<User> getByLinkedCourse(
      User.Role role,
      String firstName,
      String lastName,
      String ref,
      String courseId,
      PageFromOne page,
      BoundedPageSize pageSize,
      User.Status status,
      User.Sex sex,
      WorkStudyStatus workStatus,
      Instant commitmentBeginDate,
      List<String> excludeGroupIds) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));
    return userManagerDao.findByCriteria(
        role,
        ref,
        firstName,
        lastName,
        pageable,
        status,
        sex,
        workStatus,
        commitmentBeginDate,
        courseId,
        Instant.now(),
        excludeGroupIds);
  }

  public List<User> getByGroupId(String groupId) {
    return userRepository.findAllRemainingStudentsByGroupId(groupId).get();
  }

  public List<User> getByGroupId(String groupId, PageFromOne page, BoundedPageSize pageSize) {
    var returnedStudent = getByGroupId(groupId);
    // Calculate start and end index for pagination
    int startIndex = (page.getValue() - 1) * pageSize.getValue();
    int endIndex = Math.min(startIndex + pageSize.getValue(), returnedStudent.size());

    if (startIndex >= returnedStudent.size()) {
      return List.of();
    }
    return returnedStudent.subList(startIndex, endIndex);
  }

  public List<User> getAllStudentNotDisabled() {
    return userRepository.findAllStudentNotDisabled();
  }

  public Statistics getStudentsStat(List<Student> students) {
    return new Statistics()
        .women(
            new StatisticsDetails()
                .disabled(userRepository.countBySexAndRoleAndStatus(F, STUDENT, DISABLED))
                .suspended(userRepository.countBySexAndRoleAndStatus(F, STUDENT, SUSPENDED))
                .enabled(userRepository.countBySexAndRoleAndStatus(F, STUDENT, ENABLED))
                .total(userRepository.countBySexAndRole(F, STUDENT)))
        .totalGroups((int) groupRepository.count())
        .totalStudents(userRepository.countByRole(STUDENT))
        .men(
            new StatisticsDetails()
                .disabled(userRepository.countBySexAndRoleAndStatus(M, STUDENT, DISABLED))
                .suspended(userRepository.countBySexAndRoleAndStatus(M, STUDENT, SUSPENDED))
                .enabled(userRepository.countBySexAndRoleAndStatus(M, STUDENT, ENABLED))
                .total(userRepository.countBySexAndRole(M, STUDENT)))
        .studentsAlternating(
            new StatisticsStudentsAlternating()
                .total(
                    getStudentsAlternatingSize(students, WILL_BE_WORKING)
                        + getStudentsAlternatingSize(students, HAVE_BEEN_WORKING)
                        + getStudentsAlternatingSize(students, WORKING))
                .haveBeenWorking(getStudentsAlternatingSize(students, HAVE_BEEN_WORKING))
                .working(getStudentsAlternatingSize(students, WORKING))
                .notWorking(getStudentsAlternatingSize(students, NOT_WORKING))
                .willWork(getStudentsAlternatingSize(students, WILL_BE_WORKING)));
  }

  public Integer getStudentsAlternatingSize(
      List<Student> students, WorkStudyStatus workStudyStatus) {
    return students.stream()
        .filter(student -> Objects.equals(student.getWorkStudyStatus(), workStudyStatus))
        .toList()
        .size();
  }
}
