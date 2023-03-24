package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.Course;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.UserRepository;

@Service
@AllArgsConstructor
public class CourseService {
    private final UserRepository userRepository;

    private final CourseRepository courseRepository;
    public List<Course> getCourses(
            PageFromOne page,
            BoundedPageSize pageSize
    ) {
        int pageValue = page.getValue() - 1;
        int pageSizeValue = pageSize.getValue();
        Pageable pageable = PageRequest.of(pageValue, pageSizeValue);
        return courseRepository.findAll(pageable).toList();
    }
}
