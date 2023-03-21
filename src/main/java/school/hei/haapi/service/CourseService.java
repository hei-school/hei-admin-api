package school.hei.haapi.service;

import static org.springframework.data.domain.Sort.Direction.ASC;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.data.couchbase.CouchbaseReactiveDataAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository repository;

  public List<Course> getCourses(PageFromOne page, BoundedPageSize pageSize){
    Pageable pageable = PageRequest.of(
            page.getValue() - 1, pageSize.getValue());
    return repository.findAll(pageable).getContent();
  }

  public List<Course> crupdateCourses(List<Course> courses){
    return repository.saveAll(courses);
  }
}