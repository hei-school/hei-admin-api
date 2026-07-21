package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.dto.CourseDto;
import school.hei.haapi.model.dto.GroupFlowPeriod;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.model.validator.CourseAssignmentValidator;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.CourseAssignmentDao;

@Slf4j
@Service
@AllArgsConstructor
public class CourseAssignmentService {
  private final UserRepository userRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final CourseAssignmentValidator courseAssignmentValidator;
  private final CourseAssignmentDao courseAssignmentDao;
  private final PaginationFromPageAndPageSize pageableFromPageAndSize;

  public Optional<CourseAssignment> findById(String courseAssignmentId) {
    return courseAssignmentRepository.findById(courseAssignmentId);
  }

  public List<CourseAssignment> getByGroupId(
      String groupId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return courseAssignmentRepository.findAllByGroupId(groupId, pageable);
  }

  @Transactional
  public List<CourseAssignment> getByGroupId(String groupId) {
    return courseAssignmentRepository.findAllByGroupId(groupId);
  }

  public CourseAssignment getById(String id) {
    return courseAssignmentRepository.getById(id);
  }

  @Transactional
  public List<CourseAssignment> crupdateCourseAssignments(
      List<CourseAssignment> courseAssignments) {
    return courseAssignments.stream().map(this::crupdateCourseAssignment).toList();
  }

  public CourseAssignment crupdateCourseAssignment(CourseAssignment courseAssignment) {
    courseAssignmentValidator.accept(courseAssignment);
    return courseAssignmentRepository.save(courseAssignment);
  }

  public boolean checkTeacherOfCourseAssignment(String teacherId, String courseAssignmentId) {
    CourseAssignment courseAssignment = getById(courseAssignmentId);
    return teacherId.equals(courseAssignment.getMainTeacher().getId());
  }

  public List<CourseAssignment> getByCriteria(
      String teacherId, String courseId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = pageableFromPageAndSize.apply(page, pageSize);
    return courseAssignmentDao.findByCriteria(teacherId, courseId, null, pageable);
  }

  public List<CourseAssignment> getByTeacherId(
      String teacherId, PageFromOne page, BoundedPageSize pageSize) {
    var teacher = userRepository.findById(teacherId);
    if (teacher.isEmpty()) {
      throw new NotFoundException("Teacher with id: " + teacherId + " not found");
    }
    Pageable pageable = pageableFromPageAndSize.apply(page, pageSize);
    return courseAssignmentRepository.findAllByMainTeacher(teacher.get(), pageable);
  }

  public CourseAssignment getCourseAssignmentById(String courseAssignmentId) {
    return findById(courseAssignmentId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Course assignment with id: " + courseAssignmentId + " not found"));
  }

  public List<CourseAssignment> getByCourseId(
      String courseId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = pageableFromPageAndSize.apply(page, pageSize);
    return courseAssignmentRepository.findAllByCourseId(courseId, pageable);
  }

  public List<CourseDto> getGroupsCourseAssignmentsByGroupFlowsAtLevel(
      List<GroupFlowPeriod> studentLatestGroupFlows, StudentLevel level) {

    var courseAssignments =
        studentLatestGroupFlows.stream()
            .flatMap(
                groupFlowPeriod ->
                    getGroupCourseAssignmentsByLevelBetweenPeriod(groupFlowPeriod, level).stream())
            .collect(Collectors.groupingBy(CourseAssignment::getCourse));

    return courseAssignments.entrySet().stream()
        .map(entry -> new CourseDto(entry.getKey(), entry.getValue()))
        .toList();
  }

  @Transactional
  private List<CourseAssignment> getGroupCourseAssignmentsByLevelBetweenPeriod(
      GroupFlowPeriod groupFlowPeriod, StudentLevel level) {
    return getByGroupId(groupFlowPeriod.group().getId()).stream()
        .filter(courseAssignment -> level.equals(courseAssignment.getCourse().getStudentLevel()))
        .filter(courseAssignment -> hasExamOrAssignedBeforeLeave(courseAssignment, groupFlowPeriod))
        .toList();
  }

  private boolean hasExamOrAssignedBeforeLeave(
      CourseAssignment courseAssignment, GroupFlowPeriod groupFlowPeriod) {
    var courseExams = courseAssignment.getExams();
    if (courseExams.isEmpty() && groupFlowPeriod.end() == null) {
      return true;
    }
    return courseExams.stream()
        .anyMatch(courseExam -> groupFlowPeriod.contains(courseExam.getExaminationDate()));
  }
}
