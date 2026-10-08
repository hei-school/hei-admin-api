package school.hei.haapi.service;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static org.springframework.data.domain.Pageable.unpaged;
import static school.hei.haapi.model.User.Role.STUDENT;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.Badge;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.service.utils.AcademicYear;
import school.hei.haapi.service.utils.BadgeImageProcessor;
import school.hei.haapi.service.utils.BadgeLayout;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PdfRenderer;
import school.hei.haapi.service.utils.QrCodeGenerator;
import school.hei.haapi.service.utils.SchoolYearSupplier;

@Service
public class StudentBadgeService {
  public static final int MAX_BADGES_PER_REQUEST = 200;
  private static final String TEMPLATE = "studentBadges";
  private static final Comparator<String> NULLS_LAST = nullsLast(String.CASE_INSENSITIVE_ORDER);
  private static final Comparator<User> BY_NAME =
      comparing(User::getLastName, NULLS_LAST)
          .thenComparing(User::getFirstName, NULLS_LAST)
          .thenComparing(User::getRef, NULLS_LAST);

  private final UserService userService;
  private final StudentBadgeCodeService studentBadgeCodeService;
  private final StudentLevelResolver studentLevelResolver;
  private final BadgeImageProcessor badgeImageProcessor;
  private final QrCodeGenerator qrCodeGenerator;
  private final HtmlParser htmlParser;
  private final PdfRenderer pdfRenderer;
  private final SchoolYearSupplier schoolYearSupplier;
  private final String qrCodeBaseUrl;

  public StudentBadgeService(
      UserService userService,
      StudentBadgeCodeService studentBadgeCodeService,
      StudentLevelResolver studentLevelResolver,
      BadgeImageProcessor badgeImageProcessor,
      QrCodeGenerator qrCodeGenerator,
      HtmlParser htmlParser,
      PdfRenderer pdfRenderer,
      SchoolYearSupplier schoolYearSupplier,
      @Value("${badge.qr.base-url}") String qrCodeBaseUrl) {
    this.userService = userService;
    this.studentBadgeCodeService = studentBadgeCodeService;
    this.studentLevelResolver = studentLevelResolver;
    this.badgeImageProcessor = badgeImageProcessor;
    this.qrCodeGenerator = qrCodeGenerator;
    this.htmlParser = htmlParser;
    this.pdfRenderer = pdfRenderer;
    this.schoolYearSupplier = schoolYearSupplier;
    this.qrCodeBaseUrl = withoutTrailingSlash(qrCodeBaseUrl);
  }

  public byte[] generateBadges(String groupId, List<String> studentIds, String academicYear) {
    var students = findStudents(groupId, studentIds);
    if (students.isEmpty()) {
      throw new BadRequestException("No student found for the given group_id or student_ids");
    }
    if (students.size() > MAX_BADGES_PER_REQUEST) {
      throw new BadRequestException(
          "Cannot generate more than " + MAX_BADGES_PER_REQUEST + " badges at once");
    }
    var printedAcademicYear = academicYearOrCurrent(academicYear);
    var badges =
        withoutActiveBadge(students, printedAcademicYear).stream()
            .map(student -> toBadge(student, printedAcademicYear))
            .toList();
    return render(badges, printedAcademicYear);
  }

  public String qrCodeUrlOf(String publicId) {
    return qrCodeBaseUrl + "/" + publicId;
  }

  private List<User> findStudents(String groupId, List<String> studentIds) {
    var hasGroup = groupId != null && !groupId.isBlank();
    var hasIds = studentIds != null && !studentIds.isEmpty();
    if (!hasGroup && !hasIds) {
      throw new BadRequestException("Either group_id or student_ids must be provided");
    }

    var students = new ArrayList<User>();
    if (hasGroup) {
      students.addAll(userService.getByGroupId(groupId, unpaged()));
    }
    if (hasIds) {
      students.addAll(userService.getByRoleAndIds(List.of(STUDENT), studentIds));
    }
    var seenIds = new HashSet<String>();
    return students.stream()
        .filter(student -> STUDENT.equals(student.getRole()))
        .filter(student -> seenIds.add(student.getId()))
        .sorted(BY_NAME)
        .toList();
  }

  private AcademicYear academicYearOrCurrent(String academicYear) {
    return AcademicYear.parse(
        academicYear == null || academicYear.isBlank() ? schoolYearSupplier.get() : academicYear);
  }

  private List<User> withoutActiveBadge(List<User> students, AcademicYear academicYear) {
    var studentsWithoutBadge =
        students.stream()
            .filter(student -> !studentBadgeCodeService.hasActiveBadge(student, academicYear))
            .toList();
    if (studentsWithoutBadge.isEmpty()) {
      var who =
          students.size() == 1
              ? "The student already has"
              : "All the " + students.size() + " students already have";
      throw new BadRequestException(
          who
              + " an active badge for "
              + academicYear.label()
              + ": remove a badge before printing a new one");
    }
    return studentsWithoutBadge;
  }

  private Badge toBadge(User student, AcademicYear academicYear) {
    var lastName = nullToEmpty(student.getLastName()).toUpperCase(Locale.FRENCH);
    var level = studentLevelResolver.findLevelOf(student, academicYear);
    var withoutExpiration = studentLevelResolver.keepsBadgeAfter(student, academicYear);
    var publicId =
        studentBadgeCodeService.createBadge(student, academicYear, withoutExpiration).getPublicId();
    return new Badge(
        lastName,
        nullToEmpty(student.getFirstName()),
        nullToEmpty(student.getRef()),
        level.map(StudentLevel::name).orElse(null),
        badgeImageProcessor.profilePictureOf(student).orElse(null),
        qrCodeGenerator.apply(qrCodeUrlOf(publicId)),
        BadgeLayout.lastNameFontSize(lastName));
  }

  private byte[] render(List<Badge> badges, AcademicYear academicYear) {
    var context = new Context();
    context.setVariable("pages", BadgeLayout.pagesOf(badges));
    context.setVariable("academic_year", academicYear.label());
    context.setVariable("logo", badgeImageProcessor.logo());
    return pdfRenderer.apply(htmlParser.apply(TEMPLATE, context));
  }

  private static String withoutTrailingSlash(String url) {
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
