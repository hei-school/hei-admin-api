package school.hei.haapi.service;

import static java.time.Instant.now;
import static school.hei.haapi.service.utils.DataFormatterUtils.instantToCommonDate;
import static school.hei.haapi.service.utils.FileUtils.createFileFromBytes;

import java.io.File;
import java.math.MathContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.User;
import school.hei.haapi.service.utils.*;

@Service
@AllArgsConstructor
public class YearlyResultGenerationService {
  private final HtmlParser htmlParser;
  private final PdfRenderer pdfRenderer;
  private final Base64Converter base64Converter;
  private final ClassPathResourceResolver classPathResourceResolver;
  private static final String YEARLY_RESULT_FILENAME_PREFIX = "Bulletin-";

  public File generateYealyResultFile(User student, YearlyResult yearlyResult) {
    Context context = loadContext(student, yearlyResult);
    String html = htmlParser.apply("yearlyResult", context);
    String filename = YEARLY_RESULT_FILENAME_PREFIX + context.getVariable("promotion");
    return createFileFromBytes(pdfRenderer.apply(html), filename, ".pdf");
  }

  private Context loadContext(User student, YearlyResult yearlyResult) {
    Context context = new Context();
    var logoResource = classPathResourceResolver.apply("HEI_logo", ".png");
    context.setVariable("logo", base64Converter.apply(logoResource));
    context.setVariable("student_level", yearlyResult.getLevel());
    context.setVariable(
        "student_level_full_letters", Promotion.getLevelString(yearlyResult.getLevel()));
    context.setVariable("last_name", student.getLastName());
    context.setVariable("first_name", student.getFirstName());
    context.setVariable("ref", student.getRef());
    context.setVariable("specialization", student.getSpecializationFieldString());
    var studentGroup = student.findCurrentGroup();
    var studentPromotionString =
        studentGroup
            .map(group -> group.getPromotion().getPromotionYearString(yearlyResult.getLevel()))
            .orElse("inconnu");
    context.setVariable("promotion", studentPromotionString);
    context.setVariable("course_results", yearlyResult.getCourseResults());
    context.setVariable("obtained_credits", yearlyResult.getObtainedCredits());
      context.setVariable("total_credits", yearlyResult.getTotalCredits());
    context.setVariable(
        "yearly_average", yearlyResult.getWeightedAverage().round(new MathContext(4)));
    context.setVariable("current_date", instantToCommonDate(now()));
    var signatureRessource = classPathResourceResolver.apply("signature", ".png");
    context.setVariable("email_signature", base64Converter.apply(signatureRessource));

    return context;
  }
}
