package school.hei.haapi.service;

import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.endpoint.event.EventProducer;
import school.hei.haapi.endpoint.rest.mapper.GradeMapper;
import school.hei.haapi.endpoint.rest.model.ExamGradeStats;
import school.hei.haapi.endpoint.rest.model.GradeInvalidRow;
import school.hei.haapi.endpoint.rest.model.GradeValidRow;
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
import school.hei.haapi.repository.ExamRepository;
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
  private final ExamRepository examRepository;

  private static final String GRADE_XLSX_IMPORT_BUCKET_KEY = "/STUDENT_EXAM_GRADE_XLSX_IMPORT/";
  private final GradeResultService gradeResultService;

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
  public ImportGradeResult initStudentExamGradeImportFromXlsx(
      File excelFile, String examId, String comment) {
    var parser = new ExcelParser<>(GradeImportDto.class, GradeImportDto.getCellMap());
    try {
      var parseResult = parser.parseFile(excelFile, 0, CREATE_NULL_AS_BLANK);

      bucketComponent.upload(excelFile, GRADE_XLSX_IMPORT_BUCKET_KEY + excelFile.getName());
      var skippedGrades = checkSkippedRows(parseResult);
      var importResults = parseResult.parsedResult();
      var duplicateGrades = checkDuplicateGrade(importResults);
      var gradeInvalidScores = checkInvalidScore(parseResult);

      var skippedRefs = skippedGrades.stream().map(GradeImportDto::getRef).toList();
      var gradeInvalidScoreRefs = gradeInvalidScores.stream().map(GradeImportDto::getRef).toList();
      List<String> allInvalidRefs = new ArrayList<>(skippedRefs);
      allInvalidRefs.addAll(gradeInvalidScoreRefs);

      importResults =
          importResults.stream().filter(grade -> !allInvalidRefs.contains(grade.getRef())).toList();

      var existingGrades = filterExistingGrades(importResults, examId, comment);
      var duplicateRefs = duplicateGrades.stream().map(GradeImportDto::getRef).toList();
      var existingGradeRefs = existingGrades.stream().map(GradeImportDto::getRef).toList();

      allInvalidRefs.addAll(duplicateRefs);
      allInvalidRefs.addAll(existingGradeRefs);

      var gradeFiltered =
          importResults.stream()
              .filter(gradeImportDto -> !allInvalidRefs.contains(gradeImportDto.getRef()))
              .toList();

      var skippedGradesMapped = mapGradeInvalidRows(skippedGrades);
      var existingGradesMapped = mapExistingGrades(existingGrades, comment);
      var duplicateGradesMapped = mapDuplicateGrades(duplicateGrades);
      var gradeInvalidScoresMapped = mapGradeInvalidRows(gradeInvalidScores);

      List<GradeInvalidRow> allInvalidGrades = new ArrayList<>(skippedGradesMapped);
      allInvalidGrades.addAll(existingGradesMapped);
      allInvalidGrades.addAll(duplicateGradesMapped);
      allInvalidGrades.addAll(gradeInvalidScoresMapped);

      allInvalidGrades =
          allInvalidGrades.stream()
              .sorted(
                  Comparator.comparing(
                      GradeInvalidRow::getRef, Comparator.nullsLast(String::compareTo)))
              .toList();

      var totalRows =
          Stream.concat(allInvalidGrades.stream(), gradeFiltered.stream()).toList().size();
      List<GradeValidRow> validGrades;
      if (comment != null) {
        var grades = gradeMapper.toDomainList(gradeFiltered, examId, comment);
        validGrades = gradeMapper.toRestListValidGrade(updateParticipantGrade(grades));
      } else {
        var grades = gradeMapper.toDomainList(gradeFiltered, examId);
        validGrades = gradeMapper.toRestListValidGrade(createParticipantGrade(grades));
      }
      validGrades =
          validGrades.stream()
              .sorted(
                  Comparator.comparing(
                      GradeValidRow::getRef, Comparator.nullsLast(String::compareTo)))
              .toList();
      var importGradeStat =
          new ImportGradeStat()
              .totalRows(totalRows)
              .invalidRows(allInvalidGrades.size())
              .validRows(validGrades.size());

      return new ImportGradeResult()
          .importGradeStats(importGradeStat)
          .validGrades(validGrades)
          .invalidGrades(allInvalidGrades);
    } catch (IOException e) {
      throw new RuntimeException("Unable to read file");
    }
  }

  public List<GradeImportDto> checkDuplicateGrade(List<GradeImportDto> parseResult) {
    Map<String, Long> occurrences =
        parseResult.stream()
            .collect(Collectors.groupingBy(GradeImportDto::getRef, Collectors.counting()));

    return parseResult.stream().filter(dto -> occurrences.get(dto.getRef()) > 1).toList();
  }

  public List<GradeImportDto> filterExistingGrades(
      List<GradeImportDto> grades, String examId, String comment) {
    var existingGrades = new ArrayList<GradeImportDto>();
    for (GradeImportDto grade : grades) {
      var existing = gradeRepository.getGradeByExamIdAndStudentRef(examId, grade.getRef());
      if (existing.isPresent() && comment != null) {
        if (Objects.equals(grade.getScore(), existing.get().getScore())) {
          existingGrades.add(grade);
        }
      } else if (existing.isPresent() && existing.get().getScore() != null) {
        existingGrades.add(grade);
      }
    }
    return existingGrades;
  }

  public List<GradeImportDto> checkSkippedRows(ParseResult<GradeImportDto> parseResult) {
    List<GradeImportDto> allInvalids = new ArrayList<>();
    for (var entry : parseResult.skippedRows().entrySet()) {
      var row = entry.getKey();
      if (row.getRowNum() == 0) {
        continue;
      }
      Object refValue = row.getCell(0, CREATE_NULL_AS_BLANK).getStringCellValue();
      Cell scoreCell = row.getCell(1, CREATE_NULL_AS_BLANK);
      Double score = null;

      if (scoreCell != null) {
        if (scoreCell.getCellType() == NUMERIC) {
          score = scoreCell.getNumericCellValue();
        } else {
          var stringScore = scoreCell.getStringCellValue().trim();
          if (!stringScore.isBlank()) {
            score = Double.valueOf(stringScore);
          }
        }
      }
      var ref = refValue != null ? refValue.toString() : null;
      var invalid = new GradeImportDto();
      invalid.setRef(ref);
      invalid.setScore(score);
      allInvalids.add(invalid);
    }

    return allInvalids;
  }

  private List<GradeImportDto> checkInvalidScore(ParseResult<GradeImportDto> parseResult) {
    return parseResult.parsedResult().stream()
        .filter(gradeDto -> gradeDto.getScore() < 0 || gradeDto.getScore() > 20)
        .map(
            grade -> {
              var invalid = new GradeImportDto();
              invalid.setRef(grade.getRef());
              invalid.setScore(grade.getScore());
              return invalid;
            })
        .toList();
  }

  @NotNull
  private List<GradeInvalidRow> mapGradeInvalidRows(List<GradeImportDto> gradeInvalidScores) {
    var invalidGrades = new ArrayList<GradeInvalidRow>();
    gradeInvalidScores.forEach(
        gradeImportDto ->
            invalidGrades.add(
                new GradeInvalidRow()
                    .ref(gradeImportDto.getRef())
                    .score(
                        gradeImportDto.getScore() != null
                            ? BigDecimal.valueOf(gradeImportDto.getScore())
                            : null)
                    .reason(validateRow(gradeImportDto.getRef(), gradeImportDto.getScore()))));

    return invalidGrades;
  }

  public List<GradeInvalidRow> mapExistingGrades(
      List<GradeImportDto> existingGrades, String comment) {
    var invalidGrades = new ArrayList<GradeInvalidRow>();
    if (comment != null) {
      existingGrades.forEach(
          gradeImportDto ->
              invalidGrades.add(
                  new GradeInvalidRow()
                      .ref(gradeImportDto.getRef())
                      .score(BigDecimal.valueOf(gradeImportDto.getScore()))
                      .reason(
                          "La note insérée est identique à la note existante. Aucune mise à jour"
                              + " n’a été effectuée.")));
    } else {
      existingGrades.forEach(
          gradeImportDto ->
              invalidGrades.add(
                  new GradeInvalidRow()
                      .ref(gradeImportDto.getRef())
                      .score(BigDecimal.valueOf(gradeImportDto.getScore()))
                      .reason(
                          "L'étudiant(e) a déjà une note pour cet examen. Veuillez choisir l'option"
                              + " mettre à jour pour modifier.")));
    }

    return invalidGrades;
  }

  private List<GradeInvalidRow> mapDuplicateGrades(List<GradeImportDto> duplicateGrades) {
    var invalidGrades = new ArrayList<GradeInvalidRow>();
    duplicateGrades.forEach(
        gradeImportDto ->
            invalidGrades.add(
                new GradeInvalidRow()
                    .ref(gradeImportDto.getRef())
                    .score(BigDecimal.valueOf(gradeImportDto.getScore()))
                    .reason(
                        "La réference étudiant(e) est dupliquée, veuillez supprimer les autres pour"
                            + " ajouter une note.")));

    return invalidGrades;
  }

  private String validateRow(String ref, Double score) {
    if (ref == null || ref.isBlank()) return "La réference est null ou vide";
    if (score == null) return "La note est null";
    if (score > 20) return "La note est supérieur à 20";
    if (score < 0) return "La note est négative";
    return null;
  }

  public byte[] generateGradesTemplate(String examId) {
    var existingGrades = gradeRepository.getGradesByExamId(examId);
    var exam = examRepository.findById(examId);
    Map<String, Double> studentScoreAndRefs = new HashMap<>();
    for (Object[] grade : existingGrades) {
      var ref = (String) grade[0];
      var score = (Double) grade[1];
      studentScoreAndRefs.put(ref, score);
    }
    var participants = examRepository.findStudentRefsByExamId(examId);
    try (Workbook workbook = new XSSFWorkbook()) {
      String fileName = "note";
      if (exam.isPresent()) {
        fileName = String.format("note_%s", exam.get().getTitle());
      }
      Sheet sheet = workbook.createSheet(fileName);
      Row headerRow = sheet.createRow(0);
      headerRow.createCell(0).setCellValue("ref");
      headerRow.createCell(1).setCellValue("score");

      IntStream.range(0, participants.size())
          .forEach(
              i -> {
                String ref = participants.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(ref);
                Double score = studentScoreAndRefs.get(ref);
                if (score != null) {
                  row.createCell(1).setCellValue(score);
                } else {
                  row.createCell(1);
                }
              });

      sheet.autoSizeColumn(0);
      sheet.autoSizeColumn(1);
      try (ByteArrayOutputStream template = new ByteArrayOutputStream()) {
        workbook.write(template);
        return template.toByteArray();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
