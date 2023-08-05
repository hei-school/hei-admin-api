package school.hei.haapi.model.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.Transcript;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class TranscriptValidator implements Consumer<Transcript> {

    @Override
    public void accept(Transcript transcript) {
        Set<String> violationMessages = new HashSet<>();
        if (transcript.getId() == null) {
            violationMessages.add("Id is mandatory");
        }
        if (transcript.getStudent() == null) {
            violationMessages.add("Student is mandatory");
        }
        if (transcript.getSemester() == null) {
            violationMessages.add("Semester is mandatory");
        }
        if (transcript.getAcademicYear() == null) {
            violationMessages.add("Academic year is mandatory");
        }
        if (!violationMessages.isEmpty()) {
            String formattedViolationMessages = violationMessages.stream()
                    .map(String::toString)
                    .collect(Collectors.joining(". "));
            throw new BadRequestException(formattedViolationMessages);
        }
    }
}
