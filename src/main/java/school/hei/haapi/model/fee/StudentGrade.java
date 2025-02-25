package school.hei.haapi.model.fee;

import lombok.Getter;

@Getter
public enum StudentGrade {
  L1("l1"),
  L2("l2"),
  L3("l3");

  private final String name;

  StudentGrade(String name) {
    this.name = name;
  }
}
