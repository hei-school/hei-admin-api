package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.CrupdateExam;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.endpoint.rest.model.StudentExamGrade;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.service.CourseAssignmentService;
import school.hei.haapi.service.GradeService;

@Component
@AllArgsConstructor
public class ExamMapper {
  private CourseAssignmentMapper courseAssignmentMapper;
  private CourseAssignmentService courseAssignmentService;
  private GradeService gradeService;

  public ExamInfo toRest(Exam exam) {
    return new ExamInfo()
        .id(exam.getId())
        .coefficient(exam.getCoefficient())
        .title(exam.getTitle())
        .examinationDate(exam.getExaminationDate())
        .awardedCourse(awardedCourseMapper.toRest(exam.getAwardedCourse()));
  }

  public Exam toDomain(ExamInfo examInfo, CourseAssignment awardedCourse) {
    return Exam.builder()
        .id(examInfo.getId())
        .coefficient(examInfo.getCoefficient())
        .title(examInfo.getTitle())
        .examinationDate(examInfo.getExaminationDate())
        .awardedCourse(awardedCourse)
        .build();
  }

  public Exam toDomain(CrupdateExam createExam) {
    CourseAssignment courseAssignment = awardedCourseService.findById(createExam.getCourseAssignmentId());
    return Exam.builder()
        .id(createExam.getId())
        .coefficient(createExam.getCoefficient())
        .title(createExam.getTitle())
        .examinationDate(createExam.getExaminationDate())
        .courseAssignment(courseAssignment)
        .build();
  }

  public List<ExamInfo> toRestList(List<Exam> examList) {
    return examList.stream().map(this::toRest).toList();
  }

  public StudentExamGrade toRestStudentExamGrade(String studentId, Exam exam) {
    return new StudentExamGrade()
        .exam(toRest(exam))
        .score(gradeService.getGradeByExamIdAndStudentId(exam.getId(), studentId).getScore());
  }
}
