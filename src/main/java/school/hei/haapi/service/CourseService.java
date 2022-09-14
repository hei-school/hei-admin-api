package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseValidator courseValidator;
    private final CourseRepository courseRepository;

    public Course getById(String courseId){
        return courseRepository.getById(courseId);
    }

    public List<Course> getAll(){
        return courseRepository.findAll();
    }

    public Course save(Course course){
        courseValidator.accept(course);
        return courseRepository.save(course);
    }
}
