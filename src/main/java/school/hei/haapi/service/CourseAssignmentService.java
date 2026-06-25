package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.mapper.CourseAssignmentMapper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
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
  private final CourseAssignmentMapper courseAssignmentMapper;
  private final CourseAssignmentValidator courseAssignmentValidator;
  private final CourseAssignmentDao courseAssignmentDao;
  private final PaginationFromPageAndPageSize pageableFromPageAndSize;

  public Optional<CourseAssignment> findById(String courseAssignmentId) {
    return courseAssignmentRepository.findById(courseAssignmentId);
  }

  public List<CourseAssignment> getByStudentId(String userId) {
    User student = userRepository.getById(userId);
    /*
     *   TODO: Optimize heavy db call
     */
    List<Group> groups =
        student.getGroupFlows().stream().map(GroupFlow::getGroup).distinct().toList();
    return courseAssignmentMapper.toDomainCourseAssignmentsByGroups(groups);
  }

  public List<CourseAssignment> getByGroupId(
      String groupId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return courseAssignmentRepository.findAllByGroupId(groupId, pageable);
  }

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
}
