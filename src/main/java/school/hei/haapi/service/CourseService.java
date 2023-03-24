package school.hei.haapi.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    public List<Course> findByCriteria(PageFromOne page, BoundedPageSize pageSize, String code,
                                       String name, Integer credits, String teacherFirstName, String teacherLastName) {

        int pageValue = page == null ? 1 : page.getValue();
        int pageSizeValue = pageSize == null ? 15 : pageSize.getValue();
        Pageable pageable =
                PageRequest.of(pageValue - 1, pageSizeValue);
        return courseRepository.findByCriteria(code, name, credits, teacherFirstName,
                pageable);
    }
}