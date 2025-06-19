package school.hei.haapi.unit.objectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.CrupdateGrade;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.GradeService;

class GradeMapperTest {
  GradeMapper subject;
  ExamService examService;
  GradeRepository gradeRepository;
  GradeService gradeService;

  @BeforeEach
  void setUp() {
    gradeService = mock();
    examService = mock();
    gradeRepository = mock();
    subject = new GradeMapper(mock(), gradeService, examService, mock(), mock(), gradeRepository);
  }

  @Test
  void combine_student_with_bad_exam_to_get_grade_ko() {
    User student1 = new User();
    student1.setId("student1");
    User student2 = new User();
    student2.setId("student2");
    Exam exam = new Exam();
    exam.setGrades(List.of(new Grade("", student2, new Exam(), null, null)));

    NotFoundException notFoundException =
        assertThrows(NotFoundException.class, () -> subject.toRestStudentExamGrade(student1, exam));
    assertEquals(
        "Student %s have no grade for the exam %s".formatted(student1.getId(), exam.getId()),
        notFoundException.getMessage());
  }

  @Test
  void map_grade_to_grade_or_create_grade() {
    when(examService.getExamById(any())).thenReturn(new Exam(null, 1, null, null, null, null));
    when(gradeRepository.getGradeByExamIdAndStudentRef(any(), any())).thenReturn(Optional.empty());
    Grade gradeSaved = new Grade("", new User(), new Exam(), 1., Instant.now());
    when(gradeService.crupdateParticipantGrade(anyList())).thenReturn(List.of(gradeSaved));

    Grade grade = subject.toDomain(new CrupdateGrade().score(gradeSaved.getScore()), "", "");

    assertEquals(gradeSaved, grade);
  }
}
