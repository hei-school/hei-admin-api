package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;

@Component
@AllArgsConstructor
public class TranscriptVersionMapper {
    public StudentTranscriptVersion toRest(school.hei.haapi.model.TranscriptVersion version) {
        return new StudentTranscriptVersion()
                .id(version.getId())
                .ref(version.getRef())
                .creationDatetime(version.getCreateDatetime())
                .transcriptId(version.getTranscript().getId())
                .createdBy(version.getEditor().getId());
    }
}
