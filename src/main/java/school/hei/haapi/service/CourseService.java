package school.hei.haapi.service;

import java.util.ArrayList;
import java.util.Collection;
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
import school.hei.haapi.model.*;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseFollowed;
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

  public List<Course> getAllCourses(CourseFilter filter, PageFromOne page, BoundedPageSize pageSize){
    Pageable pageable = PageRequest.of((page == null ? 1 : page.getValue() - 1 ),
       pageSize == null ? 15 : pageSize.getValue());
    if (filter.getCode() != null && !filter.getCode().isEmpty()) {
      return courseRepository.findByCodeContainingIgnoreCase(filter.getCode(), pageable);
    } else if (filter.getName() != null && !filter.getName().isEmpty()) {
      return courseRepository.findByNameContainingIgnoreCase(filter.getName(), pageable);
    } else if (filter.getCredits() != null) {
      return courseRepository.findByCredits(filter.getCredits(), pageable);
    } else if (filter.getTeacherFirstName() != null && !filter.getTeacherFirstName().isEmpty() && filter.getTeacherLastName() != null && !filter.getTeacherLastName().isEmpty()) {
      return courseRepository.findByTeacherFirstNameContainingIgnoreCaseAndTeacherLastNameContainingIgnoreCase(filter.getTeacherFirstName(), filter.getTeacherLastName(), pageable);
    } else if (filter.getTeacherFirstName() != null && !filter.getTeacherFirstName().isEmpty()) {
      return courseRepository.findByTeacherFirstNameContainingIgnoreCase(filter.getTeacherFirstName(), pageable);
    } else if (filter.getTeacherLastName() != null && !filter.getTeacherLastName().isEmpty()) {
      return courseRepository.findByTeacherLastNameContainingIgnoreCase(filter.getTeacherLastName(), pageable);
    } else {
      return courseRepository.findAll(pageable).stream().collect(Collectors.toList());
    }
  };


  public List<CourseFollowed> updateStudentCourseLink(List<CourseFollowed> studentCourseToUpdate,
                                                      String studentId){
      return courseFollowedRepository.saveAll(studentCourseToUpdate);
  }

}
