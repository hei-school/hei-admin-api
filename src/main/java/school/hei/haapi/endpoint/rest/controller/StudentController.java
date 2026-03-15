package school.hei.haapi.endpoint.rest.controller;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static school.hei.haapi.model.User.Role.STUDENT;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.GroupFlowMapper;
import school.hei.haapi.endpoint.rest.mapper.SexEnumMapper;
import school.hei.haapi.endpoint.rest.mapper.StatusCheckMapper;
import school.hei.haapi.endpoint.rest.mapper.StatusEnumMapper;
import school.hei.haapi.endpoint.rest.mapper.UserMapper;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.endpoint.rest.validator.CoordinatesValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.service.GroupFlowService;
import school.hei.haapi.service.MultipartFileConverter;
import school.hei.haapi.service.StatusCheckService;
import school.hei.haapi.service.StudentFileService;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class StudentController {
  private final UserService userService;
  private final UserMapper userMapper;
  private final GroupFlowService groupFlowService;
  private final GroupFlowMapper groupFlowMapper;
  private final StatusEnumMapper statusEnumMapper;
  private final SexEnumMapper sexEnumMapper;
  private final MultipartFileConverter fileConverter;
  private final CoordinatesValidator validator;
  private final StudentFileService studentFileService;
  private final StatusCheckService statusCheckService;
  private final StatusCheckMapper statusCheckMapper;

  @PostMapping(value = "/students/{id}/picture/raw", consumes = MULTIPART_FORM_DATA_VALUE)
  public Student uploadStudentProfilePicture(
      @RequestPart("file_to_upload") MultipartFile profilePictureAsMultipartFile,
      @PathVariable String id) {
    userService.uploadUserProfilePicture(profilePictureAsMultipartFile, id);
    return userMapper.toRestStudent(userService.getById(id));
  }

  @GetMapping("/students/{id}")
  public Student getStudentById(@PathVariable String id) {
    return userMapper.toRestStudent(userService.getById(id));
  }

  @GetMapping("/groups/{groupId}/students")
  public List<Student> getStudentsByGroupId(
      @PathVariable String groupId,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize,
      @RequestParam(name = "first_name", required = false) String studentFirstname) {
    return userService.getByGroupIdWithFilter(groupId, page, pageSize, studentFirstname).stream()
        .map(userMapper::toRestStudent)
        .collect(Collectors.toUnmodifiableList());
  }

  @GetMapping(value = "/groups/{id}/students/raw", produces = "application/vnd.ms-excel")
  public byte[] generateStudentsGroupInXlsx(@PathVariable String id) {
    return userService.generateStudentsGroup(id);
  }

  @GetMapping("/students")
  public List<Student> getStudents(
      @RequestParam PageFromOne page,
      @RequestParam("page_size") BoundedPageSize pageSize,
      @RequestParam(value = "ref", required = false, defaultValue = "") String ref,
      @RequestParam(value = "first_name", required = false, defaultValue = "") String firstName,
      @RequestParam(value = "last_name", required = false, defaultValue = "") String lastName,
      @RequestParam(value = "course_id", required = false, defaultValue = "") String courseId,
      @RequestParam(name = "status", required = false) EnableStatus status,
      @RequestParam(name = "sex", required = false) Sex sex,
      @RequestParam(name = "work_study_status", required = false) WorkStudyStatus workStatus,
      @RequestParam(name = "commitment_begin_date", required = false) Instant commitmentBeginDate,
      @RequestParam(name = "exclude_groups", required = false) List<String> excludeGroupIds) {
    User.Sex domainSex = sexEnumMapper.toDomainSexEnum(sex);
    User.Status domainStatus = statusEnumMapper.toDomainStatus(status);
    return userService
        .getByLinkedCourse(
            STUDENT,
            firstName,
            lastName,
            ref,
            courseId,
            page,
            pageSize,
            domainStatus,
            domainSex,
            workStatus,
            commitmentBeginDate,
            excludeGroupIds)
        .stream()
        .map(userMapper::toRestStudent)
        .toList();
  }

  @PutMapping("/students")
  public List<Student> saveAll(
      @RequestBody List<CrupdateStudent> toWrite,
      @RequestParam(name = "due_datetime", required = false) Instant dueDatetime) {
    toWrite.forEach(student -> validator.accept(student.getCoordinates()));
    return userService.saveAll(userMapper.toMapDomain(toWrite), dueDatetime).stream()
        .map(userMapper::toRestStudent)
        .toList();
  }

  @PutMapping("/students/{id}")
  public Student updateStudent(
      @PathVariable(name = "id") String studentId, @RequestBody CrupdateStudent toUpdate) {
    validator.accept(toUpdate.getCoordinates());
    return userMapper.toRestStudent(
        userService.updateUser(userMapper.toDomain(toUpdate), studentId));
  }

  @GetMapping("/students/{student_id}/level")
  public StudentLevel getStudentLevel(@PathVariable(name = "student_id") String studentId) {
    return userService.getStudentLevel(studentId);
  }

  @PostMapping("/students/{id}/group_flows")
  public List<GroupFlow> moveOrDeleteStudentInGroup(
      @PathVariable(name = "id") String id, @RequestBody List<CreateGroupFlow> createGroupFlow) {
    return groupFlowService.saveAll(createGroupFlow).stream().map(groupFlowMapper::toRest).toList();
  }

  @GetMapping("/students/stats")
  public Statistics getStats() {
    return userService.getStudentsStat();
  }

  @GetMapping(value = "/students/raw/xlsx", produces = "application/vnd.ms-excel")
  public byte[] generateStudentsInXlsx(
      @RequestParam(value = "course_id", required = false, defaultValue = "") String courseId,
      @RequestParam(name = "status", required = false) EnableStatus status,
      @RequestParam(name = "sex", required = false) Sex sex,
      @RequestParam(name = "work_study_status", required = false) WorkStudyStatus workStatus,
      @RequestParam(name = "exclude_groups", required = false) List<String> excludeGroupIds) {
    User.Sex domainSex = sexEnumMapper.toDomainSexEnum(sex);
    User.Status domainStatus = statusEnumMapper.toDomainStatus(status);
    return userService.generateAllStudentsAsXlsx(
        courseId, domainStatus, domainSex, workStatus, excludeGroupIds);
  }

  @PostMapping(value = "/students/import", consumes = MULTIPART_FORM_DATA_VALUE)
  public StudentImportValidationResult importStudents(
      @RequestParam("due_datetime") Instant dueDatetime,
      @RequestPart("file_to_upload") MultipartFile fileToUpload) {
    return userService.initStudentImportFromXlsx(fileConverter.apply(fileToUpload), dueDatetime);
  }

  @GetMapping("/students/import/template")
  public String getStudentImportTemplateURL() {
    return studentFileService.getStudentImportTemplateURL();
  }

  @GetMapping("/students/{student_id}/status-checks")
  public List<StatusCheck> getStudentStatusChecks(
      @PathVariable(name = "student_id") String studentId) {
    return statusCheckService.getByConcernedStudentId(studentId).stream()
        .map(statusCheckMapper::toRest)
        .toList();
  }

  @PatchMapping("/students/{student_id}/status-checks/{status_check_id}")
  public StatusCheck updateStatusCheck(
      @PathVariable(name = "student_id") String studentId,
      @PathVariable(name = "status_check_id") String statusCheckId,
      @RequestBody UpdateStatusCheck updateStatusCheck) {
    return statusCheckMapper.toRest(
        statusCheckService.updateStatusCheckByStudentId(
            studentId, statusCheckId, updateStatusCheck));
  }

  @PostMapping("/students/{student_id}/status-check")
  public StatusCheck createStatusChecks(
      @PathVariable(name = "student_id") String studentId,
      @RequestBody CreateStatusCheck createStatusCheck) {
    return statusCheckMapper.toRest(
        statusCheckService.createStatusCheckByStudentId(studentId, createStatusCheck));
  }
}
