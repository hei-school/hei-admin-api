package school.hei.haapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.OrderType;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseFollowed;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.CourseFollowedRest;
import school.hei.haapi.repository.CourseFollowedRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.dao.CourseManagerDao;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseFollowedRepository courseFollowedRepository;
  private final CourseMapper courseMapper;
  private final CourseRepository courseRepository;

  private final CourseManagerDao courseManagerDao;

  public List<CourseFollowed> getCourseFollowedByOneStudent(String studentId,
                                                            CourseStatus status) {
    return courseFollowedRepository.findAllByStudentIdAndStatus(studentId, status);
  }

  public List<Course> crupdateCourse(List<CrupdateCourse> toCrupdate){
    List<Course> toDomain = toCrupdate.stream().map(
            courseMapper::toDomain
    ).collect(Collectors.toUnmodifiableList());
    return courseRepository.saveAll(toDomain);
  }

  public List<Course> getAllCourses(PageFromOne page, BoundedPageSize pageSize, String code, String name, Integer credits, String  teacher_first_name, String teacher_last_name, OrderType creditsOrder, OrderType codeOrder){

    List<Sort.Order> orders = new ArrayList<>();
    if (creditsOrder != null) {
      orders.add(new Sort.Order(Sort.Direction.valueOf(creditsOrder.getValue()), "credits"));
    }
    if(codeOrder != null){
      orders.add(new Sort.Order(Sort.Direction.valueOf(codeOrder.getValue()), "code"));
    }

    Pageable pageable = PageRequest.of((page == null ? 1 : page.getValue() - 1 ),
       pageSize == null ? 15 : pageSize.getValue() , Sort.by(orders));

    return courseManagerDao.getCourseByCriteria(code, name, credits, teacher_first_name, teacher_last_name, pageable);
  }


  public List<CourseFollowed> updateStudentCourseLink(List<CourseFollowed> studentCourseToUpdate,
                                                      String studentId){
      return courseFollowedRepository.saveAll(studentCourseToUpdate);
  }

}
