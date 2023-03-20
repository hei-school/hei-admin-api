package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
public class CourseService {

    private final CourseRepository repository;

    public List<Course> getCourseByStudentId(String studentId, PageFromOne page, BoundedPageSize pageSize, Course.CourseStatus status){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(DESC, "dueDatetime"));
        if (ASC.name() != null) {
            return repository.getByStudentId(studentId, pageable, status);
        }
        return repository.getByStudentId(studentId, pageable, status);
    }
    public List<Course> getAllCourses() {
        return repository.findAll();
    }

    public Course getById(String courseId) {
        return repository.getById(courseId);
    }


    public List<Course> saveAllCourses(List<Course> courses) {
        return repository.saveAll(courses);
    }

}
