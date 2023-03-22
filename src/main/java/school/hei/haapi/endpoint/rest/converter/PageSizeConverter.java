package school.hei.haapi.endpoint.rest.converter;

import org.springframework.core.convert.converter.Converter;
import school.hei.haapi.model.BoundedPageSize;

public class PageSizeConverter implements Converter<String, BoundedPageSize> {

  @Override
  public BoundedPageSize convert(String source) {
    return new BoundedPageSize(Integer.parseInt(source));
  }
}
