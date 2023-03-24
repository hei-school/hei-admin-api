package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Courses;
import school.hei.haapi.endpoint.rest.model.User;
import school.hei.haapi.repository.CoursesRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class CourseMapper {
    private final CoursesRepository coursesRepository;
    public Courses toDomain(Courses entity){
        return Courses.builder()
                .id(entity.getId())
                .name(entity.getName())
                .code(entity.getCode())
                .credits(entity.getCredits())
                .total_hours(entity.getTotal_hours())
                .main_teacher(entity.getMain_teacher())
                .build();
    }
    public List<Courses> toRest(List<Courses> list){
        return list.stream().map(this::toDomain).collect(Collectors.toList());
    }
}
