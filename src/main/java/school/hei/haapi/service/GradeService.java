package school.hei.haapi.service;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

import jakarta.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.ExamGradeStats;
import school.hei.haapi.endpoint.rest.model.GradeInvalidRow;
import school.hei.haapi.endpoint.rest.model.ImportGradeResult;
import school.hei.haapi.endpoint.rest.model.ImportGradeStat;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.dto.GradeImportDto;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.notEntity.UpdateGrade;
import school.hei.haapi.model.validator.IsNewGradeChecker;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.dao.GradeDao;
import school.hei.haapi.service.utils.excel.ExcelParser;
import school.hei.haapi.service.utils.excel.ParseResult;

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
  private final GradeMapper gradeMapper;

  private static final String GRADE_XLSX_IMPORT_BUCKET_KEY = "/STUDENT_EXAM_GRADE_XLSX_IMPORT/";

  public Grade getGradeByExamIdAndStudentRef(String examId, String ref) {
    return gradeRepository
        .getGradeByExamIdAndStudentRef(examId, ref)
        .orElseThrow(() -> new NotFoundException("Grade not found"));
  }

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

  @Transactional
  public ImportGradeResult initStudentExamGradeImportFromXlsx(File excelFile, String examId) {
    var parser = new ExcelParser<>(GradeImportDto.class, GradeImportDto.getCellMap());
    try {
      var parseResult = parser.parseFile(excelFile, 0, CREATE_NULL_AS_BLANK);
      var invalids = checkAllRows(parseResult);
      var importResults = parseResult.parsedResult();
      bucketComponent.upload(excelFile, GRADE_XLSX_IMPORT_BUCKET_KEY + excelFile.getName());
      var grades = gradeMapper.toDomainList(importResults, examId);
      var valids = gradeMapper.toRestListValidGrade(createParticipantGrade(grades));
      var importGradeStat =
          new ImportGradeStat()
              .totalRows(invalids.size() + valids.size())
              .invalidRows(invalids.size())
              .validRows(valids.size());
      return new ImportGradeResult()
          .importGradeStats(importGradeStat)
          .validGrades(valids)
          .invalidGrades(invalids);
    } catch (IOException e) {
      throw new RuntimeException("Unable to read file");
    }
  }

  public List<GradeInvalidRow> checkAllRows(ParseResult<GradeImportDto> parseResult) {
    List<GradeInvalidRow> allInvalids = new ArrayList<>();
    Set<String> existingRefs = new HashSet<>();

    var iter = parseResult.parsedResult().iterator();
    while (iter.hasNext()) {
      var row = iter.next();
      var ref = row.getRef();
      var score = BigDecimal.valueOf(row.getScore());

      var reason = validateRow(ref, score, existingRefs);

      if (reason != null) {
        var invalid = new GradeInvalidRow().ref(ref).score(score).reason(reason);
        allInvalids.add(invalid);
        iter.remove();
      } else {
        existingRefs.add(ref);
      }
    }

    for (var entry : parseResult.skippedRows().entrySet()) {
      var row = entry.getKey();

      if (row.getRowNum() == 0) {
        continue;
      }
      var refValue = getCellValue(row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
      var scoreValue = getCellValue(row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
      BigDecimal score = null;
      if (scoreValue instanceof Number n) score = BigDecimal.valueOf(n.doubleValue());
      else if (scoreValue instanceof String s) {
        try {
          score = new BigDecimal(s);
        } catch (Exception ignored) {
        }
      }
      var ref = refValue != null ? refValue.toString() : null;
      var reason = validateRow(ref, score, existingRefs);
      GradeInvalidRow invalid = new GradeInvalidRow().ref(ref).score(score).reason(reason);
      allInvalids.add(invalid);
    }

    return allInvalids.stream()
        .map(
            row -> {
              row.setRef(row.getRef());
              row.setScore(row.getScore());
              row.setReason(row.getReason());
              return row;
            })
        .toList();
  }

  private String validateRow(String ref, BigDecimal score, Set<String> existingRefs) {
    if (ref == null || ref.isBlank()) return "La réference est null ou vide";
    if (existingRefs.contains(ref)) return "La réference est dupliquée";
    if (score == null) return "La note est null";
    if (score.compareTo(BigDecimal.valueOf(20)) > 0) return "La note est supérieur à 20";
    if (score.compareTo(BigDecimal.ZERO) < 0) return "La note est négative";
    return null;
  }

  private Object getCellValue(Cell cell) {
    if (cell == null) return null;

    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue();
      case NUMERIC -> cell.getNumericCellValue();
      case BOOLEAN -> cell.getBooleanCellValue();
      case FORMULA -> cell.getCellFormula();
      default -> null;
    };
  }
}
