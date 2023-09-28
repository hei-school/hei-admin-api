package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.AwardedCourseMapper;
import school.hei.haapi.endpoint.rest.model.AwardedCourse;
import school.hei.haapi.endpoint.rest.model.CreateAwardedCourse;
import school.hei.haapi.service.AwardedCourseService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class AwardedCourseController {
    private final AwardedCourseService service;
    private final AwardedCourseMapper mapper;

    @GetMapping("/courses/{course_id}/awarded_course")
    public List<AwardedCourse> getByCourseId(@PathVariable("course_id") String courseId) {
        return service.getByCourseId(courseId).stream().map(mapper::toRest).collect(Collectors.toList());
    }

    @GetMapping("/students/{student_id}/awarded_course")
    public List<AwardedCourse> getByStudentId(@PathVariable("student_id") String studentId) {
        return service.getByStudentId(studentId).stream().map(mapper::toRest).collect(Collectors.toList());
    }

    @GetMapping("/groups/{group_id}/awarded_courses")
    public List<AwardedCourse> getByGroupId(@PathVariable("group_id") String groupId) {
        return service.getByGroupId(groupId).stream().map(mapper::toRest).collect(Collectors.toList());
    }

    @GetMapping("/groups/{group_id}/awarded_courses/{awarded_course_id}")
    public AwardedCourse getById(
            @PathVariable("group_id") String groupId,
            @PathVariable("awarded_course_id") String awardedCourseId
    ) {
        return mapper.toRest(service.getById(awardedCourseId, groupId));
    }
    @PutMapping("/groups/{group_id}/awarded_course")
    public List<AwardedCourse> createOrUpdateAwardedCourse (
            @PathVariable("group_id") String groupId,
            @RequestBody List<CreateAwardedCourse> courses
    ) {
        return service.createOrUpdateAwardedCourses(courses).stream().map(mapper::toRest).collect(Collectors.toList());
    }
}
