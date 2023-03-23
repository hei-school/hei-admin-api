package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.rest.mapper.CourseMapper;
import school.hei.haapi.model.Course;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.utils.QueryBuilder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            String teacherLastName
    ) {
        return repository.getCourseAndFilter(query.getCourseAndFilterQuery(code,name,credit,teacherFirstName,teacherLastName));
    }

}