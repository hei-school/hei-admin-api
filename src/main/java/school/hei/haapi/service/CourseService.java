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

    public Object[] findNameCreditsAndTeacherIdByCode(String code) {
        return courseRepository.findNameCreditsAndTeacherIdByCode(code);
    }
}
