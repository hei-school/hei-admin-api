package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.model.Order;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;


@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository repository;
    public List<Course> getCourses(PageFromOne page, BoundedPageSize pageSize,
                                   String code, String name, Integer credits, String teacherFirstName,
                                   String teacherLastName, Order codeOrder, Order creditsOrder) {

        Sort sort = null;

        if (codeOrder != null && codeOrder.equals(Order.ASC)) {
            sort = Sort.by(Sort.Direction.ASC, "code");
        } else if (codeOrder != null && codeOrder.equals(Order.DESC)) {
            sort = Sort.by(Sort.Direction.DESC, "code");
        } else if (creditsOrder != null && creditsOrder.equals(Order.ASC)) {
            sort = Sort.by(Sort.Direction.ASC, "credits");
        } else if (creditsOrder != null && creditsOrder.equals(Order.DESC)) {
            sort = Sort.by(Sort.Direction.DESC, "credits");
        }

        Pageable pageable = PageRequest.of(page.getValue() - 1, pageSize.getValue(), sort);

        if (code != null) {
            return repository.getByCode(code, pageable);
        } else if (name != null) {
            return repository.getByName(name, pageable);
        } else if (credits != null) {
            return repository.getByCredits(credits, pageable);
        } else if (teacherFirstName != null && teacherLastName != null) {
            return repository.getByTeacherFirstAndLastName(teacherFirstName, teacherLastName, pageable);
        } else {
            return repository.findAll(pageable).getContent();
        }
    }
}