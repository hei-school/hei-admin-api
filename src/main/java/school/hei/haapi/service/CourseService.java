package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.BadRequestException;
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
            PageFromOne page, BoundedPageSize pageSize, String code, String name, Integer credits,
            String teacherFirstName, String teacherLastName, String codeOrder, String creditsOrder) {
        Pageable pageable = this.pagination(page, pageSize, codeOrder, creditsOrder);
        return (teacherFirstName == null && teacherLastName == null)
                ? courseRepository.findAllContainsIgnoreCaseByCodeAndNameAndCredits(
                code, name, credits, pageable)
                : courseRepository.findAllContainsIgnoreCaseByCodeAndNameAndCreditsAndTeacher(
                code, name, credits, teacherFirstName, teacherLastName, pageable);
    }

    private Pageable pagination(PageFromOne page, BoundedPageSize pageSize, String codeOrder, String creditsOrder) {
        Sort sortCode = this.order("code", codeOrder);
        Sort sortCredits = this.order("credits", creditsOrder);
        Sort sort = (sortCode != null && sortCredits != null) ? sortCode.and(sortCredits)
                : (sortCode != null) ? sortCode
                : sortCredits;
        return (sort != null) ? PageRequest.of(page.getValue() - 1, pageSize.getValue(), sort)
                : PageRequest.of(page.getValue() - 1, pageSize.getValue());
    }

    private Sort order(String orderBy, String direction) {
        if (direction == null) {
            return null;
        } else if (!direction.equals("ASC") && !direction.equals("DESC")) {
            throw new BadRequestException(orderBy + "Order value must be ASC or DESC");
        } else {
            return Sort.by(Sort.Direction.valueOf(direction), orderBy);
        }
    }
}
