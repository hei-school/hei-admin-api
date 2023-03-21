package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final UserService userService;
    public List<Course> getAll(PageFromOne page, BoundedPageSize pageSize){
        Pageable pageable =
                PageRequest.of(page.getValue() - 1, pageSize.getValue());
        return repository.findAll(pageable).toList();
    }

  public List<Course> saveAll(List<Course> courses) {
    return repository.saveAll(courses);
  }
}
