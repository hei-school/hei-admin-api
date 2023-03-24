package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.hei.haapi.endpoint.rest.model.CourseOrder;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.repository.CourseRepository;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

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
                                 String teacherFirstName,
                                 String teacherLastName,
                                 CourseOrder codeOrder,
                                 CourseOrder creditsOrder) {
    Sort codeOrderSort =
        Sort.by(codeOrder == CourseOrder.ASC || codeOrder == null ? ASC : DESC, "code");
    Sort creditsOrderSort =
        Sort.by(creditsOrder == CourseOrder.ASC || codeOrder == null ? ASC : DESC, "credits");

    Pageable pageable =
        PageRequest.of(page.getValue() - 1,
            pageSize.getValue(),
            creditsOrderSort.and(codeOrderSort));

    return repository.findAllFiltered(code, name, credits, teacherFirstName, teacherLastName,
        pageable);
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


  @Transactional
  public List<Course> crupdateCourses(List<Course> toWrite) {
    toWrite.forEach(validator);
    return repository.saveAll(toWrite);
  }
}

