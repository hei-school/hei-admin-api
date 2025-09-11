package school.hei.haapi.integration.test_data;

import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Remedial;
import school.hei.haapi.model.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.UUID.randomUUID;

public class RemedialTestData {
    public static Remedial createRemedial(Instant remedialDate, CourseAssignment course, List<User> students) {
        return Remedial.builder()
                .id(randomUUID().toString())
                .title("Remedial title")
                .remedialDate(remedialDate)
                .courseAssignment(course)
                .students(students != null ? students : new ArrayList<>())
                .build();
    }
}
