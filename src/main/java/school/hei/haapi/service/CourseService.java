package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CourseService {
    private CourseRepository repository;
    private CourseMapper mapper;

    public List<Course> updateCourse(int id, List<Course> courses){
        Optional<Course> toUpdate= repository.findById(String.valueOf(id));
        if (toUpdate.isPresent()){
            repository.saveAll(toUpdate.stream()
                    .map(mapper::ToDomain)
                    .collect(Collectors.toUnmodifiableList()));
        }else {
            repository.saveAll(courses);
        }
        return courses;
    }

}