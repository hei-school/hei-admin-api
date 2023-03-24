package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.SortOrder;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @PutMapping("/students/{student_id}/courses")
    public List<Course> updateCoursesByStatus(@RequestBody List<UpdateStudentCourse> course, @PathVariable String student_id) {
        return List.of(courseMapper.toRestCourse(courseService.updateCourseStatus(student_id, course.get(0).getCourseId(), course.get(0).getStatus())));
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

    @GetMapping("/courses")
    public List<Course> getAll(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer credits,
            @RequestParam(required = false, name = "teacher_first_name") String teacherFirstName,
            @RequestParam(required = false, name = "teacher_last_name") String teacherLastName,
            @RequestParam(required = false, defaultValue = "ASC") SortOrder creditsOrder,
            @RequestParam(required = false, defaultValue = "ASC") SortOrder codeOrder,
            @RequestParam("page") int page,
            @RequestParam("page_size") int pageSize
    ) {
        List<school.hei.haapi.model.Course> courses = courseService.getAll(
                code,
                name,
                credits,
                teacherFirstName,
                teacherLastName,
                creditsOrder,
                codeOrder,
                page,
                pageSize
        );
        return courses.stream()
                .map(courseMapper::toRestCourse)
                .collect(Collectors.toUnmodifiableList());
    }
}

