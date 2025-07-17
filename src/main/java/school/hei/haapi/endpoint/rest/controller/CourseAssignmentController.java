package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toList;

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
import school.hei.haapi.endpoint.rest.model.CrupdateCourseAssignment;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseAssignmentService;

@RestController
@AllArgsConstructor
public class CourseAssignmentController {
  private final CourseAssignmentService service;
  private final CourseAssignmentMapper mapper;

  @GetMapping("/courses/{course_id}/course_assignments")
  public List<CourseAssignment> getCourseAssignmentsByCourseId(
      @PathVariable("course_id") String courseId,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return service.getByCourseId(courseId, page, pageSize).stream()
        .map(mapper::toRest)
        .collect(toList());
  }

  @GetMapping("/groups/{group_id}/course_assignments")
  public List<CourseAssignment> getCourseAssignmentsByGroupId(
      @PathVariable("group_id") String groupId,
      @RequestParam(value = "page", defaultValue = "1") PageFromOne page,
      @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize) {
    return service.getByGroupId(groupId, page, pageSize).stream()
        .map(mapper::toRest)
        .collect(toList());
  }

  @GetMapping("/course_assignments")
  public List<CourseAssignment> getCourseAssignmentsByCriteria(
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
    return service.getByTeacherId(teacherId, page, pageSize).stream().map(mapper::toRest).toList();
  }

  @PutMapping("/course_assignments")
  public List<CourseAssignment> createOrUpdateCourseAssignments(
      @RequestBody List<CrupdateCourseAssignment> crupdateCourseAssignments) {
    return service
        .crupdateCourseAssignments(
            crupdateCourseAssignments.stream().map(mapper::toDomain).toList())
        .stream()
        .map(mapper::toRest)
        .toList();
  }

  @PutMapping("/courses/{course_id}/course_assignments")
  public List<CourseAssignment> createOrUpdateCourseAssignmentsByCourseId(
      @PathVariable("course_id") String courseId,
      @RequestBody List<CrupdateCourseAssignment> crupdateCourseAssignments) {
    return service
        .crupdateCourseAssignments(
            crupdateCourseAssignments.stream()
                .map(
                    crupdateCourseAssignment -> mapper.toDomain(courseId, crupdateCourseAssignment))
                .toList())
        .stream()
        .map(mapper::toRest)
        .toList();
  }
}
