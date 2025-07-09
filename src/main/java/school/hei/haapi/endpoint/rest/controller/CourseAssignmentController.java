package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.*;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseAssignmentMapper;
import school.hei.haapi.endpoint.rest.model.CourseAssignment;
import school.hei.haapi.endpoint.rest.model.CreateCourseAssignment;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.service.CourseAssignmentService;
import school.hei.haapi.service.UserService;

@RestController
@AllArgsConstructor
public class CourseAssignmentController {
  private final CourseAssignmentService service;
  private final CourseAssignmentMapper mapper;
  private final UserService userService;

  @GetMapping("/groups/{group_id}/course_assignments")
  public List<CourseAssignment> getByGroupId(
      @PathVariable("group_id") String groupId,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return service.getByGroupId(groupId, page, pageSize).stream()
        .map(mapper::toRest)
        .collect(toList());
  }

  @GetMapping("/groups/{group_id}/course_assignments/{course_assignment_id}")
  public CourseAssignment getById(
      @PathVariable("group_id") String groupId,
      @PathVariable("course_assignment_id") String CourseAssignmentId) {
    return mapper.toRest(service.getById(CourseAssignmentId, groupId));
  }

  @PutMapping("/groups/{group_id}/course_assignments")
  public List<CourseAssignment> createOrUpdateCourseAssignment(
      @PathVariable("group_id") String groupId,
      @RequestBody List<CreateCourseAssignment> CourseAssignments) {
    return service.createOrUpdateCourseAssignments(CourseAssignments).stream()
        .map(mapper::toRest)
        .collect(toList());
  }

  @GetMapping("/course_assignments")
  public List<CourseAssignment> getAllCourseAssignmentByCriteria(
      @RequestParam(value = "teacher_id", required = false) String teacherId,
      @RequestParam(value = "course_id", required = false) String courseId,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return service.getByCriteria(teacherId, courseId, page, pageSize).stream()
        .map(mapper::toRest)
        .collect(toList());
  }

  @GetMapping("/teachers/{teacher_id}/course_assignments")
  public List<CourseAssignment> getCourseAssignmentsByTeacherId(
      @PathVariable("teacher_id") String teacherId,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return service.getCourseAssignmentsByTeacherId(teacherId, page, pageSize).stream()
        .map(mapper::toRest)
        .toList();
  }

  @PutMapping("/teachers/{teacher_id}/course_assignments")
  public List<CourseAssignment> createOrUpdateCourseAssignmentByTeacherId(
      @PathVariable("teacher_id") String teacherId,
      @RequestBody List<CreateCourseAssignment> createCourseAssignments) {
    List<school.hei.haapi.model.CourseAssignment> courseAssignments =
        createCourseAssignments.stream()
            .map(mapper::toDomain)
            .toList();
    return service.createOrUpdateCourseAssignmentsByTeacherId(teacherId, courseAssignments)
            .stream()
            .map(mapper::toRest)
            .toList();
  }
}
