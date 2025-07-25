package school.hei.haapi.service;

import org.junit.jupiter.api.Test;
import school.hei.haapi.model.Exam;
import school.hei.haapi.model.Grade;
import school.hei.haapi.model.User;

class GradeResultServiceTest {
  @Test
  void correct_result() {
    var student1 = User.builder().ref("STD22075").build();
    var mgt1Grade =
        Grade.builder().score(17.75).exam(Exam.builder().coefficient(4).build()).build();
    var prog1Grade =
        Grade.builder().score(13.59).exam(Exam.builder().coefficient(3).build()).build();
    var donnees1Grade =
        Grade.builder().score(15.4375).exam(Exam.builder().coefficient(4).build()).build();
    var web1Grade =
        Grade.builder().score(18.75).exam(Exam.builder().coefficient(6).build()).build();
    var sys1Grade = Grade.builder().score(13.).exam(Exam.builder().coefficient(6).build()).build();
    var lv1Grade = Grade.builder().score(13.91).exam(Exam.builder().coefficient(4).build()).build();
  }
}
