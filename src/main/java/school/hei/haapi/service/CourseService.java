package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.model.*;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.repository.CourseRepository;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.contains;
import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository repository;
  private final CourseValidator validator;

  public List<Course> getCourses(PageFromOne page,
                                 BoundedPageSize pageSize,
                                 String code,
                                 String name,
                                 Integer credits,
                                 String teacher_first_name,
                                 String teacher_last_name) {

    Pageable pageable = PageRequest.of(
        page.getValue() - 1,
        pageSize.getValue(),
        Sort.by(ASC, "code"));

    return repository.findAllFiltered(code,name,credits,teacher_first_name,teacher_last_name,pageable);
  }

  public List<Course> getUnlinkedCoursesByStudentId(String studentId) {
    return repository.findCoursesByStudentIdAndStatusOrUnlinked(studentId,
        StudentCourse.CourseStatus.UNLINKED);
  }

  public List<Course> getLinkedCoursesByStudentId(String studentId) {
    return repository.findCoursesByStudentIdAndStatusLinked(studentId,
        StudentCourse.CourseStatus.LINKED);
  }

  public Course getById(String courseId) {
    return repository.findCourseById(courseId);
  }

  public Course getByCode(String code) {
    return repository.findCourseByCode(code);
  }

  @Transactional
  public List<Course> crupdateCourses(List<Course> toWrite) {
    toWrite.forEach(validator);
    return repository.saveAll(toWrite);
  }
}

