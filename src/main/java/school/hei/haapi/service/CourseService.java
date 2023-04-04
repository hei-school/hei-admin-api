package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.Direction;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.dao.CourseDao;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
@Slf4j
public class CourseService {
  private final CourseDao courseDao;
  private final CourseRepository courseRepository;
  private final CourseValidator courseValidator;

  public List<Course> getCourses(
      String code, String name,
      Integer credits, Direction creditsOrder,
      Direction codeOrder, String teacherFirstName,
      String teacherLastName, PageFromOne page,
      BoundedPageSize pageSize
  ) {
    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(ASC, "name"));
    return courseDao.findByCriteria(code, name, credits,
        teacherFirstName, teacherLastName, String.valueOf(creditsOrder),
        String.valueOf(codeOrder), pageable);
  }

  @Transactional
  public List<Course> crupdateCourses(List<Course> toCrupdate) {
    courseValidator.accept(toCrupdate);
    return courseRepository.saveAll(toCrupdate);
  }

}
