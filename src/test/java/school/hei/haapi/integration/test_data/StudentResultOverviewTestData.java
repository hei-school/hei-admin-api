package school.hei.haapi.integration.test_data;

import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.ResultOverviewStatus;
import school.hei.haapi.model.StudentResultOverview;
import school.hei.haapi.model.User;

import java.math.BigDecimal;
import java.time.Instant;

import static school.hei.haapi.model.CycleLevel.BACHELOR;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;
import static school.hei.haapi.model.User.Role.STUDENT;
import static school.hei.haapi.model.User.Status.ENABLED;

public class StudentResultOverviewTestData {
  public static Group groupAB() {
    return Group.builder()
        .name("group AB")
        .ref("group AB")
        .creationDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .build();
  }

  public static Group groupCD() {
    return Group.builder()
        .name("group CD")
        .ref("group CD")
        .creationDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  public static Promotion promotionAB() {
    return Promotion.builder()
        .name("Promotion 2020-2021")
        .ref("Alumni 2020 2021")
        .creationDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .cycleLevel(BACHELOR)
        .build();
  }

  public static Promotion promotionCD() {
    return Promotion.builder()
        .name("Promotion 2021-2022")
        .ref("Alumni 2021 2022")
        .creationDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .cycleLevel(BACHELOR)
        .build();
  }

  public static User studentA() {
    return User.builder()
        .firstName("student A firstname")
        .lastName("student A lastname")
        .email("studentA@gmail.com")
        .ref("STD2002")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .role(STUDENT)
        .build();
  }

  public static User studentB() {
    return User.builder()
        .firstName("student B firstname")
        .lastName("student B lastname")
        .email("studentB@gmail.com")
        .ref("STD2001")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .role(STUDENT)
        .build();
  }

  public static User studentC() {
    return User.builder()
        .firstName("student C firstname")
        .lastName("student C lastname")
        .email("studentC@gmail.com")
        .ref("STD2101")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .role(STUDENT)
        .build();
  }

  public static User studentD() {
    return User.builder()
        .firstName("student D firstname")
        .lastName("student D lastname")
        .email("studentD@gmail.com")
        .ref("STD2102")
        .status(ENABLED)
        .entranceDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .role(STUDENT)
        .build();
  }

  public static GroupFlow groupFlowA() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .build();
  }

  public static GroupFlow groupFlowB() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .build();
  }

  public static GroupFlow groupFlowC() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  public static GroupFlow groupFlowD() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  public static StudentResultOverview studentResultOverviewA() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.VALIDATED)
        .weightedAverage(BigDecimal.valueOf(17.45))
        .obtainedCredits(BigDecimal.valueOf(180.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }

  public static StudentResultOverview studentResultOverviewB() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.INVALIDATED)
        .weightedAverage(BigDecimal.valueOf(09.45))
        .obtainedCredits(BigDecimal.valueOf(172.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }

  public static StudentResultOverview studentResultOverviewC() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.VALIDATED)
        .weightedAverage(BigDecimal.valueOf(10.45))
        .obtainedCredits(BigDecimal.valueOf(180.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }

  public static StudentResultOverview studentResultOverviewD() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.VALIDATED)
        .weightedAverage(BigDecimal.valueOf(14.15))
        .obtainedCredits(BigDecimal.valueOf(180.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }
}
