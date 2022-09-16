package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.validator.CourseValidator;
import school.hei.haapi.repository.CourseRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseValidator courseValidator;
    private final CourseRepository courseRepository;

    public Course getById(String courseId){
        return courseRepository.getById(courseId);
    }

    public List<Course> getByRef(
            String ref,
            PageFromOne page,
            BoundedPageSize pageSize
            ){
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue(),
                Sort.by(ASC, "ref"));
        return courseRepository.findByRefContainingIgnoreCase(
                ref,pageable
        );
    }

    public Course save(Course course){
        courseValidator.accept(course);
        return courseRepository.save(course);
    }
}
