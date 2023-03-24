package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> updateOrCreateCourses(List<Course> toUpdate) {
        return courseRepository.saveAll(toUpdate);
    }

    public List<Course> getCoursesByCode(String code) {
        return courseRepository.findByCodeContainingIgnoreCase(code);
    }

    public List<Course> getCoursesByName(String name) {
        return courseRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Course> getCoursesByCredits(int credits) {
        return courseRepository.findAllByCredits(credits);
    }

}
