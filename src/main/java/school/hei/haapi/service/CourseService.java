package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.*;
import school.hei.haapi.endpoint.rest.model.CourseStatus;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.CourseStudentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository repository;
    private final CourseStudentRepository courseStudentRepository;
    private final UserService userService;
    public List<Course> getAll(PageFromOne page, BoundedPageSize pageSize){
        Pageable pageable =
                PageRequest.of(page.getValue() - 1, pageSize.getValue());
        return repository.findAll(pageable).toList();
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

    public List<Course> getByCriteria(PageFromOne page, BoundedPageSize pageSize, String firstName, String lastName, String codeOrder, String creditsOrder) {
        List<Sort.Order> orders = new ArrayList<>();
        if (codeOrder != null){
            orders.add(new Sort.Order(Sort.Direction.valueOf(codeOrder.toUpperCase()),"code"));
        }
        if (creditsOrder != null){
            orders.add(new Sort.Order(Sort.Direction.valueOf(creditsOrder.toUpperCase()),"credits"));
        }
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(orders)
        );

        if (firstName != null && lastName != null) {
            return repository.getByCriteria(firstName, lastName, pageable);
        }else if (firstName == null && lastName != null) {
            return repository.getByLastName(lastName, pageable);
        } else if(firstName != null && lastName == null){
            return repository.getByFirstName(firstName, pageable);
        }
        return repository.findAll(pageable).toList();
    }
}
