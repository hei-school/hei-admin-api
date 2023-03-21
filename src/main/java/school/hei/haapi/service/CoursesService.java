package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Courses;
import school.hei.haapi.repository.CoursesRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CoursesService {
    private final CoursesRepository repository;

    public Courses getById(String courseId) { return repository.getById(courseId);}

    public List<Courses> getAll() { return repository.findAll();}

    public List<Courses> saveAll(List<Courses> courses) {
        return repository.saveAll(courses);
    }
}
