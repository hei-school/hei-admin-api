package school.hei.haapi.service.utils;

import static java.util.function.Function.identity;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CollectionUtils {
  public <T> Collection<T> findCommonElement(Collection<T> collection1, Collection<T> collection2) {
    Collection<T> common = new HashSet<>(collection1);
    common.retainAll(collection2);
    return common;
  }

  public <T, K> Collection<T> filterDistinctByField(
      Collection<T> collection, Function<T, K> keyExtractor) {
    return collection.stream()
        .collect(Collectors.toMap(keyExtractor, identity(), (old, actual) -> actual))
        .values()
        .stream()
        .toList();
  }
}
