package school.hei.haapi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCourse;
import school.hei.haapi.endpoint.rest.model.UpdateStudentCourse;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseFollowed;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.CourseFollowedRest;
import school.hei.haapi.repository.CourseFollowedRepository;
import school.hei.haapi.repository.CourseRepository;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseFollowedRepository courseFollowedRepository;
  private final CourseMapper courseMapper;
  private final CourseRepository courseRepository;

  public List<CourseFollowed> getCourseFollowedByOneStudent(String studentId,
                                                            CourseStatus status) {
    return courseFollowedRepository.findAllByStudentIdAndStatus(studentId, status);
  }

  public List<Course> crupdateCourse(List<CrupdateCourse> toCrupdate){
    List<Course> toDomain = toCrupdate.stream().map(
        course -> courseMapper.toDomain(course)
    ).collect(Collectors.toUnmodifiableList());
    return courseRepository.saveAll(toDomain);
  }

  public List<Course> getAllCourses(PageFromOne page, BoundedPageSize pageSize){
    Pageable pageable = PageRequest.of((page == null ? 1 : page.getValue() - 1 ),
       pageSize == null ? 15 : pageSize.getValue());
    return courseRepository.findAll(pageable).stream().collect(Collectors.toUnmodifiableList());
  }


  public List<CourseFollowed> updateStudentCourseLink(List<CourseFollowed> studentCourseToUpdate,
                                                      String studentId){
      return courseFollowedRepository.saveAll(studentCourseToUpdate);
  }

}
