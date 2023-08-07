package school.hei.haapi.endpoint.rest.mapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentTranscriptVersion;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.service.TranscriptService;
import school.hei.haapi.service.UserService;

import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class StudentTranscriptVersionMapper {

    private final UserService userService;
    private final TranscriptService transcriptService;

    public StudentTranscriptVersion toRest(school.hei.haapi.model.StudentTranscriptVersion studentTranscriptVersion) {
        final Transcript transcript = studentTranscriptVersion.getTranscript();
        final User responsible = studentTranscriptVersion.getResponsible();
        if (Objects.isNull(transcript) || Objects.isNull(responsible)) {
            throw new NotFoundException("Transcript or User not found");
        }
        return new StudentTranscriptVersion()
                .id(studentTranscriptVersion.getId())
                .transcriptId(transcript.getId())
                .ref(studentTranscriptVersion.getRef())
                .createdByUserId(responsible.getId())
                .createdByUserRole(responsible.getRole().name())
                .creationDatetime(studentTranscriptVersion.getCreationDatetime());
    }

    public school.hei.haapi.model.StudentTranscriptVersion toDomain(StudentTranscriptVersion restStudentTranscriptVersion, String studentId) {
        final Transcript transcript = transcriptService.getByIdAndStudentId(restStudentTranscriptVersion.getTranscriptId(), studentId);
        final User responsible = userService.getById(restStudentTranscriptVersion.getCreatedByUserId());
        if (Objects.isNull(transcript) || Objects.isNull(responsible)) {
            throw new NotFoundException("Transcript or User not found");
        }
        return school.hei.haapi.model.StudentTranscriptVersion.builder()
                .id(restStudentTranscriptVersion.getId())
                .transcript(transcript)
                .ref(restStudentTranscriptVersion.getRef())
                .responsible(responsible)
                .creationDatetime(restStudentTranscriptVersion.getCreationDatetime())
                .build();
    }
}
