package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.response.CoursesResponse;
import school.hei.haapi.endpoint.rest.response.CreateCourses;
import school.hei.haapi.service.CourseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;

    @GetMapping("/courses")
    public List<CoursesResponse> getAllCourses() {
        return courseService.getAllCourses()
                .stream()
                .map(courseMapper::responseToRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PutMapping("/courses")
    public List<CoursesResponse> updateOrCreateCourses(List<CreateCourses> toUpdate) {
        return courseService.updateOrCreateCourses(toUpdate
                    .stream()
                    .map(courseMapper::toDomain)
                    .collect(Collectors.toUnmodifiableList()))
                .stream()
                .map(courseMapper::responseToRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @GetMapping("/courses/code={code}")
    public Map<String, Object> getCourseInfo(@PathVariable String code) {
        Object[] result = courseService.findNameCreditsAndTeacherIdByCode(code);
        Map<String, Object> courseInfo = new HashMap<>();
        courseInfo.put("name", result[0]);
        courseInfo.put("credits", result[1]);
        courseInfo.put("teacher_id", result[2]);
        return courseInfo;
    }
}
