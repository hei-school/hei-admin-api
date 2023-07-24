package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.*;

@Component
@AllArgsConstructor
public class TranscriptMapper {

    private final UserMapper userMapper;

    public Transcript toRest(school.hei.haapi.model.Transcript domain) {
        Student student = userMapper.toRestStudent(domain.getStudentId());
        return new Transcript()
                .id(domain.getId())
                .academicYear(domain.getAcademic_year())
                .semester(Semester.fromValue(domain.getSemester().toString()))
                .isDefinitive(domain.is_definitive())
                .creationDatetime(domain.getCreationDatetime())
                .studentId(student);
    }
}
