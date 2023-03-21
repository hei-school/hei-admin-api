package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.Fee;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRespository;
import school.hei.haapi.repository.FeeRepository;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@AllArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRespository courseRepository;

    public List<Course> getCoursesByStudentId(
            String studentId, PageFromOne page, BoundedPageSize pageSize,
            school.hei.haapi.endpoint.rest.model.CourseStatus status) {
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue());
        if (status != null) {
            return courseRepository.getCoursesByStudentIdAndStatus(studentId, status, pageable);
        }
        return courseRepository.getByStudentId(studentId, pageable);
    }
}
