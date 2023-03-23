package school.hei.haapi.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository courseRepository;

  public List<Course> getAllCourses(PageFromOne page, BoundedPageSize pageSize){
    Pageable pageable = PageRequest.of((page == null ? 1 : page.getValue() - 1 ),
        pageSize == null ? 15 : pageSize.getValue());
    return courseRepository.findAll(pageable).stream().collect(Collectors.toUnmodifiableList());
  }
}
