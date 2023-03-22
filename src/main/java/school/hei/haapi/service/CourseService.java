package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.Order;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.dao.CourseDao;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseDao repository;

  public List<Course> getCourses(String code, String name, Integer credits, String teacherFirstname,
                                 String teacherLastname, PageFromOne page, BoundedPageSize pageSize,
                                 Order creditsOrder, Order codeOrder) {
    return repository.getCoursesBycriteria(code, name, credits, teacherFirstname,
        teacherLastname, page,
        pageSize, creditsOrder, codeOrder);
  }

}
