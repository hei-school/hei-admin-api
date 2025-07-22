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
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.validator.CourseAssignmentValidator;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.CourseAssignmentDAO;

@Slf4j
@Service
@AllArgsConstructor
public class CourseAssignmentService {
  private final UserRepository userRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final CourseAssignmentMapper courseAssignmentMapper;
  private final CourseAssignmentValidator courseAssignmentValidator;
  private final CourseAssignmentDAO courseAssignmentDAO;

  public Optional<CourseAssignment> getById(String courseAssignmentId) {
    return courseAssignmentRepository.findById(courseAssignmentId);
  }

  public List<CourseAssignment> getByStudentId(String userId) {
    User student = userRepository.getById(userId);
    List<Group> groups =
        student.getGroupFlows().stream().map(groupFlow -> groupFlow.getGroup()).distinct().toList();
    return courseAssignmentMapper.toDomainCourseAssignmentsByGroups(groups);
  }

  public List<CourseAssignment> getByGroupId(
      String groupId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable =
        PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(DESC, "creationDatetime"));
    return courseAssignmentRepository.findAllByGroupId(groupId, pageable);
  }

  public CourseAssignment getById(String id, String groupId) {
    return courseAssignmentRepository.getByIdAndGroupId(id, groupId);
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

  public boolean checkTeacherOfCourseAssignment(
      String teacherId, String courseAssignmentId, String groupId) {
    CourseAssignment courseAssignment = getById(courseAssignmentId, groupId);
    return teacherId.equals(courseAssignment.getMainTeacher().getId());
  }

  public List<CourseAssignment> getByCriteria(
      String teacherId, String courseId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return courseAssignmentDAO.findByCriteria(teacherId, courseId, pageable);
  }

  public List<CourseAssignment> getByTeacherId(
      String teacherId, PageFromOne page, BoundedPageSize pageSize) {
    var teacher = userRepository.findById(teacherId);
    if (teacher.isEmpty()) {
      throw new NotFoundException("Teacher with id: " + teacherId + " not found");
    }
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return courseAssignmentRepository.findAllByMainTeacher(teacher.get(), pageable);
  }

  @Transactional
  public List<CourseAssignment> crupdateCourseAssignmentsByTeacherId(
      String teacherId, List<CourseAssignment> courseAssignments) {
    courseAssignmentValidator.accept(courseAssignments);
    return courseAssignmentRepository.saveAll(courseAssignments);
  }

  public CourseAssignment findCourseAssignmentById(String courseAssignmentId) {
    return courseAssignmentRepository
        .findById(courseAssignmentId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    "Course assignment with id: " + courseAssignmentId + " not found"));
  }

  public List<CourseAssignment> getByCourseId(
      String courseId, PageFromOne page, BoundedPageSize pageSize) {
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue());
    return courseAssignmentRepository.findAllByCourseId(courseId, pageable);
  }
}
