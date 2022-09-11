package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository repository;

    public Course getById(String courseId) {
        return repository.getById(courseId);
    }

    public List<Course> getAll() {
        return repository.findAll();
    }

    public Course save(Course courses) {
        return repository.save(courses);
    }
}
