package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CourseService;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@RestController
@AllArgsConstructor
public class CourseController {
    private CourseMapper courseMapper;
    private CourseService service;

    @GetMapping("/courses")
    public List<Course> getAllCourses(){
                return service.getAllCourses().stream()
                        .map(courseMapper::toRest)
                        .collect(toUnmodifiableList());
    }
    @PutMapping("/courses")
    public List<Course> saveAllCourses(@RequestBody List<Course> toCreate) {
        return service
                .saveAllCourses(toCreate
                        .stream()
                        .map(courseMapper::toDomain)
                        .collect(toUnmodifiableList()))
                .stream()
                .map(courseMapper::toRest)
                .collect(toUnmodifiableList());
    }

    @GetMapping("/students/{student_id}/courses")
    public List<Course> getCourseByStudentId(
            @PathVariable String studentId,
            @PathVariable PageFromOne page,
            @PathVariable Course.CourseStatus status,
            @RequestParam("page_size") BoundedPageSize pageSize){
        return service.getCourseByStudentId(studentId, page, pageSize, status).stream()
                .map(courseMapper::toRest)
                .collect(toUnmodifiableList());
    }

//    @PutMapping("/students/{student_id}/courses")
//    public List<Fee> createCourse(
//            @PathVariable String studentId,
//            @RequestBody List<CreateCourse> toCreate) {
//        return service.saveAll(
//                        courseMapper.toDomainC(studentId, toCreate)).stream()
//                .map(courseMapper::toRest)
//                .collect(toUnmodifiableList());
//    }

}