package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.Transcript;

@Component
@AllArgsConstructor
public class TranscriptMapper {
    public Transcript toRest(school.hei.haapi.model.Transcript transcript) {
        return new Transcript()
                .id(transcript.getId())
                .studentId(transcript.getStudent().getId())
                .semester(transcript.getSemester())
                .academicYear(transcript.getAcademicYear())
                .isDefinitive(transcript.getIsDefinitive());
    }
}
