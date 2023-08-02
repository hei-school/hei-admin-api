package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim;
import school.hei.haapi.model.TranscriptClaim;


@Component
@AllArgsConstructor
public class TranscriptClaimMapper {
    public  StudentTranscriptClaim toRest(TranscriptClaim transcriptClaim){
        return new StudentTranscriptClaim()
                .id(transcriptClaim.getId())
                .transcriptId(transcriptClaim.getTranscriptVersion().getId())
                .creationDatetime(transcriptClaim.getCreationDate())
                .closedDatetime(transcriptClaim.getClosedDate())
                .reason(transcriptClaim.getReason())
                .status(StudentTranscriptClaim.StatusEnum.fromValue(transcriptClaim.getStatus().toString()));
    }
}
