package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;
import java.util.Optional;

public class CoursesController {
    @RestController
    @AllArgsConstructor
    public class CourseController {

        @Autowired
        private CourseRepository courseRepository;

        @PutMapping("/{id}")
        public ResponseEntity<Course> updateCourse(@PathVariable String id, @RequestBody Course course) {
            Optional<Course> optionalCourse = courseRepository.findById(id);
            if (optionalCourse.isPresent()) {
                Course existingCourse = optionalCourse.get();
                existingCourse.setCode(course.getCode());
                existingCourse.setName(course.getName());
                existingCourse.setCredits(course.getCredits());
                existingCourse.setTotal_hours(course.getTotal_hours());
                existingCourse.setMain_teacher(course.getMain_teacher());
                Course updatedCourse = courseRepository.save(existingCourse);
                return ResponseEntity.ok(updatedCourse);
            } else {
                return ResponseEntity.notFound().build();
            }
        }
    }


}
