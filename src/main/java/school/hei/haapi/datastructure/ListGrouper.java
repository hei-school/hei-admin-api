package school.hei.haapi.datastructure;

import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import org.springframework.stereotype.Component;
import school.hei.haapi.PojaGenerated;

@PojaGenerated
@Component
public class ListGrouper<T> implements BiFunction<List<T>, Integer, List<List<T>>> {
  @Override
  public List<List<T>> apply(List<T> list, Integer groupSize) {
    List<List<T>> groupedList = new ArrayList<>();
    int size = list.size();

    for (int i = 0; i < size; i += groupSize) {
      int end = min(size, i + groupSize);
      groupedList.add(new ArrayList<>(list.subList(i, end)));
    }

    return groupedList;
  }
}
