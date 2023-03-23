package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.Course;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CreateFee;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.Fee;
import school.hei.haapi.endpoint.rest.model.Group;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.service.CourseService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @PutMapping(value = "/courses")
    public List<Course> crupdateCourses(@RequestBody List<CrupdateCourse> toWrite) {
        var saved = courseService.saveAll(toWrite.stream()
                .map(courseMapper::toDomain)
                .collect(toUnmodifiableList()));
        return saved.stream()
                .map(courseMapper::toRestCourse)
                .collect(toUnmodifiableList());
    }

    @PutMapping("/students/{studentId}/courses")
    public List<Course> createOrUpdateCourses(
            @PathVariable String studentId, @RequestBody List<UpdateStudentCourse> toCreate) {

        return courseService.saveAllStudentCourses(studentId,courseMapper.toDomainStudentCourse(studentId, toCreate)).stream()
                        .map(courseMapper::toRestCourse)
                .collect(Collectors.toList());
    }
    @GetMapping("/students/{studentId}/courses")
    public List<Course> getCoursesByStudentAndStatus(
            @PathVariable String studentId, @RequestParam(required = false) StudentCourse.CourseStatus status) {
        return courseMapper.toRestCourse(courseService.getByStudentIdAndStatus(studentId,status));
    }

    @GetMapping("/courses")
    public List<Course> getCourses(

            @RequestParam(name = "code", required = false, defaultValue = "")String code,
            @RequestParam(name = "name", required = false, defaultValue = "")String name,
            @RequestParam(name = "credits", required = false, defaultValue = "")Integer credits,
            @RequestParam(name = "teacher_first_name", required = false, defaultValue = "")String teacher_first_name,
            @RequestParam(name = "teacher_last_name", required = false, defaultValue = "")String teacher_last_name,
            @RequestParam(name = "creditsOrder", required = false, defaultValue = "")String creditsOrder,
            @RequestParam(name = "codeOrder", required = false, defaultValue = "")String codeOrder,
            @RequestParam(name = "page", required = false)PageFromOne page,
            @RequestParam(name = "page_size", required = false)BoundedPageSize pageSize
            ){
        return courseService.getCourses(page, pageSize).stream()
                .map(courseMapper::toRestCourse)
                .collect(Collectors.toList());
    }
}
