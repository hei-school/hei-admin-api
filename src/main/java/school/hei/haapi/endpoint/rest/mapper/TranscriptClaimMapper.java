package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptClaim;
import school.hei.haapi.endpoint.rest.validator.CreateTranscriptClaimValidator;
import school.hei.haapi.model.TranscriptClaim;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.TranscriptClaimRepository;
import school.hei.haapi.service.TranscriptClaimService;
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

    public TranscriptClaim toDomain(StudentTranscriptClaim studentTranscriptClaim, String studentId, String transcriptId, String versionId, String claimId) {
        Optional<TranscriptClaim> claim = transcriptClaimRepository.findByTranscriptVersionTranscriptStudentIdAndTranscriptVersionTranscriptIdAndTranscriptVersionIdAndId(studentId,transcriptId,versionId,claimId);
        Instant creationDatetime = Instant.now();
        Instant closedDatetime = null;
        if (claim != null && claim.isPresent()){
            creationDatetime = claim.get().getCreationDatetime();
            closedDatetime = claim.get().getClosedDatetime();

            if (claim.get().getClaimStatus().equals(TranscriptClaim.ClaimStatus.OPEN)
                    && Objects.equals(studentTranscriptClaim.getStatus(), StudentTranscriptClaim.StatusEnum.CLOSE)){
                closedDatetime = Instant.now();
            }

        }

        transcriptClaimValidator.accept(studentTranscriptClaim );
        if (!Objects.equals(studentTranscriptClaim.getId(), claimId)) {
            throw new BadRequestException("Id in request body: "+studentTranscriptClaim.getId()+"is different from id in path variable: "+claimId);
        }
        if (!Objects.equals(studentTranscriptClaim.getTranscriptId(), transcriptId)) {
            throw new BadRequestException("transcriptId in request body: "+studentTranscriptClaim.getTranscriptId()+"is different from transcript_id in path variable: "+transcriptId);
        }
        if (!Objects.equals(studentTranscriptClaim.getTranscriptVersionId(), versionId)) {
            throw new BadRequestException("versionId in request body: "+studentTranscriptClaim.getTranscriptVersionId()+"is different from version_id in path variable: "+versionId);
        }

        return TranscriptClaim.builder()
                .id(studentTranscriptClaim.getId())
                .reason(studentTranscriptClaim.getReason())
                .claimStatus(studentTranscriptClaim.getStatus()==null?TranscriptClaim.ClaimStatus.OPEN
                        : TranscriptClaim.ClaimStatus.valueOf(studentTranscriptClaim.getStatus().toString()))
                .closedDatetime(closedDatetime)
                .creationDatetime(creationDatetime)
                .transcriptVersion(transcriptVersionService.getTranscriptVersion(transcriptId,versionId))
                .build();
    }
}
