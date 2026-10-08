package school.hei.haapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static school.hei.haapi.model.CycleLevel.BACHELOR;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Role.TEACHER;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.StudentBadge;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.utils.BadgeImageProcessor;
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
  private PromotionService promotionService;
  private StudentBadgeCodeService studentBadgeCodeService;
  private StudentBadgeService subject;

  @BeforeEach
  void setUp() {
    userService = mock(UserService.class);
    bucketComponent = mock(BucketComponent.class);
    promotionService = mock(PromotionService.class);
    when(promotionService.getAllStudentPromotions(anyString())).thenReturn(new LinkedHashSet<>());
    studentBadgeCodeService = mock(StudentBadgeCodeService.class);
    when(studentBadgeCodeService.newBadge(any(), any(), anyBoolean()))
        .thenAnswer(
            invocation ->
                StudentBadge.builder()
                    .student(invocation.getArgument(0))
                    .publicId(UUID.randomUUID().toString())
                    .build());
    var schoolYearSupplier = new SchoolYearSupplier();
    subject =
        new StudentBadgeService(
            userService,
            studentBadgeCodeService,
            new StudentLevelResolver(promotionService, schoolYearSupplier),
            new BadgeImageProcessor(bucketComponent, new ClassPathResourceResolver()),
            new QrCodeGenerator(),
            new HtmlParser(),
            new PdfRenderer(),
            schoolYearSupplier,
            "https://admin.hei.school/badges/");
  }

  @Test
  void generate_badges_ten_per_page() throws Exception {
    var students =
        IntStream.rangeClosed(1, 12)
            .mapToObj(i -> student("student" + i + "_id", "STD2600" + i, i % 3 != 0))
            .toList();
    students.get(0).setLastName("RANDRIANARIVELONDRAZAFINDRAKOTO");
    students.get(0).setFirstName("Jean Marc Hery Andrianina");
    when(userService.getByGroupId(eq(GROUP_ID), any())).thenReturn(students);
    when(bucketComponent.download(anyString())).thenAnswer(invocation -> aPhoto());

    var pdf = subject.generateBadges(GROUP_ID, null, "2026 - 2027");
    writePreview(pdf);

    var reader = new PdfReader(pdf);
    assertEquals(2, reader.getNumberOfPages());
    var firstPage = new PdfTextExtractor(reader).getTextFromPage(1);
    assertTrue(firstPage.contains("STD26001"));
    assertTrue(firstPage.contains("Année universitaire 2026 - 2027"));
    var secondPage = new PdfTextExtractor(reader).getTextFromPage(2);
    assertTrue(secondPage.contains("RANDRIANARIVELO"));
    assertFalse(firstPage.contains("RANDRIANARIVELO"));
  }

  @Test
  void badge_shows_level_of_printed_academic_year() throws Exception {
    var promotion =
        Promotion.builder()
            .id("promotion_2025")
            .startDatetime(Instant.parse("2025-11-01T00:00:00Z"))
            .cycleLevel(BACHELOR)
            .build();
    when(promotionService.getAllStudentPromotions("student1_id"))
        .thenReturn(new LinkedHashSet<>(List.of(promotion)));
    when(userService.getByGroupId(eq(GROUP_ID), any()))
        .thenReturn(List.of(student("student1_id", "STD25001", false)));

    var l2Badge = textOfFirstPage(subject.generateBadges(GROUP_ID, null, "2026 - 2027"));
    var l1Badge = textOfFirstPage(subject.generateBadges(GROUP_ID, null, "2025 - 2026"));

    assertTrue(l2Badge.contains("L2"));
    assertTrue(l1Badge.contains("L1"));
  }

  @Test
  void generate_badges_even_when_photo_cannot_be_downloaded() throws Exception {
    when(userService.getByRoleAndIds(List.of(STUDENT), List.of("student1_id")))
        .thenReturn(List.of(student("student1_id", "STD26001", true)));
    when(bucketComponent.download(anyString())).thenThrow(new RuntimeException("NoSuchKey"));

    var pdf = subject.generateBadges(null, List.of("student1_id"), null);

    assertEquals(1, new PdfReader(pdf).getNumberOfPages());
  }

  @Test
  void students_with_an_active_badge_are_not_printed_again() throws Exception {
    var withBadge = student("student1_id", "STD26001", false);
    var withoutBadge = student("student2_id", "STD26002", false);
    when(userService.getByGroupId(eq(GROUP_ID), any()))
        .thenReturn(List.of(withBadge, withoutBadge));
    when(studentBadgeCodeService.hasActiveBadge(eq(withBadge), any())).thenReturn(true);

    var page = textOfFirstPage(subject.generateBadges(GROUP_ID, null, "2026 - 2027"));

    assertTrue(page.contains("STD26002"));
    assertFalse(page.contains("STD26001"));
    verify(studentBadgeCodeService, never()).newBadge(eq(withBadge), any(), anyBoolean());
  }

  @Test
  void students_who_left_the_school_get_no_badge() throws Exception {
    var left = student("student1_id", "STD26001", false);
    left.setStatus(User.Status.DISABLED);
    var stillThere = student("student2_id", "STD26002", false);
    when(userService.getByGroupId(eq(GROUP_ID), any())).thenReturn(List.of(left, stillThere));

    var page = textOfFirstPage(subject.generateBadges(GROUP_ID, null, "2026 - 2027"));

    assertTrue(page.contains("STD26002"));
    assertFalse(page.contains("STD26001"));
    verify(studentBadgeCodeService, never()).newBadge(eq(left), any(), anyBoolean());
  }

  @Test
  void badges_are_saved_all_at_once_once_their_pdf_is_ready() {
    var students =
        IntStream.rangeClosed(1, 3)
            .mapToObj(i -> student("student" + i + "_id", "STD2600" + i, true))
            .toList();
    when(userService.getByGroupId(eq(GROUP_ID), any())).thenReturn(students);
    when(bucketComponent.download(anyString())).thenAnswer(invocation -> aPhoto());

    subject.generateBadges(GROUP_ID, null, "2026 - 2027");

    verify(studentBadgeCodeService).saveBadges(argThat(badges -> badges.size() == students.size()));
  }

  @Test
  void no_badge_is_saved_when_the_generation_fails() {
    var students =
        List.of(
            student("student1_id", "STD26001", false), student("student2_id", "STD26002", false));
    when(userService.getByGroupId(eq(GROUP_ID), any())).thenReturn(students);
    when(studentBadgeCodeService.newBadge(eq(students.get(1)), any(), anyBoolean()))
        .thenThrow(new IllegalStateException("database down"));

    assertThrows(
        IllegalStateException.class, () -> subject.generateBadges(GROUP_ID, null, "2026 - 2027"));
    verify(studentBadgeCodeService, never()).saveBadges(any());
  }

  @Test
  void generate_badges_ko_when_every_student_has_an_active_badge() {
    when(userService.getByRoleAndIds(List.of(STUDENT), List.of("student1_id")))
        .thenReturn(List.of(student("student1_id", "STD26001", false)));
    when(studentBadgeCodeService.hasActiveBadge(any(), any())).thenReturn(true);

    assertThrows(
        BadRequestException.class,
        () -> subject.generateBadges(null, List.of("student1_id"), "2026 - 2027"));
  }

  @Test
  void generate_badges_ko_without_students() {
    assertThrows(BadRequestException.class, () -> subject.generateBadges(null, null, null));
    assertThrows(BadRequestException.class, () -> subject.generateBadges(" ", List.of(), null));

    var teacher = student("teacher1_id", "TCR26001", false);
    teacher.setRole(TEACHER);
    when(userService.getByGroupId(eq(GROUP_ID), any())).thenReturn(List.of(teacher));
    assertThrows(BadRequestException.class, () -> subject.generateBadges(GROUP_ID, null, null));
  }

  @Test
  void generate_badges_ko_with_invalid_academic_year() {
    when(userService.getByGroupId(eq(GROUP_ID), any()))
        .thenReturn(List.of(student("student1_id", "STD26001", false)));

    assertThrows(BadRequestException.class, () -> subject.generateBadges(GROUP_ID, null, "2026"));
    assertThrows(
        BadRequestException.class, () -> subject.generateBadges(GROUP_ID, null, "2026 - 2028"));
  }

  @Test
  void badge_has_no_expiration_only_after_licence() {
    var graduated = student("student1_id", "STD23001", false);
    var l3 = student("student2_id", "STD24001", false);
    when(promotionService.getAllStudentPromotions("student1_id"))
        .thenReturn(new LinkedHashSet<>(List.of(bachelorPromotion("2023-11-01T00:00:00Z"))));
    when(promotionService.getAllStudentPromotions("student2_id"))
        .thenReturn(new LinkedHashSet<>(List.of(bachelorPromotion("2024-11-01T00:00:00Z"))));
    when(userService.getByGroupId(eq(GROUP_ID), any())).thenReturn(List.of(graduated, l3));

    subject.generateBadges(GROUP_ID, null, "2026 - 2027");

    verify(studentBadgeCodeService).newBadge(eq(graduated), any(), eq(true));
    verify(studentBadgeCodeService).newBadge(eq(l3), any(), eq(false));
  }

  @Test
  void qr_code_links_to_badge_page_of_public_id_never_to_student_id() {
    assertEquals(
        "https://admin.hei.school/badges/7c1e4b9a-2f3d-4e8a-9b6c-1d2e3f4a5b6c",
        subject.qrCodeUrlOf("7c1e4b9a-2f3d-4e8a-9b6c-1d2e3f4a5b6c"));
  }

  private static Promotion bachelorPromotion(String startDatetime) {
    return Promotion.builder()
        .id("promotion_" + startDatetime)
        .startDatetime(Instant.parse(startDatetime))
        .cycleLevel(BACHELOR)
        .build();
  }

  private static String textOfFirstPage(byte[] pdf) throws IOException {
    return new PdfTextExtractor(new PdfReader(pdf)).getTextFromPage(1);
  }

  private static void writePreview(byte[] pdf) throws IOException {
    Files.createDirectories(PREVIEW.getParent());
    try {
      Files.write(PREVIEW, pdf);
    } catch (IOException e) {
      var fallback =
          PREVIEW.resolveSibling("badges-preview-" + System.currentTimeMillis() + ".pdf");
      Files.write(fallback, pdf);
    }
  }

  private static User student(String id, String ref, boolean hasPhoto) {
    var student = new User();
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

  private static File aPhoto() throws IOException {
    var image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
    var graphics = image.createGraphics();
    graphics.setPaint(new GradientPaint(0, 0, new Color(40, 90, 160), 800, 600, Color.ORANGE));
    graphics.fillRect(0, 0, 800, 600);
    graphics.setColor(Color.WHITE);
    graphics.fillOval(300, 150, 200, 260);
    graphics.dispose();
    var file = File.createTempFile("photo", ".jpg");
    ImageIO.write(image, "jpg", file);
    return file;
  }
}
