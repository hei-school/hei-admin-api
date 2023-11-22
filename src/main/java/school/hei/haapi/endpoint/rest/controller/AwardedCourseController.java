package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.AwardedCourseMapper;
import school.hei.haapi.endpoint.rest.model.AwardedCourse;
import school.hei.haapi.endpoint.rest.model.CreateAwardedCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.AwardedCourseService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class AwardedCourseController {
  private final AwardedCourseService service;
  private final AwardedCourseMapper mapper;

  @GetMapping("/groups/{group_id}/awarded_courses")
  public List<AwardedCourse> getByGroupId(@PathVariable("group_id") String groupId,
                                          @RequestParam(value = "page", defaultValue = "1")
                                          PageFromOne page,
                                          @RequestParam(value = "page_size", defaultValue = "15")
                                          BoundedPageSize pageSize) {
    return service.getByGroupId(groupId, page, pageSize).stream()
        .map(mapper::toRest)
        .collect(Collectors.toList());
  }

  @GetMapping("/groups/{group_id}/awarded_courses/{awarded_course_id}")
  public AwardedCourse getById(@PathVariable("group_id") String groupId,
                               @PathVariable("awarded_course_id") String awardedCourseId) {
    return mapper.toRest(service.getById(awardedCourseId, groupId));
  }

  @PutMapping("/groups/{group_id}/awarded_courses")
  public List<AwardedCourse> createOrUpdateAwardedCourse(@PathVariable("group_id") String groupId,
                                                         @RequestBody
                                                         List<CreateAwardedCourse> awardedCourses) {
    return service.createOrUpdateAwardedCourses(awardedCourses).stream()
        .map(mapper::toRest)
        .collect(Collectors.toList());
  }

  @GetMapping("/awarded_courses")
  public List<AwardedCourse> getByCriteria(@RequestParam(value = "teacher_id", required = false)
                                             String teacherId,
                                           @RequestParam(value = "course_id", required = false)
                                           String courseId,
                                           @RequestParam(value = "page", defaultValue = "1")
                                             PageFromOne page,
                                           @RequestParam(value = "page_size", defaultValue = "15")
                                             BoundedPageSize pageSize) {
    return service.getByCriteria(teacherId, courseId, page, pageSize).stream()
            .map(mapper::toRest)
            .collect(Collectors.toList());
  }
}
