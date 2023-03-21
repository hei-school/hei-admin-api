package school.hei.haapi.endpoint.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @PutMapping("/{id}")
    public Course updateOrCreateCourse(@PathVariable Long id, @RequestBody Course course) {
        if (course.getCode() == null || course.getCode().isEmpty()) {
            throw new InvalidRequestException("Course code cannot be empty");
        }
        Course existingCourse = courseRepository.findById(id).orElse(null);
        if (existingCourse == null) {
            // create a new course
            if (courseRepository.findByCode(course.getCode()).isPresent()) {
                throw new InvalidRequestException("Course with code " + course.getCode() + " already exists");
            }
            return courseRepository.save(course);
        } else {
            // update existing course
            if (!existingCourse.getCode().equals(course.getCode()) && courseRepository.findByCode(course.getCode()).isPresent()) {
                throw new InvalidRequestException("Course with code " + course.getCode() + " already exists");
            }
            existingCourse.setCode(course.getCode());
            existingCourse.setDescription(course.getDescription());
            if (course.getMainTeacherId() != null) {
                Teacher mainTeacher = teacherRepository.findById(course.getMainTeacherId()).orElseThrow(() -> new InvalidRequestException("Teacher with id " + course.getMainTeacherId() + " not found"));
                existingCourse.setMainTeacher(mainTeacher);
            }
            return courseRepository.save(existingCourse);
        }
    }

}