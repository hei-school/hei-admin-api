package school.hei.haapi.service;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static school.hei.haapi.integration.StudentIT.student1;
import static school.hei.haapi.integration.conf.TestUtils.getMockedFile;
import static school.hei.haapi.integration.conf.TestUtils.setUpCasdoor;
import static school.hei.haapi.integration.conf.TestUtils.setUpCognito;
import static school.hei.haapi.integration.conf.TestUtils.setUpS3Service;
import static school.hei.haapi.integration.test_data.CourseAssignmentTestData.createCourseAssignment;
import static school.hei.haapi.integration.test_data.CourseTestData.prog1;
import static school.hei.haapi.integration.test_data.CourseTestData.prog2;
import static school.hei.haapi.integration.test_data.ExamTestData.createExam;
import static school.hei.haapi.integration.test_data.GroupTestData.createGroupFlow;
import static school.hei.haapi.integration.test_data.GroupTestData.g1;
import static school.hei.haapi.integration.test_data.GroupTestData.g2;
import static school.hei.haapi.integration.test_data.MonitorTestData.monitorOfAxel;
import static school.hei.haapi.integration.test_data.MonitorTestData.monitorOfTolojanahary;
import static school.hei.haapi.integration.test_data.StudentTestData.axel;
import static school.hei.haapi.integration.test_data.StudentTestData.tolojanahary;
import static school.hei.haapi.integration.test_data.TeacherTestData.toky;
import static school.hei.haapi.model.dto.MonitorStudentLinkDto.Status.LINKED;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.model.Course;
import school.hei.haapi.model.CourseAssignment;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.CourseAssignmentRepository;
import school.hei.haapi.repository.CourseRepository;
import school.hei.haapi.repository.ExamRepository;
import school.hei.haapi.repository.GradeRepository;
import school.hei.haapi.repository.GroupFlowRepository;
import school.hei.haapi.repository.GroupRepository;
import school.hei.haapi.repository.MonitoringStudentRepository;
import school.hei.haapi.repository.UserRepository;

@Slf4j
public class GradeImportTest extends FacadeITMockedThirdParties {
  @Autowired private GradeService subject;
  @MockBean BucketComponent bucketComponent;
  @Autowired private UserService userService;
  private static User studentTolojanahary;
  private static User studentAxel;
  private static User monitorOfAxel;
  private static User monitorOfTolojanahary;
  private static Course courseProg1;
  private static Course courseProg2;
  private static User teacherToky;
  private static Exam exam2Prog1;
  private static CourseAssignment assignProg1ToTokyForGroup;
  private static CourseAssignment assignProg2ToTokyForGroup2;
  private static Group groupG1;
  private static Group groupG2;
  private static GroupFlow groupFlowsAxel;
  private static GroupFlow groupFlowsTolojanahary;
  private static String exam2prog1Id;
  @Autowired private GradeRepository gradeRepository;

  @BeforeAll
  static void setUpTestData(
      @Autowired GradeRepository gradeRepository,
      @Autowired UserRepository userRepository,
      @Autowired CourseRepository courseRepository,
      @Autowired GroupFlowRepository groupFlowRepository,
      @Autowired GroupRepository groupRepository,
      @Autowired CourseAssignmentRepository courseAssignmentRepository,
      @Autowired MonitoringStudentRepository monitoringStudentRepository,
      @Autowired ExamRepository examRepository) {
    groupG1 = g1();
    groupG2 = g2();
    studentAxel = axel();
    studentAxel.setRef("STD22033");
    studentTolojanahary = tolojanahary();
    studentTolojanahary.setRef("STD22031");
    courseProg1 = prog1();
    courseProg2 = prog2();
    teacherToky = toky();
    assignProg1ToTokyForGroup = createCourseAssignment(courseProg1, teacherToky, List.of(groupG1));
    assignProg2ToTokyForGroup2 = createCourseAssignment(courseProg2, teacherToky, List.of(groupG2));

    monitorOfAxel = monitorOfAxel();
    monitorOfTolojanahary = monitorOfTolojanahary();
    studentAxel.setMonitors(List.of(monitorOfAxel));
    studentTolojanahary.setMonitors(List.of(monitorOfTolojanahary));
    groupFlowsAxel = createGroupFlow(studentAxel, groupG1);
    groupFlowsTolojanahary = createGroupFlow(studentTolojanahary, groupG1);

    exam2Prog1 = createExam(Instant.parse("2025-09-22T10:15:30Z"), assignProg1ToTokyForGroup);

    groupRepository.saveAll(List.of(groupG1, groupG2));
    userRepository.saveAll(List.of(monitorOfAxel, monitorOfTolojanahary));
    userRepository.saveAll(List.of(studentAxel, studentTolojanahary));
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfAxel.getId(), List.of(studentAxel.getId()), LINKED.toString());
    monitoringStudentRepository.saveMonitorFollowingStudents(
        monitorOfTolojanahary.getId(), List.of(studentTolojanahary.getId()), LINKED.toString());
    userRepository.saveAll(List.of(teacherToky));
    courseRepository.saveAll(List.of(courseProg1, courseProg2));
    groupFlowRepository.saveAll(List.of(groupFlowsAxel, groupFlowsTolojanahary));
    courseAssignmentRepository.saveAll(
        List.of(assignProg1ToTokyForGroup, assignProg2ToTokyForGroup2));
    exam2prog1Id = examRepository.save(exam2Prog1).getId();
  }

  @BeforeEach
  void setUp() {
    setUpCasdoor(casdoorAuthServiceMock, certificateLoaderMock);
    setUpCognito(cognitoComponentMock);
    setUpS3Service(fileService, student1());
  }

  @Test
  void validate_import_student_grade_ok() {
    var importResult =
        subject.initStudentExamGradeImportFromXlsx(
            getMockedFile("test-grade-import", ".xlsx"), exam2prog1Id, null);
    assertNotNull(importResult.getImportGradeStats());
    assertEquals(8, importResult.getImportGradeStats().getTotalRows());
    assertEquals(6, importResult.getImportGradeStats().getInvalidRows());
    assertEquals(2, importResult.getImportGradeStats().getValidRows());
    assertNotNull(importResult.getInvalidGrades());
    assertEquals("La note est supérieur à 20", importResult.getInvalidGrades().get(4).getReason());
    assertEquals("La note est négative", importResult.getInvalidGrades().get(5).getReason());
    assertEquals("La note est null", importResult.getInvalidGrades().get(1).getReason());
    assertEquals(
        "La réference est null ou vide", importResult.getInvalidGrades().getFirst().getReason());
    assertEquals(
        "La réference étudiant(e) est dupliquée, veuillez supprimer les autres pour ajouter une"
            + " note.",
        importResult.getInvalidGrades().get(2).getReason());
  }

  @Test
  void update_grade_via_excel_file_OK() {
    var updateGrades =
        subject.initStudentExamGradeImportFromXlsx(
            getMockedFile("test-update-grade", ".xlsx"), exam2prog1Id, "test comment");
    assertNotNull(updateGrades);
    assertNotNull(updateGrades.getInvalidGrades());
    assertNotNull(updateGrades.getImportGradeStats());
    assertEquals(8, updateGrades.getImportGradeStats().getTotalRows());
    assertEquals(7, updateGrades.getImportGradeStats().getInvalidRows());
    assertEquals(1, updateGrades.getImportGradeStats().getValidRows());
  }

  @Test
  void generateGradesTemplate_returns_file() throws IOException {
    byte[] file = subject.generateGradesTemplate(exam2prog1Id);

    assertNotNull(file);
    assertNotEquals(0, file.length);

    try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
      Sheet sheet = workbook.getSheetAt(0);
      Row headerRow = sheet.getRow(0);

      assertEquals("ref", headerRow.getCell(0).getStringCellValue());
      assertEquals("score", headerRow.getCell(1).getStringCellValue());
    }
  }
}
