package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateExam;
import school.hei.haapi.endpoint.rest.model.Exam;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.service.CourseAssignmentService;
import school.hei.haapi.service.GradeService;

@Component
@AllArgsConstructor
public class ExamMapper {
  private CourseAssignmentMapper courseAssignmentMapper;
  private CourseAssignmentService courseAssignmentService;
  private GradeService gradeService;

  public Exam toRest(school.hei.haapi.model.Exam exam) {
    return new Exam()
        .id(exam.getId())
        .coefficient(exam.getCoefficient())
        .title(exam.getTitle())
        .examinationDate(exam.getExaminationDate())
        .courseAssignment(courseAssignmentMapper.toRest(exam.getCourseAssignment()));
  }

  public school.hei.haapi.model.Exam toDomain(Exam examInfo, CourseAssignment courseAssignment) {
    return school.hei.haapi.model.Exam.builder()
        .id(examInfo.getId())
        .coefficient(examInfo.getCoefficient())
        .title(examInfo.getTitle())
        .examinationDate(examInfo.getExaminationDate())
        .courseAssignment(courseAssignment)
        .build();
  }

  public school.hei.haapi.model.Exam toDomain(CrupdateExam createExam) {
    CourseAssignment courseAssignment =
        courseAssignmentService.getCourseAssignmentById(createExam.getCourseAssignmentId());
    return school.hei.haapi.model.Exam.builder()
        .id(createExam.getId())
        .coefficient(createExam.getCoefficient())
        .title(createExam.getTitle())
        .examinationDate(createExam.getExaminationDate())
        .courseAssignment(courseAssignment)
        .build();
  }

  public List<Exam> toRestList(List<school.hei.haapi.model.Exam> examList) {
    return examList.stream().map(this::toRest).toList();
  }

  public StudentExamGrade toRestStudentExamGrade(school.hei.haapi.model.Grade grade) {
    return new StudentExamGrade().exam(toRest(grade.getExam())).score(grade.getScore());
  }
}
