package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static school.hei.haapi.model.CycleLevel.BACHELOR;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Role.TEACHER;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.Badge;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.StudentBadge;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.utils.ClassPathResourceResolver;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PdfRenderer;
import school.hei.haapi.service.utils.QrCodeGenerator;
import school.hei.haapi.service.utils.SchoolYearSupplier;

class StudentBadgeServiceTest {
  private static final String GROUP_ID = "group1_id";
  private static final Path PREVIEW = Path.of("build", "badges-preview.pdf");
  private UserService userService;
  private BucketComponent bucketComponent;
  private StudentBadgeService subject;

  @BeforeEach
  void setUp() {
    userService = mock(UserService.class);
    bucketComponent = mock(BucketComponent.class);
    StudentBadgeCodeService studentBadgeCodeService = mock(StudentBadgeCodeService.class);
    when(studentBadgeCodeService.getOrCreateActiveBadge(any()))
        .thenAnswer(
            invocation ->
                StudentBadge.builder()
                    .student(invocation.getArgument(0))
                    .publicId(java.util.UUID.randomUUID().toString())
                    .build());
    subject =
        new StudentBadgeService(
            userService,
            bucketComponent,
            new HtmlParser(),
            new PdfRenderer(),
            new QrCodeGenerator(),
            new ClassPathResourceResolver(),
            new SchoolYearSupplier(),
            studentBadgeCodeService,
            "https://admin.hei.school/students/");
  }

  @Test
  void generate_badges_ten_per_page() throws Exception {
    List<User> students =
        IntStream.rangeClosed(1, 12)
            .mapToObj(i -> student("student" + i + "_id", "STD2600" + i, i % 3 != 0))
            .toList();
    students.get(0).setLastName("RANDRIANARIVELONDRAZAFINDRAKOTO");
    students.get(0).setFirstName("Jean Marc Hery Andrianina");
    when(userService.getByGroupId(eq(GROUP_ID), any())).thenReturn(students);
    when(bucketComponent.download(anyString())).thenAnswer(invocation -> aPhoto());

    byte[] pdf = subject.generateBadges(GROUP_ID, null, "2026 - 2027");
    writePreview(pdf);

    PdfReader reader = new PdfReader(pdf);
    assertEquals(2, reader.getNumberOfPages());
    String firstPage = new PdfTextExtractor(reader).getTextFromPage(1);
    assertTrue(firstPage.contains("STD26001"));
    assertTrue(firstPage.contains("Année universitaire 2026 - 2027"));
    String secondPage = new PdfTextExtractor(reader).getTextFromPage(2);
    assertTrue(secondPage.contains("RANDRIANARIVELO"));
    assertFalse(firstPage.contains("RANDRIANARIVELO"));
  }

  @Test
  void badge_shows_level_of_printed_academic_year() throws Exception {
    User student = student("student1_id", "STD25001", false);
    Promotion promotion =
        Promotion.builder()
            .id("promotion1_id")
            .startDatetime(Instant.parse("2025-11-01T00:00:00Z"))
            .cycleLevel(BACHELOR)
            .build();
    Group group = Group.builder().id(GROUP_ID).promotion(promotion).build();
    student.setGroupFlows(
        List.of(
            GroupFlow.builder()
                .student(student)
                .group(group)
                .groupFlowType(JOIN)
                .flowDatetime(Instant.parse("2025-11-01T00:00:00Z"))
                .build()));
    when(userService.getByGroupId(eq(GROUP_ID), any())).thenReturn(List.of(student));

    String l2Badge =
        new PdfTextExtractor(new PdfReader(subject.generateBadges(GROUP_ID, null, "2026 - 2027")))
            .getTextFromPage(1);
    String l1Badge =
        new PdfTextExtractor(new PdfReader(subject.generateBadges(GROUP_ID, null, "2025 - 2026")))
            .getTextFromPage(1);

    assertTrue(l2Badge.contains("L2"));
    assertTrue(l1Badge.contains("L1"));
  }

  @Test
  void level_instant_is_in_november_of_academic_year_start() {
    assertEquals(
        2026,
        StudentBadgeService.levelInstantOf("2026 - 2027").atZone(ZoneId.systemDefault()).getYear());
    assertEquals(
        11,
        StudentBadgeService.levelInstantOf("2026 - 2027")
            .atZone(ZoneId.systemDefault())
            .getMonthValue());
  }

  @Test
  void long_last_names_use_smaller_font() {
    assertEquals("10pt", StudentBadgeService.lastNameFontSize("RAKOTOARIVELO"));
    assertEquals("8.5pt", StudentBadgeService.lastNameFontSize("RANDRIANARIVELO"));
    assertEquals("8pt", StudentBadgeService.lastNameFontSize("ANDRIAMANOHINIAINA"));
    assertEquals("7.5pt", StudentBadgeService.lastNameFontSize("ANDRIAMPARANIMAHEFA"));
    assertEquals("7pt", StudentBadgeService.lastNameFontSize("RANDRIANARIVELONDRAZAF"));
    assertEquals("10pt", StudentBadgeService.lastNameFontSize("RAKOTO ANDRIAMANANA"));
    assertEquals("6pt", StudentBadgeService.lastNameFontSize("RANDRIANARIVELONDRAZAFINDRAKOTO"));
  }

  @Test
  void generate_badges_even_when_photo_cannot_be_downloaded() throws Exception {
    when(userService.getByRoleAndIds(List.of(STUDENT), List.of("student1_id")))
        .thenReturn(List.of(student("student1_id", "STD26001", true)));
    when(bucketComponent.download(anyString())).thenThrow(new RuntimeException("NoSuchKey"));

    byte[] pdf = subject.generateBadges(null, List.of("student1_id"), null);

    assertEquals(1, new PdfReader(pdf).getNumberOfPages());
  }

  @Test
  void generate_badges_ko_without_students() {
    assertThrows(BadRequestException.class, () -> subject.generateBadges(null, null, null));
    assertThrows(BadRequestException.class, () -> subject.generateBadges(" ", List.of(), null));

    User teacher = student("teacher1_id", "TCR26001", false);
    teacher.setRole(TEACHER);
    when(userService.getByGroupId(eq(GROUP_ID), any())).thenReturn(List.of(teacher));
    assertThrows(BadRequestException.class, () -> subject.generateBadges(GROUP_ID, null, null));
  }

  @Test
  void qr_code_links_to_public_id_not_to_student_id() {
    assertEquals(
        "https://admin.hei.school/students/7c1e4b9a-2f3d-4e8a-9b6c-1d2e3f4a5b6c",
        subject.qrCodeUrlOf("7c1e4b9a-2f3d-4e8a-9b6c-1d2e3f4a5b6c"));
  }

  @Test
  void split_badges_by_pages_and_rows() {
    List<Badge> badges =
        IntStream.range(0, 23)
            .mapToObj(i -> new Badge("L", "F", "R" + i, "L1", null, "", "9pt"))
            .toList();

    var pages = StudentBadgeService.toPages(badges);

    assertEquals(3, pages.size());
    assertEquals(5, pages.get(0).size());
    assertEquals(2, pages.get(2).size());
    assertEquals(1, pages.get(2).get(1).size());
    assertEquals("R22", pages.get(2).get(1).get(0).getRef());
  }

  private static void writePreview(byte[] pdf) throws IOException {
    Files.createDirectories(PREVIEW.getParent());
    try {
      Files.write(PREVIEW, pdf);
    } catch (IOException e) {
      Path fallback =
          PREVIEW.resolveSibling("badges-preview-" + System.currentTimeMillis() + ".pdf");
      Files.write(fallback, pdf);
    }
  }

  private static User student(String id, String ref, boolean hasPhoto) {
    User student = new User();
    student.setId(id);
    student.setRef(ref);
    student.setFirstName("Prénom " + ref);
    student.setLastName("Rakoto");
    student.setRole(STUDENT);
    if (hasPhoto) {
      student.setProfilePictureKey("STUDENT/" + ref + "/PROFILE_PICTURE_" + ref + ".jpg");
    }
    return student;
  }

  private static File aPhoto() throws Exception {
    BufferedImage image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = image.createGraphics();
    graphics.setPaint(new GradientPaint(0, 0, new Color(40, 90, 160), 800, 600, Color.ORANGE));
    graphics.fillRect(0, 0, 800, 600);
    graphics.setColor(Color.WHITE);
    graphics.fillOval(300, 150, 200, 260);
    graphics.dispose();
    File file = File.createTempFile("photo", ".jpg");
    ImageIO.write(image, "jpg", file);
    return file;
  }
}
