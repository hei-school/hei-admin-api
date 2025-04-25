package school.hei.haapi.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Grade;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@Testcontainers
@AutoConfigureMockMvc
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class DirtyGradeServiceTest extends FacadeITMockedThirdParties {
    @Autowired GradeService subject;

    private List<Grade> createRandom(int count) {
        return IntStream.range(0, count).mapToObj(()->{
            Grade grade = new Grade(
                    null,
                    null,
                    null,
                    null,
                    null
            );
            grade.setStudent();
            grade.setExam();
            grade.setScore(18.2);
            return grade;
        }).toList();
    }

    @Test
    void crupdate_grade_ok() {
        List<Grade> randomGrade = createRandom(1);

        Grade savedGrades = subject.crupdateParticipantGrade(randomGrade).getFirst();

        assertNotNull(savedGrades.getId());
        assertEquals(36.4, savedGrades.getScore());
    }
}
