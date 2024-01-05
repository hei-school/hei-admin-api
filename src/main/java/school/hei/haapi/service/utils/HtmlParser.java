package school.hei.haapi.service.utils;

import java.util.function.BiFunction;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Component
public class HtmlParser implements BiFunction<String, Context, String> {
  @Override
  public String apply(String html, Context context) {
    TemplateEngine templateEngine = configureTemplate();
    return templateEngine.process(html, context);
  }

  private TemplateEngine configureTemplate() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("/templates/");
    resolver.setSuffix(".html");
    resolver.setCharacterEncoding("UTF-8");
    resolver.setTemplateMode(TemplateMode.HTML);

    TemplateEngine templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(resolver);
    return templateEngine;
  }
}
