package school.hei.haapi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.StudentCourse;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.StudentCourseRepository;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.repository.dao.FilterCourseDao;

@Service
@AllArgsConstructor
public class CourseService {
  private final StudentCourseRepository repository;
  private final UserRepository userRepository;

  private final FilterCourseDao filterCourseDao;

  public List<Course> getStudentCourses(String studentId, CourseStatus status) {
    CourseStatus status1 = status == null ? CourseStatus.LINKED : status;
    if (userRepository.findById(studentId).isPresent()) {
      return repository.findByStudentIdAndStatus(studentId, status1).stream()
              .map(StudentCourse::getCourse)
              .collect(Collectors.toUnmodifiableList());
    }
    throw new NotFoundException("Student." + studentId + " is not found.");
  }

  private final CourseRepository courseRepository;

  public List<Course> createOrUpdateCourse(List<Course> toUpdate) {
    return courseRepository.saveAll(toUpdate);
  }

  public List<Course> getCourses(String code, String name, Integer credits, String teacherFirstName, String teacherLastName,
                                 PageFromOne page, BoundedPageSize pageSize, String creditsOrder, String codeOrder) {
    Pageable pageable;
    int pageValue = page.getValue() - 1;
    int pageSizeValue = pageSize.getValue();

    if (creditsOrder != null) {
      if (creditsOrder.equals("ASC")) {
        pageable = PageRequest.of(pageValue, pageSizeValue, Sort.by(Sort.Direction.ASC, "credits"));
      } else if (creditsOrder.equals("DESC")) {
        pageable = PageRequest.of(pageValue, pageSizeValue, Sort.by(Sort.Direction.DESC, "credits"));
      } else {
        pageable = PageRequest.of(pageValue, pageSizeValue);
      }
    } else if (codeOrder != null) {
      if (codeOrder.equals("ASC")) {
        pageable = PageRequest.of(pageValue, pageSizeValue, Sort.by(Sort.Direction.ASC, "code"));
      } else if (codeOrder.equals("DESC")) {
        pageable = PageRequest.of(pageValue, pageSizeValue, Sort.by(Sort.Direction.DESC, "code"));
      } else {
        pageable = PageRequest.of(pageValue, pageSizeValue);
      }
    } else {
      pageable = PageRequest.of(pageValue, pageSizeValue);
    }

    if (code != null || name != null || credits != null || teacherFirstName != null || teacherLastName != null) {
      return filterCourseDao.findByCriteria(code, name, credits, teacherFirstName, teacherLastName, pageable);
    } else {
      return courseRepository.findAll(pageable).toList();
    }
  }

//  public List<Course> getCourses(PageFromOne page, BoundedPageSize pageSize) {
//    int pageValue = page.getValue() - 1;
//    int pageSizeValue = pageSize.getValue();
//    Pageable pageable = PageRequest.of(pageValue, pageSizeValue);
//    return courseRepository.findAll(pageable).toList();
//  }

//  public List<Course> getCoursesByTri(PageFromOne page, BoundedPageSize pageSize, String creditsOrder, String codeOrder) {
//    Pageable pageable;
//    int pageValue = page.getValue() - 1;
//    int pageSizeValue = pageSize.getValue();
//
//    if (creditsOrder != null) {
//      if (creditsOrder.equals("ASC")) {
//        pageable = PageRequest.of(pageValue, pageSizeValue, Sort.by(Sort.Direction.ASC, "credits"));
//      } else if (creditsOrder.equals("DESC")) {
//        pageable = PageRequest.of(pageValue, pageSizeValue, Sort.by(Sort.Direction.DESC, "credits"));
//      } else {
//        pageable = PageRequest.of(pageValue, pageSizeValue);
//      }
//    } else if (codeOrder != null) {
//      if (codeOrder.equals("ASC")) {
//        pageable = PageRequest.of(pageValue, pageSizeValue, Sort.by(Sort.Direction.ASC, "code"));
//      } else if (codeOrder.equals("DESC")) {
//        pageable = PageRequest.of(pageValue, pageSizeValue, Sort.by(Sort.Direction.DESC, "code"));
//      } else {
//        pageable = PageRequest.of(pageValue, pageSizeValue);
//      }
//    } else {
//      pageable = PageRequest.of(pageValue, pageSizeValue);
//    }
//    return courseRepository.findAll(pageable).toList();
//  }

  //  public List<Course> getByCriteria(
//          String code, String name, Integer credits, String teacherFirstName, String teacherLastName,
//          PageFromOne page, BoundedPageSize pageSize) {
//    Pageable pageable = PageRequest.of(
//            page.getValue() - 1,
//            pageSize.getValue());
//            return filterCourseDao.findByCriteria(code,name,credits,teacherFirstName,teacherLastName,pageable);
//  }

  public List<Course> updateStudentCourse(List<StudentCourse> toUpdate) {
    return repository.saveAll(toUpdate).stream()
            .map(StudentCourse::getCourse)
            .collect(Collectors.toUnmodifiableList());
  }
}

