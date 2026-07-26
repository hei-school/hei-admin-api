package school.hei.haapi.service;

import static java.time.Instant.now;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;
import static org.springframework.data.domain.Pageable.unpaged;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Role.TEACHER;
import static school.hei.haapi.model.User.Status.ENABLED;
import static school.hei.haapi.model.User.Status.SUSPENDED;
import static school.hei.haapi.service.aws.FileService.getFormattedProfilePictureKey;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.StudentImportEvent;
import school.hei.haapi.endpoint.event.model.UserUpserted;
import school.hei.haapi.endpoint.rest.model.PaymentFrequency;
import school.hei.haapi.endpoint.rest.model.Statistics;
import school.hei.haapi.endpoint.rest.model.StudentImportValidationResult;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.endpoint.rest.model.WorkStudyStatus;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.EventParticipant;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.dto.StudentImportDto;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.promotion.PromotionLevelOutOfRangeException;
import school.hei.haapi.model.validator.UserValidator;
import school.hei.haapi.repository.EventParticipantRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.PromotionRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.WorkDocumentRepository;
import school.hei.haapi.repository.dao.UserManagerDao;
import school.hei.haapi.service.aws.FileService;
import school.hei.haapi.service.utils.XlsxCellsGenerator;
import school.hei.haapi.service.utils.excel.ExcelParser;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
  private final UserRepository userRepository;
  private final WorkDocumentRepository workDocumentRepository;
  private final EventProducer eventProducer;
  private final UserValidator userValidator;
  private final UserManagerDao userManagerDao;
  private final FileService fileService;
  private final MultipartFileConverter fileConverter;
  private final GroupRepository groupRepository;
  private final PromotionRepository promotionRepository;
  private final MonitoringStudentService monitoringStudentService;
  private final FeeService feeService;
  private final EventParticipantRepository eventParticipantRepository;
  private final XlsxCellsGenerator<User> userXlsxCellsGenerator;
  private final XlsxCellsGenerator<EventParticipant> eventParticipantXlsxCellsGenerator;
  private final BucketComponent bucketComponent;
  private static final String STUDENT_XLSX_IMPORT_BUCKET_KEY = "/STUDENT_XLSX_IMPORT/";

  @Transactional
  public void uploadUserProfilePicture(MultipartFile profilePictureAsMultipartFile, String userId) {
    var user = getById(userId);
    var savedProfilePicture = fileConverter.apply(profilePictureAsMultipartFile);
    var bucketKey =
        getFormattedProfilePictureKey(user)
            + fileService.getFileExtension(profilePictureAsMultipartFile);
    user.setProfilePictureKey(bucketKey);
    userRepository.save(user);
    fileService.uploadObjectToS3Bucket(bucketKey, savedProfilePicture);
  }

  @Transactional
  public void suspendStudentById(String suspendedStudentId) {
    userManagerDao.updateUserStatusById(SUSPENDED, suspendedStudentId);
  }

  @Transactional
  public User updateUser(User user, String userId) {
    var toUpdate = refreshUserById(userId, user);
    return userRepository.save(toUpdate);
  }

  @Transactional
  public User refreshUserById(String userId, User refreshedUser) {
    var userToRefresh = getById(userId);

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
    userToRefresh.setDegree(refreshedUser.getDegree());
    userToRefresh.setCnaps(refreshedUser.getCnaps());
    userToRefresh.setEndingService(refreshedUser.getEndingService());
    userToRefresh.setOstie(refreshedUser.getOstie());
    userToRefresh.setFunction(refreshedUser.getFunction());
    return userToRefresh;
  }

  @Transactional(readOnly = true)
  public User getById(String userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException("User with id: " + userId + " not found"));
  }

  public List<User> findAllByRefIn(List<String> userRefs) {
    return userRepository.findAllByRefIn(userRefs);
  }

  // TODO: Must be get, find must return Optional
  public User findByRef(String userRef) {
    return userRepository
        .findByRef(userRef)
        .orElseThrow(() -> new NotFoundException("User with ref: " + userRef + " not found"));
  }

  public User getByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new NotFoundException("User with email %s not found".formatted(email)));
  }

  @Transactional
  public List<User> saveAll(List<User> users) {
    userValidator.accept(users);
    // TODO: do not nullify profile picture here
    var savedUsers = userRepository.saveAll(users);
    eventProducer.accept(users.stream().map(this::toUserUpsertedEvent).toList());
    return savedUsers;
  }

  @Transactional
  public List<User> saveAll(
      HashMap<User, PaymentFrequency> userPaymentFrequencyMap, Instant firstDueDatetime) {
    var users = new ArrayList<>(userPaymentFrequencyMap.keySet());
    userValidator.accept(users);
    var savedUsers = userRepository.saveAll(users);
    eventProducer.accept(users.stream().map(this::toUserUpsertedEvent).toList());

    // TODO: handle existing users exception when creating fees automatically
    for (Map.Entry<User, PaymentFrequency> entry : userPaymentFrequencyMap.entrySet()) {
      if (entry.getValue() != null)
        feeService.saveFromPaymentFrequency(entry.getKey(), entry.getValue(), firstDueDatetime);
    }

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

  public <T> byte[] getByRoleAndStatusAsXlsx(
      User.Role role, User.Status status, Function<User, T> mapper) {
    var users = getByRoleAndStatus(role, status);
    XlsxCellsGenerator<T> generator = new XlsxCellsGenerator<>();
    List<T> mappedUsers = users.stream().map(mapper).toList();

    return generator.apply(
        mappedUsers,
        List.of(
            "ref",
            "firstName",
            "lastName",
            "sex",
            "phone",
            "email",
            "nic",
            "function",
            "ostie",
            "cnaps",
            "address"));
  }

  public StudentImportValidationResult initStudentImportFromXlsx(
      File excelFile, Instant dueDatetime) {
    var parser = new ExcelParser<>(StudentImportDto.class, StudentImportDto.getCellMap());
    var coordinatorEmail = AuthProvider.getPrincipal().getUser().getEmail();
    try {
      var parseResult = parser.parseFile(excelFile, 0, CREATE_NULL_AS_BLANK);
      if (parseResult.skippedRows().size() > 1) {
        var errorMessage =
            parseResult.skippedRows().values().stream()
                .map(Throwable::getMessage)
                .collect(Collectors.joining("\n"));
        throw new BadRequestException(errorMessage);
      }
      var importResults = parseResult.parsedResult();
      if (importResults.size() > 50) {
        throw new BadRequestException(
            "Le nombre maximum d'importation par excel est de 50 étudiants");
      }
      validateDuplicateStudentImport(importResults);
      bucketComponent.upload(excelFile, STUDENT_XLSX_IMPORT_BUCKET_KEY + excelFile.getName());
      eventProducer.accept(
          List.of(
              StudentImportEvent.builder()
                  .coordinatorEmail(coordinatorEmail)
                  .students(importResults)
                  .dueDatetime(dueDatetime)
                  .build()));
      return new StudentImportValidationResult().validStudentNumber(importResults.size());
    } catch (IOException e) {
      throw new RuntimeException("Unable to read file");
    }
  }

  private void validateDuplicateStudentImport(List<StudentImportDto> importResults) {
    var seenRefs = new HashSet<>();
    var seenEmails = new HashSet<>();
    for (StudentImportDto dto : importResults) {
      if (!seenRefs.add(dto.getRef())) {
        throw new BadRequestException("Référence dupliqués détecté: " + dto.getRef());
      }
      if (!seenEmails.add(dto.getEmail())) {
        throw new BadRequestException("Email dupliqués détecté: " + dto.getEmail());
      }
    }
  }

  public List<User> getAllEnabledUsers() {
    return userRepository.findAllByStatus(ENABLED);
  }

  public List<User> getAllSuspendedUsers() {
    return userRepository.findAllByStatus(SUSPENDED);
  }

  public List<User> getByCriteria(
      User.Role role,
      String firstName,
      String lastName,
      String ref,
      PageFromOne page,
      BoundedPageSize pageSize) {
    var pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));
    return userManagerDao.findByCriteria(
        role, ref, firstName, lastName, pageable, null, null, null, null, null, null, null, null);
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
    var pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));

    return userManagerDao.findByCriteria(
        role, ref, firstName, lastName, pageable, status, sex, null, null, null, null, null, null);
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
    log.info("Page = {}", page.getValue());
    log.info("PageSize = {}", pageSize.getValue());
    var pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));
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
        now(),
        excludeGroupIds,
        null);
  }

  public List<User> getByGroupId(String groupId, Pageable pageable) {
    return getByGroupIds(List.of(groupId), pageable);
  }

  public List<User> getByGroupIds(Collection<String> groupIds, Pageable pageable) {
    return userManagerDao.findByCriteria(
        null, null, null, null, pageable, null, null, null, null, null, null, null, groupIds);
  }

  public List<User> getByStudentRefAndGroupIds(
      Collection<String> groupIds, String studentRef, Pageable pageable) {
    return userManagerDao.findByCriteria(
        STUDENT,
        studentRef,
        null,
        null,
        pageable,
        ENABLED,
        null,
        null,
        null,
        null,
        null,
        null,
        groupIds);
  }

  public byte[] generateStudentsGroup(String groupId) {
    var studentsGroup = getByGroupId(groupId, unpaged());
    return userXlsxCellsGenerator.apply(studentsGroup, List.of("ref", "firstName", "lastName"));
  }

  public byte[] generateTeachersXlsx() {
    var teachers = userRepository.findAllByRoleAndStatus(TEACHER, ENABLED);
    return userXlsxCellsGenerator.apply(teachers, List.of("firstName", "lastName", "email", "sex"));
  }

  public List<User> getByGroupIdWithFilter(
      String groupId, PageFromOne page, BoundedPageSize pageSize, String studentFirstname) {
    var pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(ASC, "ref"));
    return userRepository
        .findStudentGroupsWithFilter(groupId, studentFirstname, pageable)
        .getContent();
  }

  public Statistics getStudentsStat() {
    var studentStatisticsDao = userRepository.getStudentsStatistics();
    var alternatingStatisticsDao = workDocumentRepository.getStudentAlternatingStatistics();
    return studentStatisticsDao.toRestStatistics(alternatingStatisticsDao);
  }

  // Todo: try to move in MonitoringStudentService
  public List<User> findMonitorsByStudentId(String studentId) {
    return monitoringStudentService.getMonitorsByStudentId(studentId);
  }

  public List<User> getStudentsWithLateFee() {
    return userRepository.getStudentsWithLateFees();
  }

  public byte[] generateStudentsInEventXlsx(String eventId) {
    var students =
        eventParticipantRepository
            .findAllByEventId(eventId, null)
            .orElseThrow(
                () -> new NotFoundException("Event with id #" + eventId + " does not exist"));
    return eventParticipantXlsxCellsGenerator.apply(
        students,
        List.of(
            "participant.ref",
            "participant.lastName",
            "participant.firstName",
            "participant.email",
            "group.ref",
            "status"));
  }

  public byte[] generateStudentsInPromotionXlsx(String promotionId) {
    var promotion =
        promotionRepository
            .findById(promotionId)
            .orElseThrow(
                () ->
                    new NotFoundException("Promotion with id #" + promotionId + " does not exist"));
    List<User> students = new ArrayList<>();
    promotion.getGroups().forEach(group -> students.addAll(getByGroupId(group.getId(), unpaged())));
    return userXlsxCellsGenerator.apply(students, List.of("firstName", "lastName", "email", "sex"));
  }

  public byte[] generateAllStudentsAsXlsx(
      String courseId,
      User.Status status,
      User.Sex sex,
      WorkStudyStatus workStatus,
      List<String> excludeGroupIds) {
    List<User> students =
        userManagerDao.findByCriteria(
            STUDENT,
            null,
            null,
            null,
            unpaged(),
            status,
            sex,
            workStatus,
            null,
            courseId,
            now(),
            excludeGroupIds,
            null);
    return userXlsxCellsGenerator.apply(students, List.of("firstName", "lastName", "email", "sex"));
  }

  public List<User> getByRoleAndIds(Collection<User.Role> roles, Collection<String> ids) {
    return userRepository.findAllByRoleInAndIdIn(roles, ids);
  }

  @Transactional
  public StudentLevel getStudentLevel(String studentId) {
    try {
      return getById(studentId)
          .findCurrentGroup()
          .map(g -> g.getPromotion().getLevelAt(now()))
          .orElse(null);
    } catch (PromotionLevelOutOfRangeException e) {
      log.error("Level for student id {} is out of bounds: {}", studentId, e.getMessage());
      return null;
    }
  }
}
