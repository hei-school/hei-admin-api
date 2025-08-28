package school.hei.haapi.model.pagination;

import java.util.function.BiFunction;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;

@Component
public class PaginationFromPageAndPageSize
    implements BiFunction<PageFromOne, BoundedPageSize, Pageable> {

  @Override
  public Pageable apply(PageFromOne page, BoundedPageSize pageSize) {
    return PageRequest.of(page.getValue() - 1, pageSize.getValue());
  }
}
