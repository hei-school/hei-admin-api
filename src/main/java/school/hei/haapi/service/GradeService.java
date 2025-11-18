package school.hei.haapi.service;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.event.model.GradeImportEvent;
import school.hei.haapi.endpoint.rest.model.ExamGradeStats;
import school.hei.haapi.endpoint.rest.model.StudentExamGradeImportValidationResult;
import school.hei.haapi.endpoint.rest.security.AuthProvider;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.dto.GradeImportDto;
import school.hei.haapi.model.dto.StudentImportDto;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.notEntity.UpdateGrade;
import school.hei.haapi.model.validator.IsNewGradeChecker;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.dao.GradeDao;
import school.hei.haapi.service.utils.excel.ExcelParser;

@Service
@AllArgsConstructor
public class GradeService {
  private final GradeRepository gradeRepository;
  private final GradeDao gradeDao;
  private final UserService userService;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final IsNewGradeChecker isNewGradeChecker;
  private final BucketComponent bucketComponent;
  private final EventProducer eventProducer;

  private static final String GRADE_XLSX_IMPORT_BUCKET_KEY = "/STUDENT_EXAM_GRADE_XLSX_IMPORT/";

  public List<Grade> getGradesByStudentId(String studentId) {
    var student = userService.getById(studentId);
    return gradeRepository.getAllByStudent(student);
  }

  public Grade getGradeByExamIdAndStudentId(String examId, String studentId) {
    return gradeRepository
        .getGradeByExamIdAndStudentId(examId, studentId)
        .orElseThrow(() -> new NotFoundException("Grade not found"));
  }

  public Grade getById(String id) {
    return gradeRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("grade with id " + id + " not found"));
  }

  // TODO: make this obey the single-responsibility rule, at least in the name.
  private Grade checkGradeToCreate(Grade grade) {
    String examId = grade.getExam().getId();
    String studentId = grade.getStudent().getId();

    isNewGradeChecker.accept(grade);

    Optional<Group> studentGroup = grade.getStudent().findCurrentGroup();
    if (studentGroup.isEmpty()) {
      String error = String.format("Student with id %s is not in any group", studentId);
      throw new BadRequestException(error);
    }

    boolean isInAssignedGroup =
        grade.getExam().getCourseAssignment().getGroups().stream()
            .anyMatch(group -> group.getId().equals(studentGroup.get().getId()));
    if (!isInAssignedGroup) {
      String error = String.format("Student with id %s is not in exam %s", studentId, examId);
      throw new BadRequestException(error);
    }

    return grade;
  }

  private Grade checkGradeToUpdate(UpdateGrade grade) {
    String examId = grade.grade().getExam().getId();
    String studentId = grade.grade().getStudent().getId();

    Optional<Grade> existingGrade = gradeRepository.findByExamIdAndStudentId(examId, studentId);
    if (existingGrade.isEmpty()) {
      throw new BadRequestException(
          "Grade for the student " + studentId + " for the exam " + examId + " not found");
    }

    Grade presentGrade = existingGrade.get();
    presentGrade.setScore(grade.grade().getScore(), grade.comment());
    return presentGrade;
  }

  @Transactional
  public List<Grade> createParticipantGrade(List<Grade> grades) {
    return gradeRepository.saveAll(grades.stream().map(this::checkGradeToCreate).toList());
  }

  @Transactional
  public List<Grade> updateParticipantGrade(List<UpdateGrade> grades) {
    return gradeRepository.saveAll(grades.stream().map(this::checkGradeToUpdate).toList());
  }

  private double getExamAverageGrade(String examId) {
    var averageOfGradeResult =
        gradeDao.getGradesByExamId(examId).stream().mapToDouble(Grade::getScore).average();
    if (averageOfGradeResult.isEmpty())
      throw new NotFoundException("Exam with id " + examId + " do not have a score");
    return averageOfGradeResult.getAsDouble();
  }

  public ExamGradeStats getExamGradeStats(String examId) {
    return new ExamGradeStats().average(getExamAverageGrade(examId));
  }

  private boolean isGroupInCourse(Group studentCurrentGroup, String courseId) {
    var pageable = Pageable.unpaged();
    return courseAssignmentRepository.findAllByCourseId(courseId, pageable).stream()
        .anyMatch(
            courseAssignment ->
                courseAssignment.getGroups().stream()
                    .anyMatch(group -> group.getId().equals(studentCurrentGroup.getId())));
  }

  public List<Grade> getGradesByStudentAndCourseId(String studentId, String courseId) {
    var student = userService.getById(studentId);
    var studentCurrentGroup = student.findCurrentGroup();
    if (studentCurrentGroup.isEmpty()) {
      throw new BadRequestException(String.format("Student with id: %s not in any group", student));
    }
    if (!isGroupInCourse(studentCurrentGroup.get(), courseId)) {
      throw new BadRequestException(
          String.format("Student's current group is not assigned to course with id: %s", courseId));
    }
    return gradeRepository.getGradesByStudentIdAndCourseId(studentId, courseId);
  }

  public StudentExamGradeImportValidationResult initStudentExamGradeImportFromXlsx(
      File excelFile) {
    var parser = new ExcelParser<>(GradeImportDto.class, StudentImportDto.getCellMap());
    var coordinatorEmail = AuthProvider.getPrincipal().getUser().getEmail();
    try {
      var parseResult = parser.parseFile(excelFile, 0, CREATE_NULL_AS_BLANK);
      if (parseResult.skippedRows().size() > 1) {
        var errorMessage =
            parseResult.skippedRows().values().stream()
                .map(Throwable::getMessage)
                .collect(Collectors.joining("\n"));
        throw new BadRequestException(errorMessage);
      }
      var importResults = parseResult.parsedResult();
      if (importResults.size() > 50) {
        throw new BadRequestException(
            "Le nombre maximum d'importation par excel est de 50 étudiants");
      }
      validateDuplicateStudentGradeImport(importResults);
      bucketComponent.upload(excelFile, GRADE_XLSX_IMPORT_BUCKET_KEY + excelFile.getName());
      eventProducer.accept(List.of(GradeImportEvent.builder().grades(importResults)
                      .coordinatorEmail(coordinatorEmail)
              .build()));
      return new StudentExamGradeImportValidationResult()
          .validStudentExamGradeNumber(importResults.size());
    } catch (IOException e) {
      throw new RuntimeException("Unable to read file");
    }
  }

  private void validateDuplicateStudentGradeImport(List<GradeImportDto> importResults) {
    Set<String> seenRefs = new HashSet<>();
    for (GradeImportDto dto : importResults) {
      if (!seenRefs.add(dto.getRef())) {
        throw new BadRequestException("Référence dupliqués détecté: " + dto.getRef());
      }
    }
  }
}
