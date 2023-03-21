package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static school.hei.haapi.model.Course.StatusEnum.LINKED;
import static school.hei.haapi.model.Course.StatusEnum.UNLINKED;

@Service
@AllArgsConstructor
public class CourseService {

    private final CourseRepository repository;

    public List<Course> getCourseByStudentId(String studentId, PageFromOne page, BoundedPageSize pageSize, Course.StatusEnum status){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(DESC, "dueDatetime"));
        if (status != null) {
            return repository.getCourseByStudentId(studentId, pageable, status);
        }
        return repository.getByStudentId(studentId, pageable);
    }
    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public Course getById(String courseId) {
        return repository.getById(courseId);
    }

    private Course updateCourseStatus(Course initialCourse) {
        if (initialCourse.getStatus() == null || initialCourse.getId() == null) {
            initialCourse.setStatus(LINKED);
        } else if (initialCourse.getStatus() != null) {
            initialCourse.setStatus(UNLINKED);
        }
        return initialCourse;
    }

    public List<Course> saveAllCourses(List<Course> courses) {
        return repository.saveAll(courses);
    }

}
