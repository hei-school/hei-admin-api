package school.hei.haapi.service.utils;

import java.util.Collection;
import java.util.HashSet;
import org.springframework.stereotype.Component;

@Component
public class CollectionUtils {
  public <T> Collection<T> findCommonElement(Collection<T> collection1, Collection<T> collection2) {
    Collection<T> common = new HashSet<>(collection1);
    common.retainAll(collection2);
    return common;
  }
}
