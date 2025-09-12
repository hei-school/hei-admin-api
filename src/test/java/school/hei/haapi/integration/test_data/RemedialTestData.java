package school.hei.haapi.integration.test_data;

import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Remedial;
import school.hei.haapi.model.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;

public class RemedialTestData {
    public static Remedial createRemedial(CourseAssignment courseAssignment) {
        return Remedial.builder()
                .id(randomUUID().toString())
                .title("Remedial title")
                .remedialDate(Instant.now())
                .courseAssignment(courseAssignment)
                .students(List.of(axel(), tolojanahary()))
                .build();
    }
}
