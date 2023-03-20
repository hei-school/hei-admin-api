package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.Courses;
import school.hei.haapi.repository.CoursesRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private final CoursesRepository coursesRepository;
    public List<Courses>  getCourses(int page, int pageSize){
        Pageable pageable = PageRequest.of(page, pageSize);
        return coursesRepository.findAll(pageable).toList();
    }
}
