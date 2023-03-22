package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.endpoint.rest.model.Course;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;

import java.util.stream.Collectors;


@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @PutMapping("/students/{student_id}/courses")
    public Course updateCoursesByStatus(@RequestBody List<UpdateStudentCourse> course, @PathVariable String student_id, String status) {
        return courseMapper.toRestCourse(courseService.updateCourseStatus(student_id, course.get(0).getCourseId(), course.get(0).getStatus().toString()));
    }

    @GetMapping(value = "/courses")
    public List<Course> getAllCourses(
            @RequestParam("page") PageFromOne page,
            @RequestParam("page_size") BoundedPageSize pageSize) {
        return courseService.getAll(page, pageSize).stream()
                .map(courseMapper::toRestCourse)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping(value = "/courses")
    public List<Course> createOrUpdateCourses(@RequestBody List<CrupdateCourse> toWrite) {
        var saved = courseService.saveAll(toWrite.stream()
                .map(courseMapper::toDomainCrupdateCourse)
                .collect(Collectors.toUnmodifiableList()));
        return saved.stream()
                .map(courseMapper::toRestCourse)
                .collect(Collectors.toUnmodifiableList());
    }
}
