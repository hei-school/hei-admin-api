package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @GetMapping(value = "/courses")
    public List<Course> getAllCourses(
            @RequestParam(name = "page", required = false, defaultValue = "1") PageFromOne page,
            @RequestParam(name = "page_size", required = false, defaultValue = "15") BoundedPageSize pageSize,
            @RequestParam(name = "code", required = false, defaultValue = "") String code,
            @RequestParam(name = "name", required = false, defaultValue = "") String name,
            @RequestParam(name = "credits", required = false) Integer credits,
            @RequestParam(name = "teacher_first_name", required = false) String teacherFirstName,
            @RequestParam(name = "teacher_last_name", required = false) String teacherLastName,
            @RequestParam(name = "codeOrder", required = false) String codeOrder,
            @RequestParam(name = "creditsOrder", required = false) String creditsOrder) {
        return courseService
                .getAllByCriteria(page, pageSize, code, name, credits, teacherFirstName, teacherLastName, codeOrder, creditsOrder)
                .stream()
                .map(courseMapper::toRestCourse)
                .collect(Collectors.toUnmodifiableList());
    }
}
