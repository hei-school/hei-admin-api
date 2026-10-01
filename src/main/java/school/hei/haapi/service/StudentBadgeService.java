package school.hei.haapi.service;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.time.Month.NOVEMBER;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.Badge;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;
import school.hei.haapi.model.exception.BadRequestException;
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
  // ~2.7 mm on the 22.5 mm wide photo frame
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
      @Value("${badge.qr.base-url}") String qrCodeBaseUrl) {
    this.userService = userService;
    this.bucketComponent = bucketComponent;
    this.htmlParser = htmlParser;
    this.pdfRenderer = pdfRenderer;
    this.qrCodeGenerator = qrCodeGenerator;
    this.classPathResourceResolver = classPathResourceResolver;
    this.schoolYearSupplier = schoolYearSupplier;
    this.studentBadgeCodeService = studentBadgeCodeService;
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

    String printedAcademicYear =
        academicYear == null || academicYear.isBlank() ? schoolYearSupplier.get() : academicYear;
    Instant levelInstant = levelInstantOf(printedAcademicYear);
    List<Badge> badges = students.stream().map(student -> toBadge(student, levelInstant)).toList();
    Context context = new Context();
    context.setVariable("pages", toPages(badges));
    context.setVariable("academic_year", printedAcademicYear);
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

  private Badge toBadge(User student, Instant levelInstant) {
    String lastName = nullToEmpty(student.getLastName()).toUpperCase(Locale.FRENCH);
    return new Badge(
        lastName,
        nullToEmpty(student.getFirstName()),
        nullToEmpty(student.getRef()),
        findLevel(student, levelInstant).map(StudentLevel::name).orElse(null),
        loadPhoto(student).orElse(null),
        qrCodeGenerator.apply(
            qrCodeUrlOf(studentBadgeCodeService.getOrCreateActiveBadge(student).getPublicId())),
        lastNameFontSize(lastName));
  }

  /**
   * The level is computed for the printed academic year (e.g. "2026 - 2027" gives the level of
   * November 2026), so that badges printed before the start of the year show the new level.
   */
  static Instant levelInstantOf(String academicYear) {
    Matcher startYear = Pattern.compile("(\\d{4})").matcher(academicYear);
    if (!startYear.find()) {
      return Instant.now();
    }
    return LocalDate.of(Integer.parseInt(startYear.group(1)), NOVEMBER, 15)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant();
  }

  public static Optional<StudentLevel> findLevel(User student, Instant levelInstant) {
    try {
      return student
          .findCurrentGroup()
          .map(Group::getPromotion)
          .flatMap(promotion -> promotion.findLevelAt(levelInstant));
    } catch (RuntimeException e) {
      log.warn("Cannot compute level of student {}: {}", student.getRef(), e.getMessage());
      return Optional.empty();
    }
  }

  /** The QR code only holds the random public id, never the real student id. */
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

  /**
   * flying-saucer cannot clip an image with CSS border-radius, so the corners are rounded on the
   * image itself, over the white background of the badge (JPEG keeps the PDF light).
   */
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

  /**
   * Long malagasy last names must fit the ~35 mm wide column: names with several words wrap, so
   * only the longest word matters.
   */
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
