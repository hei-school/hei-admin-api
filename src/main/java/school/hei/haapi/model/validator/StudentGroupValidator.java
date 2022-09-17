package school.hei.haapi.model.validator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.StudentGroup;
import school.hei.haapi.model.exception.BadRequestException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class StudentGroupValidator implements Consumer<StudentGroup> {
    private final Validator validator;

    public void accept(List<StudentGroup> StudentGroups) {
        StudentGroups.forEach(this::accept);
    }

    @Override
    public void accept(StudentGroup StudentGroup) {
        Set<ConstraintViolation<StudentGroup>> violations = validator.validate(StudentGroup);
        if (!violations.isEmpty()) {
            String constraintMessages = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(". "));
            throw new BadRequestException(constraintMessages);
        }
    }
}
