package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.utils.QueryBuilder;

import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final CourseMapper mapper;
    private QueryBuilder query = new QueryBuilder();
    public List<Course> GetAllAndFiltreReturnedList(
            String code,
            String name,
            int credit,
            String teacherFirstName,
            String teacherLastName,
            String codeOrder,
            String creditsOrder,
            PageFromOne page,
            BoundedPageSize pageSize
    ) {
        Pageable pageable = PageRequest.of(
                page.getValue() - 1,
                pageSize.getValue());
        return repository.getCourseAndFilter(query.getCourseAndFilterQuery(code,name,credit,teacherFirstName,teacherLastName,codeOrder,creditsOrder),pageable);
    }
}