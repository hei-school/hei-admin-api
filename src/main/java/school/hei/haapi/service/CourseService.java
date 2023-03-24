package school.hei.haapi.service;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.OrderDirection;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.dao.CourseManagerDao;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository repository;
  private final CourseManagerDao courseManagerDao;


  private final CourseManagerDao courseManagerDao;
  private List<Order> retrieveOrders(OrderDirection creditsOrder, OrderDirection codeOrder){
    List<Order> orderList = new ArrayList<>();
    if(creditsOrder != null){
      orderList.add(new Order(Direction.valueOf(creditsOrder.getValue()), "credits"));
    }
    if(codeOrder != null){
      orderList.add(new Order(Direction.valueOf(codeOrder.getValue()), "code"));
    }
    if(orderList.isEmpty()){
      orderList.add(new Order(Direction.ASC, "code"));
    }
    return orderList;
  }
 
  public List<Course> getAllCourses(String code, String name, Integer credits, String teacherFirstName, String teacherLastName,
                                    OrderDirection creditsOrder,
                                    OrderDirection codeOrder,
                                    PageFromOne page,
                                    BoundedPageSize pageSize) {
    List<Order> orders = retrieveOrders(creditsOrder, codeOrder);
    Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(), Sort.by(orders));
    return courseManagerDao.findCoursesByCriteria(code, name, credits, teacherFirstName, teacherLastName, pageable);
  }

  public Course getCourseById(String courseId) {
    return repository.getById(courseId);
  }

  public List<Course> crupdateCourses(List<Course> toCrupdate) {
    return repository.saveAll(toCrupdate);
  }
}
