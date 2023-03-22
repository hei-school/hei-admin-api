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
        int pageValue = 1;
        int pageSizeValue = 15;
        if (page.getValue() != 0)
            pageValue = page.getValue();
        if (pageSize.getValue() != 0)
            pageSizeValue = pageSize.getValue();
        Pageable pageable = PageRequest.of(
                pageValue - 1,
                pageSizeValue
        );
        return courseRepository.findAll(pageable).getContent();
    }
}
