package school.hei.haapi.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.LEAVE;
import static school.hei.haapi.service.aws.FileService.getFormattedBucketKey;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.gen.UserUpserted;
import school.hei.haapi.endpoint.rest.model.WorkStudyStatus;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.UserValidator;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.UserManagerDao;
import school.hei.haapi.service.aws.FileService;

@Service
@AllArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final EventProducer eventProducer;
  private final UserValidator userValidator;
  private final UserManagerDao userManagerDao;
  private final GroupFlowRepository groupFlowRepository;
  private final FileService fileService;
  private final MultipartFileConverter fileConverter;

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
        role, ref, firstName, lastName, pageable, status, sex, null, null, null);
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
      Instant commitmentBeginDate) {
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
        courseId);
  }

  public List<User> getByGroupId(String groupId) {
    Sort sortable = Sort.by(DESC, "flowDatetime");
    List<GroupFlow> studentsGroupFlow = groupFlowRepository.findAllByGroupId(groupId, sortable);
    List<User> groupedStudentsJoin = new ArrayList<>();
    List<User> groupedStudentsLeave = new ArrayList<>();

    studentsGroupFlow.forEach(
        groupFlow -> {
          if (groupFlow.getGroupFlowType().equals(LEAVE)) {
            groupedStudentsLeave.add(groupFlow.getStudent());
          }
          if (!groupedStudentsLeave.stream().distinct().toList().contains(groupFlow.getStudent())) {
            groupedStudentsJoin.add(groupFlow.getStudent());
          }
        });
    return groupedStudentsJoin.stream().distinct().collect(toList());
  }
}
