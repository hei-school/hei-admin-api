package school.hei.haapi.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import school.hei.haapi.endpoint.rest.model.Student;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.User;
import school.hei.haapi.service.utils.HtmlParser;
import school.hei.haapi.service.utils.PdfRenderer;

import java.io.File;

@Service
@AllArgsConstructor
public class YearlyResultGenerationService {
  private final HtmlParser htmlParser;
  private final PdfRenderer pdfRenderer;

  public File generateYealyResultFile(User student, YearlyResult yearlyResult) {
    return new File(".");
  }

  private Context loadContext(User student, YearlyResult yearlyResult) {
    var studentGroup = student.findCurrentGroup();
    var studentPromotionString = studentGroup
        .map(group -> group.getPromotion().getName())
        .orElse("inconnu");
    Context context = new Context();
    context.setVariable("student_level", yearlyResult.getLevel());
    context.setVariable("student_level_full_letters", );
    context.setVariable("last_name", student.getLastName());
    context.setVariable("first_name", student.getFirstName());
    context.setVariable("ref", student.getRef());
    context.setVariable("promotion", studentPromotionString);
    context.setVariable("course_results", yearlyResult.getCourseResults());

  }
}
