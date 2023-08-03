package school.hei.haapi.endpoint.rest.validator;

import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CreatePayment;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.function.Consumer;
@Component
public class CreateTranscriptClaimValidator implements Consumer<StudentTranscriptClaim> {
    @Override public void accept(StudentTranscriptClaim studentTranscriptClaim) {
        //TODO: reason validation if blank
        if (studentTranscriptClaim.getReason() == null && studentTranscriptClaim.getReason().isEmpty()) {
            throw new BadRequestException("Reason is mandatory");
        }
        if (studentTranscriptClaim.getId() == null && studentTranscriptClaim.getId().isEmpty()) {
            throw new BadRequestException("Id is mandatory");
        }
        if (studentTranscriptClaim.getTranscriptId() == null && studentTranscriptClaim.getTranscriptId().isEmpty()) {
            throw new BadRequestException("Transcript Id is mandatory");
        }
        if (studentTranscriptClaim.getTranscriptVersionId() == null && studentTranscriptClaim.getTranscriptVersionId().isEmpty()) {
            throw new BadRequestException("Transcript Version Id is mandatory");
        }
        if (studentTranscriptClaim.getCreationDatetime() != null
                && studentTranscriptClaim.getClosedDatetime() != null
                && studentTranscriptClaim.getClosedDatetime().isBefore(studentTranscriptClaim.getCreationDatetime())) {
            throw new BadRequestException("closed datetime should be after creation datetime");
        }
    }
}
