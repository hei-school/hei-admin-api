package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
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
    public List<Course> createOrUpdateCourses(@RequestBody List<CrupdateCourse> toWrite) {
        var saved = courseService.saveAll(toWrite.stream()
                .map(courseMapper::toDomain)
                .collect(toUnmodifiableList()));
        return saved.stream()
                .map(courseMapper::toRestCourse)
                .collect(toUnmodifiableList());
    }

    @PutMapping("/students/{studentId}/courses")
    public List<Course> createFees(
            @PathVariable String studentId, @RequestBody List<UpdateStudentCourse> toCreate) {

        return courseService.saveAllStudentCourses(studentId,courseMapper.toDomainStudentCourse(studentId, toCreate)).stream()
                .map(courseMapper::toRestCourse)
                .collect(Collectors.toList());
    }
    @GetMapping("/students/{studentId}/courses")
    public List<Course> getFeesByStudentAndStatus(
            @PathVariable String studentId, @RequestParam(required = false) StudentCourse.CourseStatus status) {
        return courseMapper.toRestCourse(courseService.getByStudentIdAndStatus(studentId,status));
    }

    @GetMapping("/courses")
    public List<Course> getCourses(
            @RequestParam(name = "page", defaultValue = "1", required = false)PageFromOne page,
            @RequestParam(name = "page_size", defaultValue = "15", required = false)BoundedPageSize pageSize,
            @RequestParam(name = "name", defaultValue = "", required = false)String name,
            @RequestParam(name = "code", defaultValue = "", required = false)String code,
            @RequestParam(name = "credits", defaultValue = "", required = false)Integer credits,
            @RequestParam(name = " teacher_first_name", required = false)String teacherFirstName,
            @RequestParam(name = "teacher_last_name", required = false)String teacherLastName,
            @RequestParam(name = "codeOrder", required = false) Sort.Direction codeOrder
    ){
        return courseService.getCourses(
                page,
                pageSize,
                name,
                code,
                credits,
                teacherFirstName,
                teacherLastName,
                codeOrder
                ).stream()
                .map(courseMapper::toRestCourse)
                .collect(Collectors.toList());
    }
}
