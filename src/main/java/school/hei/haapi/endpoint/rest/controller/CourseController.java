package school.hei.haapi.endpoint.rest.controller;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.*;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;
import school.hei.haapi.service.UserService;

import javax.transaction.Transactional;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService service;
    private final CourseMapper mapper;
    private final UserService userService;

    @GetMapping("/courses")
    public List<Course> getCourses(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "credits", required = false) Integer credits,
            @RequestParam(value = "teacher_first_name", required = false)
            String teacherFirstName,
            @RequestParam(value = "teacher_last_name", required = false)
            String teacherLastName,
            @RequestParam(value = "credits_order", required = false) CourseDirection creditsOrder,
            @RequestParam(value = "code_order", required = false) CourseDirection codeOrder,
            @RequestParam(defaultValue = "1") PageFromOne page,
            @RequestParam(value = "page_size", defaultValue = "15") BoundedPageSize pageSize
    ) {
        return service.getCourses(code, name, credits, creditsOrder, codeOrder, teacherFirstName,
                        teacherLastName, page, pageSize).stream()
                .map(mapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping("/courses")
    public List<Course> crupdateCourses(@RequestBody List<CrupdateCourse> courses) {
        HashMap<String, school.hei.haapi.model.User> teachers = new HashMap<String, school.hei.haapi.model.User>();
        return service.crupdateCourses(courses.stream()
                        .map(course -> {
                            if (teachers.get(course.getMainTeacherId()) == null) {
                                return mapper.toDomain(course, userService.getById(course.getMainTeacherId()));
                            }
                            return mapper.toDomain(course, teachers.get(course.getMainTeacherId()));
                        })
                        .collect(Collectors.toUnmodifiableList())
                ).stream()
                .map(mapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping("students/{student_id}/courses")
    public List<Course> getStudentCoursesById(
            @PathVariable("student_id") String studentId,
            @RequestParam(value = "status", required = false) CourseStatus status) {
        return service.getCoursesByStatus(studentId, status).stream()
                .map(mapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping("students/{student_id}/courses")
    public List<Course> updateStudentCourses(
            @PathVariable("student_id") String studentId,
            @RequestBody List<UpdateStudentCourse> studentCourses) {
        return service.updateStudentCourses(studentId, studentCourses).stream()
                .map(mapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

}
