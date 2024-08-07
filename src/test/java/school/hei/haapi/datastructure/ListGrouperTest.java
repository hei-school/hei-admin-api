package school.hei.haapi.datastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListGrouperTest {

  @Test
  void group() {
    List<Integer> numbers = new ArrayList<>();
    for (int i = 1; i <= 8; i++) {
      numbers.add(i);
    }

    ListGrouper<Integer> subject = new ListGrouper<>();
    var grouped = subject.apply(numbers, 3);

    assertEquals(3, grouped.size());
    assertEquals(List.of(1, 2, 3), grouped.get(0));
    assertEquals(List.of(4, 5, 6), grouped.get(1));
    assertEquals(List.of(7, 8), grouped.get(2));
  }
}
