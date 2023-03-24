package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import school.hei.haapi.endpoint.rest.model.CodeOrder;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.endpoint.rest.model.CreditsOrder;
import school.hei.haapi.model.*;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.CourseStudentRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
@CrossOrigin
public class CourseService {
    private final CourseRepository repository;
    private final CourseStudentRepository courseStudentRepository;
    private final UserService userService;

    public List<Course> getAll(PageFromOne page, BoundedPageSize pageSize){
        Pageable pageable = PageRequest.of(page.getValue(), pageSize.getValue());
        return repository.findAll(pageable).toList();
    }

    public List<Course> getByFilter(
            PageFromOne page, BoundedPageSize pageSize, CodeOrder codeOrder, String name,
            CreditsOrder creditsOrder, String teacherFirstName, String teacherLastName){

        Pageable pageable = PageRequest.of(page.getValue(), pageSize.getValue());

        if (codeOrder != null) {
            Sort.Direction direction = codeOrder.getValue() == CodeOrder.ASC.getValue() ? ASC : DESC;
            pageable = PageRequest.of(page.getValue(), pageSize.getValue(),Sort.by(direction, "code"));
            return repository.findAll(pageable).toList();
        }
        else if (creditsOrder != null) {
            Sort.Direction direction = creditsOrder.getValue() == CreditsOrder.ASC.getValue() ? ASC : DESC;
            pageable = PageRequest.of(page.getValue(), pageSize.getValue(),Sort.by(direction, "credits"));
            return repository.findAll(pageable).toList();
        }
        else if (name != null) {
            return repository.getCourseByNameContainingIgnoreCase(name, pageable);
        }
        else if (teacherFirstName != null && teacherLastName != null) {
            return repository.getCourseByMainTeacherFirstNameAndLastName(
                    teacherFirstName, teacherLastName, pageable);
        }
        else if (teacherFirstName != null) {
            return repository.getByMainTeacherFirstNameContainingIgnoreCase(teacherFirstName, pageable);
        }
        else if (teacherLastName != null) {
            return repository.getByMainTeacherLastNameContainingIgnoreCase(teacherLastName, pageable);
        }
        else {
            return repository.findAll(pageable).toList();
        }
    }

  public List<Course> saveAll(List<Course> courses) {
    return repository.saveAll(courses);
  }
    public List<Course> findCoursesByStudent(String studentId) {
        User student = userService.getById(studentId);
        return courseStudentRepository.findAllByStudent(student).stream()
                .map(CourseStudent::getCourse)
                .collect(Collectors.toList());
    }

    public void updateCourseStudentStatus(String studentId, String courseId, CourseStatus newStatus) {
        User student = userService.getById(studentId);
        Course course = repository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course id: " + courseId));
        CourseStudent courseStudent = courseStudentRepository.findByStudentAndCourse(student, course)
                .orElseThrow(() -> new IllegalArgumentException("No course student found for student " + studentId + " and course " + courseId));
        courseStudent.setStatus(newStatus);
        courseStudentRepository.save(courseStudent);
    }



}
