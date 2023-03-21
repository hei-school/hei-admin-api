package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.hibernate.annotations.Any;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.ResourceNotFoundException;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final UserRepository userRepository;
    public List<Course> getCourses(PageFromOne page, BoundedPageSize pageSize){
        if (page == null){
            page = new PageFromOne(1);
        }
        if (pageSize == null){
            pageSize = new BoundedPageSize(15);
        }
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(ASC, "code"));
        return repository.findAll(pageable).getContent();
    }

    public List<Course> getCoursesByStatus(String student_id, Course.Status status){
        if(status == null){
            return repository.findAllByStudentIdAndStatus(student_id, Course.Status.LINKED).getContent();
        }
        return repository.findAllByStudentIdAndStatus(student_id, status).getContent();
    }

    public Course getCourseById(String id) {
        Optional<Course> course = repository.findById(id);
        if (course.isEmpty()) {
            throw new ResourceNotFoundException("Course not found with id " + id);
        }
        return course.get();
    }

    public Course createCourse(Course course) {
        if (repository.findByCode(course.getCode()).isPresent()) {
            throw new IllegalArgumentException("Course with code " + course.getCode() + " already exists");
        }
        return repository.save(course);
    }

    public Course updateCourse(String id, Course course) {
        Course existingCourse = getCourseById(id);
        existingCourse.setCode(course.getCode());
        existingCourse.setName(course.getName());
        existingCourse.setCredits(course.getCredits());
        existingCourse.setTotalHours(course.getTotalHours());
        existingCourse.setMainTeacher(course.getMainTeacher());
        existingCourse.setStudent(course.getStudent());
        existingCourse.setStatus(course.getStatus());
        return repository.save(existingCourse);
    }

    public void deleteCourse(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found with id " + id);
        }
        repository.deleteById(id);
    }
}
