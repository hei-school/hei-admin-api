package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.Comparator;
import java.util.List;

@RestController
@AllArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/courses")
    public List<Course> getAllCourseBy(
            @RequestParam(name = "page",required = false) PageFromOne page,
            @RequestParam(name = "page_size",required = false) BoundedPageSize pageSize,
            @RequestParam(name = "teacher_first_name", required = false) String teacherFirstName,
            @RequestParam(name = "teacher_last_name",required = false) String teacherLastName,
            @RequestParam(name = "code", required = false)String code,
            @RequestParam(name ="name", required = false)String name,
            @RequestParam(name = "credits",required = false)Integer credits,
            @RequestParam(value = "creditsOrder", required = false) String creditsOrder,
            @RequestParam(value = "codeOrder", required = false) String codeOrder
    ) {

        List<Course> courseList = courseService.getAllCoursesBy(page,pageSize,teacherFirstName,teacherLastName,code,name,credits);

        // Tri par ordre décroissant ou croissant des crédits
        if (creditsOrder != null && !creditsOrder.isEmpty()) {
            if (creditsOrder.equals("ASC")) {
                courseList.sort(Comparator.comparingInt(Course::getCredits));
            } else if (creditsOrder.equals("DESC")) {
                courseList.sort(Comparator.comparingInt(Course::getCredits).reversed());
            }
        }

        // Tri par ordre alphabétique croissant ou décroissant du code
        if (codeOrder != null && !codeOrder.isEmpty()) {
            if (codeOrder.equals("ASC")) {
                courseList.sort(Comparator.comparing(Course::getCode));
            } else if (codeOrder.equals("DESC")) {
                courseList.sort(Comparator.comparing(Course::getCode).reversed());
            }
        }
        return courseList;
    }
}