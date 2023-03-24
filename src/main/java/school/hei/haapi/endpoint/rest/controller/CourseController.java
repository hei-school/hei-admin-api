package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.response.CoursesResponse;
import school.hei.haapi.endpoint.rest.response.CreateCourses;
import school.hei.haapi.model.Course;
import school.hei.haapi.service.CourseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/courses")
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @GetMapping
    public List<CoursesResponse> getAllCourses() {
        return courseService.getAllCourses()
                .stream()
                .map(courseMapper::responseToRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping
    public List<CoursesResponse> updateOrCreateCourses(List<CreateCourses> toUpdate) {
        return courseService.updateOrCreateCourses(toUpdate
                    .stream()
                    .map(courseMapper::toDomain)
                    .collect(Collectors.toUnmodifiableList()))
                .stream()
                .map(courseMapper::responseToRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping
    @GetMapping("/courses")
    public List<CoursesResponse> getCoursesByFilter(@RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String code,
                                                    @RequestParam(required = false) Integer credits,
                                                    @RequestParam(required = false) String teacher_first_name,
                                                    @RequestParam(required = false) String teacher_last_name) {
        List<Course> courses;
        if (name != null) {
            courses = courseService.getCoursesByName(name);
        } else if (code != null) {
            courses = courseService.getCoursesByCode(code);
        } else if (credits != null) {
            courses = courseService.getCoursesByCredits(credits);
        } else if (teacher_first_name != null) {
            courses = courseService.getCoursesByMainTeacherFirstName(teacher_first_name);
        } else if (teacher_last_name != null) {
            courses = courseService.getCoursesByMainTeacherLastName(teacher_last_name);
        } else {
            courses = courseService.getAllCourses();
        }
        return courses.stream().map(courseMapper::responseToRest).collect(Collectors.toList());
    }

}
