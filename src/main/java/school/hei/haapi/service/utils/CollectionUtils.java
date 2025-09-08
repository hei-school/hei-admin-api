package school.hei.haapi.service.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CollectionUtils {
  public <T> Set<T> findCommonElement(
      Collection<T> excludeGroupIds, Collection<T> includeGroupIds) {
    var common = new HashSet<>(excludeGroupIds);
    common.retainAll(includeGroupIds);
    return common;
  }
}
