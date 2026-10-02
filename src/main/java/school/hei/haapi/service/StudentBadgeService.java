package school.hei.haapi.service;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static org.springframework.data.domain.Pageable.unpaged;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.Badge;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.utils.AcademicYear;
import school.hei.haapi.service.utils.ClassPathResourceResolver;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PdfRenderer;
import school.hei.haapi.service.utils.QrCodeGenerator;
import school.hei.haapi.service.utils.SchoolYearSupplier;

@Slf4j
@Service
public class StudentBadgeService {
  public static final int BADGES_PER_ROW = 2;
  public static final int ROWS_PER_PAGE = 5;
  public static final int MAX_BADGES_PER_REQUEST = 200;
  private static final String TEMPLATE = "studentBadges";
  private static final int PHOTO_WIDTH_IN_PIXELS = 300;
  private static final int PHOTO_HEIGHT_IN_PIXELS = 400;
  private static final int PHOTO_CORNER_RADIUS_IN_PIXELS = 36;
  private static final int LOGO_WIDTH_IN_PIXELS = 360;

  private final UserService userService;
  private final BucketComponent bucketComponent;
  private final HtmlParser htmlParser;
  private final PdfRenderer pdfRenderer;
  private final QrCodeGenerator qrCodeGenerator;
  private final ClassPathResourceResolver classPathResourceResolver;
  private final SchoolYearSupplier schoolYearSupplier;
  private final StudentBadgeCodeService studentBadgeCodeService;
  private final PromotionService promotionService;
  private final String qrCodeBaseUrl;

  @Autowired
  public StudentBadgeService(
      UserService userService,
      BucketComponent bucketComponent,
      HtmlParser htmlParser,
      PdfRenderer pdfRenderer,
      QrCodeGenerator qrCodeGenerator,
      ClassPathResourceResolver classPathResourceResolver,
      SchoolYearSupplier schoolYearSupplier,
      StudentBadgeCodeService studentBadgeCodeService,
      PromotionService promotionService,
      @Value("${badge.qr.base-url}") String qrCodeBaseUrl) {
    this.userService = userService;
    this.bucketComponent = bucketComponent;
    this.htmlParser = htmlParser;
    this.pdfRenderer = pdfRenderer;
    this.qrCodeGenerator = qrCodeGenerator;
    this.classPathResourceResolver = classPathResourceResolver;
    this.schoolYearSupplier = schoolYearSupplier;
    this.studentBadgeCodeService = studentBadgeCodeService;
    this.promotionService = promotionService;
    this.qrCodeBaseUrl =
        qrCodeBaseUrl.endsWith("/")
            ? qrCodeBaseUrl.substring(0, qrCodeBaseUrl.length() - 1)
            : qrCodeBaseUrl;
    ImageIO.scanForPlugins();
  }

  public byte[] generateBadges(String groupId, List<String> studentIds, String academicYear) {
    List<User> students = findStudents(groupId, studentIds);
    if (students.isEmpty()) {
      throw new BadRequestException("No student found for the given group_id or student_ids");
    }
    if (students.size() > MAX_BADGES_PER_REQUEST) {
      throw new BadRequestException(
          "Cannot generate more than " + MAX_BADGES_PER_REQUEST + " badges at once");
    }

    AcademicYear printedAcademicYear =
        AcademicYear.parse(
            academicYear == null || academicYear.isBlank()
                ? schoolYearSupplier.get()
                : academicYear);
    // no second badge in circulation: students with an active badge are not printed again
    List<User> studentsWithoutBadge =
        students.stream()
            .filter(
                student -> !studentBadgeCodeService.hasActiveBadge(student, printedAcademicYear))
            .toList();
    if (studentsWithoutBadge.isEmpty()) {
      throw new BadRequestException(
          (students.size() == 1
                  ? "The student already has"
                  : "All the " + students.size() + " students already have")
              + " an active badge for "
              + printedAcademicYear.label()
              + ": remove a badge before printing a new one");
    }
    List<Badge> badges =
        studentsWithoutBadge.stream()
            .map(student -> toBadge(student, printedAcademicYear))
            .toList();
    Context context = new Context();
    context.setVariable("pages", toPages(badges));
    context.setVariable("academic_year", printedAcademicYear.label());
    context.setVariable("logo", loadLogo());
    return pdfRenderer.apply(htmlParser.apply(TEMPLATE, context));
  }

  private List<User> findStudents(String groupId, List<String> studentIds) {
    boolean hasGroup = groupId != null && !groupId.isBlank();
    boolean hasIds = studentIds != null && !studentIds.isEmpty();
    if (!hasGroup && !hasIds) {
      throw new BadRequestException("Either group_id or student_ids must be provided");
    }

    List<User> students = new ArrayList<>();
    if (hasGroup) {
      students.addAll(userService.getByGroupId(groupId, unpaged()));
    }
    if (hasIds) {
      students.addAll(userService.getByRoleAndIds(List.of(STUDENT), studentIds));
    }
    Comparator<String> nullSafe = nullsLast(String.CASE_INSENSITIVE_ORDER);
    Set<String> seenIds = new HashSet<>();
    return students.stream()
        .filter(student -> STUDENT.equals(student.getRole()))
        .filter(student -> seenIds.add(student.getId()))
        .sorted(
            comparing(User::getLastName, nullSafe)
                .thenComparing(User::getFirstName, nullSafe)
                .thenComparing(User::getRef, nullSafe))
        .toList();
  }

  static List<List<List<Badge>>> toPages(List<Badge> badges) {
    int badgesPerPage = BADGES_PER_ROW * ROWS_PER_PAGE;
    List<List<List<Badge>>> pages = new ArrayList<>();
    for (int pageStart = 0; pageStart < badges.size(); pageStart += badgesPerPage) {
      List<List<Badge>> rows = new ArrayList<>();
      int pageEnd = Math.min(pageStart + badgesPerPage, badges.size());
      for (int rowStart = pageStart; rowStart < pageEnd; rowStart += BADGES_PER_ROW) {
        rows.add(badges.subList(rowStart, Math.min(rowStart + BADGES_PER_ROW, pageEnd)));
      }
      pages.add(rows);
    }
    return pages;
  }

  private Badge toBadge(User student, AcademicYear academicYear) {
    String lastName = nullToEmpty(student.getLastName()).toUpperCase(Locale.FRENCH);
    return new Badge(
        lastName,
        nullToEmpty(student.getFirstName()),
        nullToEmpty(student.getRef()),
        // level of the printed year: badges printed before its start show the new level
        findLevel(student, academicYear.levelInstant()).map(StudentLevel::name).orElse(null),
        loadPhoto(student).orElse(null),
        qrCodeGenerator.apply(
            qrCodeUrlOf(studentBadgeCodeService.createBadge(student, academicYear).getPublicId())),
        lastNameFontSize(lastName));
  }

  public Optional<StudentLevel> findLevel(User student, Instant levelInstant) {
    try {
      List<Promotion> promotions =
          new ArrayList<>(promotionService.getAllStudentPromotions(student.getId()));
      for (int i = promotions.size() - 1; i >= 0; i--) {
        Optional<StudentLevel> level = promotions.get(i).findLevelAt(levelInstant);
        if (level.isPresent()) {
          return level;
        }
      }
      log.info(
          "No level for student {} at {}, promotions: {}",
          student.getRef(),
          levelInstant,
          promotions.stream().map(Promotion::getRef).toList());
    } catch (RuntimeException e) {
      log.warn("Cannot compute level of student {}: {}", student.getRef(), e.getMessage());
    }
    return Optional.empty();
  }

  public Optional<StudentLevel> findCurrentLevel(User student) {
    return findLevel(student, AcademicYear.parse(schoolYearSupplier.get()).levelInstant());
  }

  public String qrCodeUrlOf(String publicId) {
    return qrCodeBaseUrl + "/" + publicId;
  }

  private Optional<String> loadPhoto(User student) {
    String key = student.getProfilePictureKey();
    if (key == null || key.isBlank()) {
      return Optional.empty();
    }
    File downloaded = null;
    try {
      downloaded = bucketComponent.download(key);
      BufferedImage image = ImageIO.read(downloaded);
      if (image == null) {
        log.warn("Unsupported profile picture format for student {}", student.getRef());
        return Optional.empty();
      }
      BufferedImage photo =
          roundCorners(
              cropAndScale(image, PHOTO_WIDTH_IN_PIXELS, PHOTO_HEIGHT_IN_PIXELS, TYPE_INT_ARGB));
      return Optional.of(toBase64(photo, "jpg"));
    } catch (Exception e) {
      log.warn("Cannot load profile picture of student {}: {}", student.getRef(), e.getMessage());
      return Optional.empty();
    } finally {
      if (downloaded != null && !downloaded.delete()) {
        downloaded.deleteOnExit();
      }
    }
  }

  private String loadLogo() {
    try (InputStream inputStream =
        classPathResourceResolver.apply("HEI_logo", ".png").getInputStream()) {
      BufferedImage logo = ImageIO.read(inputStream);
      BufferedImage mark =
          logo.getSubimage(
              logo.getWidth() * 15 / 100,
              logo.getHeight() * 29 / 100,
              logo.getWidth() * 70 / 100,
              logo.getHeight() * 42 / 100);
      int height = LOGO_WIDTH_IN_PIXELS * mark.getHeight() / mark.getWidth();
      return toBase64(cropAndScale(mark, LOGO_WIDTH_IN_PIXELS, height, TYPE_INT_ARGB), "png");
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  private static BufferedImage cropAndScale(
      BufferedImage source, int targetWidth, int targetHeight, int imageType) {
    double targetRatio = (double) targetWidth / targetHeight;
    int cropWidth = source.getWidth();
    int cropHeight = source.getHeight();
    if ((double) cropWidth / cropHeight > targetRatio) {
      cropWidth = (int) Math.round(cropHeight * targetRatio);
    } else {
      cropHeight = (int) Math.round(cropWidth / targetRatio);
    }
    int x = (source.getWidth() - cropWidth) / 2;
    int y = (source.getHeight() - cropHeight) / 2;

    BufferedImage target = new BufferedImage(targetWidth, targetHeight, imageType);
    Graphics2D graphics = target.createGraphics();
    try {
      if (imageType == TYPE_INT_RGB) {
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, targetWidth, targetHeight);
      }
      graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
      graphics.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
      graphics.drawImage(
          source, 0, 0, targetWidth, targetHeight, x, y, x + cropWidth, y + cropHeight, null);
    } finally {
      graphics.dispose();
    }
    return target;
  }

  private static BufferedImage roundCorners(BufferedImage photo) {
    int width = photo.getWidth();
    int height = photo.getHeight();
    int arc = PHOTO_CORNER_RADIUS_IN_PIXELS * 2;

    BufferedImage rounded = new BufferedImage(width, height, TYPE_INT_ARGB);
    Graphics2D graphics = rounded.createGraphics();
    try {
      graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
      graphics.setColor(Color.WHITE);
      graphics.fill(new RoundRectangle2D.Float(0, 0, width, height, arc, arc));
      graphics.setComposite(AlphaComposite.SrcIn);
      graphics.drawImage(photo, 0, 0, null);
    } finally {
      graphics.dispose();
    }

    BufferedImage onWhite = new BufferedImage(width, height, TYPE_INT_RGB);
    graphics = onWhite.createGraphics();
    try {
      graphics.setColor(Color.WHITE);
      graphics.fillRect(0, 0, width, height);
      graphics.drawImage(rounded, 0, 0, null);
    } finally {
      graphics.dispose();
    }
    return onWhite;
  }

  private static String toBase64(BufferedImage image, String format) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, format, outputStream);
    return Base64.getEncoder().encodeToString(outputStream.toByteArray());
  }

  static String lastNameFontSize(String lastName) {
    int longestWord =
        Arrays.stream(lastName.split("[\\s-]+")).mapToInt(String::length).max().orElse(0);
    if (longestWord <= 14) {
      return "10pt";
    }
    if (longestWord <= 16) {
      return "8.5pt";
    }
    if (longestWord <= 18) {
      return "8pt";
    }
    if (longestWord <= 20) {
      return "7.5pt";
    }
    if (longestWord <= 22) {
      return "7pt";
    }
    return "6pt";
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
