package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.event.model.TypedUserUpserted;
import school.hei.haapi.endpoint.event.model.gen.UserUpserted;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

import static java.util.stream.Collectors.toUnmodifiableList;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public Course getById(String courseId) {
        return courseRepository.getById(courseId);
    }
    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    public List<Course> getAllByName(String name) {return courseRepository.findByNameIgnoreCase(name);}

    public List<Course> saveAll(List<Course> courses) {
        return courseRepository.saveAll(courses);
    }
}
