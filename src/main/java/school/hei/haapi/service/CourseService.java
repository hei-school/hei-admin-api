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

    public Course getCourseByStudentId(String studentId) {return repository.getById(studentId);}

    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public List<Course> saveAllCourses(List<Course> courses) {return repository.saveAll(courses);}

}
