package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim;
import school.hei.haapi.endpoint.rest.validator.CreateTranscriptClaimValidator;
import school.hei.haapi.model.TranscriptClaim;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.TranscriptClaimRepository;
import school.hei.haapi.service.TranscriptVersionService;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;


@Component
@AllArgsConstructor
public class TranscriptClaimMapper {
    private  final TranscriptVersionService transcriptVersionService;
    private final TranscriptClaimRepository transcriptClaimRepository;
    private final CreateTranscriptClaimValidator transcriptClaimValidator;
    public  StudentTranscriptClaim toRest(TranscriptClaim transcriptClaim){
        return new StudentTranscriptClaim()
                .id(transcriptClaim.getId())
                .transcriptId(transcriptClaim.getTranscriptVersion().getTranscript().getId())
                .transcriptVersionId(transcriptClaim.getTranscriptVersion().getId())
                .creationDatetime(transcriptClaim.getCreationDatetime())
                .closedDatetime(transcriptClaim.getClosedDatetime())
                .reason(transcriptClaim.getReason())
                .status(StudentTranscriptClaim.StatusEnum.fromValue(transcriptClaim.getClaimStatus().toString()));
    }

    public TranscriptClaim toDomain(
            StudentTranscriptClaim studentTranscriptClaim, String studentId,
            String transcriptId, String versionId, String claimId) {
        Optional<TranscriptClaim> claim = transcriptClaimRepository
                .findByTranscriptVersionTranscriptStudentIdAndTranscriptVersionTranscriptIdAndTranscriptVersionIdAndId(
                        studentId,transcriptId,versionId,claimId);
        Instant creationDatetime = Instant.now();
        Instant closedDatetime = null;
        TranscriptClaim.ClaimStatus status = studentTranscriptClaim.getStatus()==null?
                TranscriptClaim.ClaimStatus.OPEN
                : TranscriptClaim.ClaimStatus.valueOf(studentTranscriptClaim.getStatus().toString());
        if (claim != null && claim.isPresent()){

            creationDatetime = studentTranscriptClaim.getCreationDatetime();
            closedDatetime = studentTranscriptClaim.getClosedDatetime();

            if (studentTranscriptClaim.getStatus()!=null){
                status = TranscriptClaim.ClaimStatus.valueOf(studentTranscriptClaim.getStatus().toString());
            }

            if (claim.get().getClaimStatus().equals(TranscriptClaim.ClaimStatus.OPEN)
                    && Objects.equals(studentTranscriptClaim.getStatus(), StudentTranscriptClaim.StatusEnum.CLOSE)){
                closedDatetime = Instant.now();
            }

        } else {
            if (studentTranscriptClaim.getCreationDatetime()!=null || studentTranscriptClaim.getClosedDatetime()!=null){
                throw new BadRequestException("At creation, creationDatetime and closedDatetime should be empty");
            }
        }

        transcriptClaimValidator.accept(studentTranscriptClaim );
        if (!Objects.equals(studentTranscriptClaim.getId(), claimId)) {
            throw new BadRequestException("Id in request body "+studentTranscriptClaim.getId()+" is different from id in path variable "+claimId);
        }

        return TranscriptClaim.builder()
                .id(studentTranscriptClaim.getId())
                .reason(studentTranscriptClaim.getReason())
                .claimStatus(status)
                .closedDatetime(closedDatetime)
                .creationDatetime(creationDatetime)
                .transcriptVersion(transcriptVersionService.getTranscriptVersion(studentId,transcriptId,versionId))
                .build();
    }
}
