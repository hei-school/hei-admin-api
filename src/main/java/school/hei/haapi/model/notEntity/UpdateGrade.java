package school.hei.haapi.model.notEntity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;

@AllArgsConstructor
@Getter
public class UpdateGrade {
  Grade grade;
  User student;
  String comment;
  Exam exam;
}
