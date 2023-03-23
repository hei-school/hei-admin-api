package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private CourseRepository courseRepository;

    public List<Course> getAll(PageFromOne page, BoundedPageSize pageSize) {
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue()
        );
        return courseRepository.findAll(pageable).getContent();
    }

    public List<Course> getAllByCriteria(
            PageFromOne page, BoundedPageSize pageSize, String code,
            String name, Integer credits, String teacherFirstName, String teacherLastName) {
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue()
        );
        return courseRepository
                .findCoursesByCriteria(code, name, credits, teacherFirstName, teacherLastName, pageable);
    }
}
