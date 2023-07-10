package school.hei.haapi.endpoint.rest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.ExamMapper;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.ExamDetail;
import school.hei.haapi.endpoint.rest.model.ExamInfo;
import school.hei.haapi.endpoint.rest.model.StudentGrade;
import school.hei.haapi.model.Exam;
import school.hei.haapi.service.ExamService;
import school.hei.haapi.service.GradeService;

@RestController
@AllArgsConstructor

public class ExamController {
  private final ExamService examService;
  private final ExamMapper examMapper;
  private final GradeService gradeService;
  private final GradeMapper gradeMapper;

  @GetMapping(value = "/courses/{course_id}/exams")
  public List<ExamInfo> getCourseExams(@PathVariable("course_id") String courseId) {
    //TODO: use ".stream().map(...).collect(...)" but not "for"
    List<Exam> exams = examService.getCourseExams(courseId);
    List<ExamInfo> examInfos = new ArrayList<>();

    for (Exam exam : exams) {

      ExamInfo examInfo = examMapper.toExamInfo(exam);
      examInfos.add(examInfo);
    }

    return examInfos;
  }

  @GetMapping(value = "/courses/{course_id}/exams/{exam_id}/details")
  public ExamDetail getExamDetails(
      @PathVariable("course_id") String courseId,
      @PathVariable("exam_id") String examId
  ) {
    Exam exam = examService.getExamById(examId);
    List<StudentGrade> studentGrades = gradeService.getGradeByExam(exam).stream().map(gradeMapper::toRestStudentGrade).collect(Collectors.toList());

    ExamDetail examDetail = new ExamDetail()
            .id(exam.getId())
            .coefficient(exam.getCoefficient())
            .title(exam.getTitle())
            .participants(studentGrades)
            .examinationDate(exam.getExaminationDate());
    return examDetail;
  }
}

