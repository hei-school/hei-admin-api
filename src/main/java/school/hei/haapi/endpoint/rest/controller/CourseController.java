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
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.service.CourseService;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;



    @GetMapping("/courses")
    public List<Course> getCourses(
            @RequestParam(name = "code", required = false, defaultValue = "")String code,
            @RequestParam(name = "name", required = false, defaultValue = "")String name,
            @RequestParam(name = "credits", required = false, defaultValue = "0")Integer credits,
            @RequestParam(name = "teacher_first_name", required = false, defaultValue = "")String teacher_first_name,
            @RequestParam(name = "teacher_last_name", required = false, defaultValue = "")String teacher_last_name,
            @RequestParam(name = "creditsOrder", required = false, defaultValue = "")String creditsOrder,
            @RequestParam(name = "codeOrder", required = false, defaultValue = "")String codeOrder,
            @RequestParam(name = "page", required = false)PageFromOne page,
            @RequestParam(name = "page_size", required = false)BoundedPageSize pageSize
    ){
        return courseService.getCourses(code, name, credits, teacher_first_name, teacher_last_name, page, pageSize).stream()
                .map(courseMapper::toRestCourse)
                .collect(Collectors.toList());
    }


    @PutMapping(value = "/courses")
    public List<Course> createOrUpdateCourses(@RequestBody List<school.hei.haapi.model.Course> toWrite) {
        var saved = courseService.saveAll(toWrite);
        return saved.stream()
                .map(courseMapper::toRestCourse)
                .collect(toUnmodifiableList());
    }
    
    @GetMapping("/students/{studentId}/courses")
    public List<Course> getFeesByStudentAndStatus(
            @PathVariable String studentId, @RequestParam(required = false) StudentCourse.CourseStatus status) {
        return courseMapper.toRestCourse(courseService.getByStudentIdAndStatus(studentId,status));
    }

    @PutMapping("/students/{studentId}/courses")
    public List<Course> createFees(
            @PathVariable String studentId, @RequestBody List<UpdateStudentCourse> toCreate) {

        return courseService.saveAllStudentCourses(studentId,courseMapper.toDomainStudentCourse(studentId, toCreate)).stream()
                .map(courseMapper::toRestCourse)
                .collect(Collectors.toList());
    }



}
