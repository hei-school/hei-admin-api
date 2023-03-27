package school.hei.haapi.endpoint.rest.converter;

import org.springframework.core.convert.converter.Converter;
import school.hei.haapi.model.PageFromOne;

public class PageConverter implements Converter<String, PageFromOne> {

  @Override
  public PageFromOne convert(String source) {
    return new PageFromOne(Integer.parseInt(source));
  }
}
