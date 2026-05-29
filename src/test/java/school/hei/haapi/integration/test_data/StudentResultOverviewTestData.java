package school.hei.haapi.integration.test_data;

import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.INVALIDATED;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.IN_PROGRESS;
import static school.hei.haapi.endpoint.rest.model.ResultOverviewStatus.VALIDATED;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L1;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L2;
import static school.hei.haapi.endpoint.rest.model.StudentLevel.L3;
import static school.hei.haapi.model.CycleLevel.BACHELOR;
import static school.hei.haapi.model.GroupFlow.GroupFlowType.JOIN;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import school.hei.haapi.endpoint.rest.model.YearlyResult;
import school.hei.haapi.model.Group;
import school.hei.haapi.model.GroupFlow;
import school.hei.haapi.model.Promotion;
import school.hei.haapi.model.ResultOverviewStatus;
import school.hei.haapi.model.StudentResultOverview;

public class StudentResultOverviewTestData {
  public static Group groupJ() {
    return Group.builder()
        .name("group J")
        .ref("group J")
        .creationDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .build();
  }

  public static Group groupH() {
    return Group.builder()
        .name("group H")
        .ref("group H")
        .creationDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  public static Promotion promotionH() {
    return Promotion.builder()
        .name("Promotion 2020-2021")
        .ref("Alumni 2020 2021")
        .creationDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .cycleLevel(BACHELOR)
        .build();
  }

  public static Promotion promotionJ() {
    return Promotion.builder()
        .name("Promotion 2021-2022")
        .ref("Promo 2021 2022")
        .creationDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .cycleLevel(BACHELOR)
        .build();
  }

  public static GroupFlow groupFlowAxel() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .build();
  }

  public static GroupFlow groupFlowTolojanahary() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2020-01-01T00:00:00Z"))
        .build();
  }

  public static GroupFlow groupFlowManitra() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  public static GroupFlow groupFlowFreddy() {
    return GroupFlow.builder()
        .groupFlowType(JOIN)
        .flowDatetime(Instant.parse("2021-01-01T00:00:00Z"))
        .build();
  }

  public static StudentResultOverview tolojanaharyResultOverview() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.VALIDATED)
        .weightedAverage(BigDecimal.valueOf(17.45))
        .obtainedCredits(BigDecimal.valueOf(180.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }

  public static StudentResultOverview axelResultOverview() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.INVALIDATED)
        .weightedAverage(BigDecimal.valueOf(09.45))
        .obtainedCredits(BigDecimal.valueOf(172.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }

  public static StudentResultOverview manitraResultOverview() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.VALIDATED)
        .weightedAverage(BigDecimal.valueOf(10.45))
        .obtainedCredits(BigDecimal.valueOf(180.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }

  public static StudentResultOverview freddyResultOverview() {
    return StudentResultOverview.builder()
        .status(ResultOverviewStatus.VALIDATED)
        .weightedAverage(BigDecimal.valueOf(14.15))
        .obtainedCredits(BigDecimal.valueOf(180.0))
        .totalCredits(BigDecimal.valueOf(180.0))
        .build();
  }

  public static YearlyResult tolojanaharyYearlyResultL1() {
    return new YearlyResult()
        .level(L1)
        .weightedAverage(BigDecimal.valueOf(17.45))
        .obtainedCredits(BigDecimal.valueOf(60.0))
        .courseResults(List.of())
        .status(VALIDATED)
        .totalCredits(BigDecimal.valueOf(60.0));
  }

  public static YearlyResult tolojanaharyYearlyResultL2() {
    return new YearlyResult()
        .level(L2)
        .weightedAverage(BigDecimal.valueOf(17.45))
        .obtainedCredits(BigDecimal.valueOf(60.0))
        .courseResults(List.of())
        .status(VALIDATED)
        .totalCredits(BigDecimal.valueOf(60.0));
  }

  public static YearlyResult tolojanaharyYearlyResultL3() {
    return new YearlyResult()
        .level(L3)
        .weightedAverage(BigDecimal.valueOf(17.45))
        .obtainedCredits(BigDecimal.valueOf(60.0))
        .courseResults(List.of())
        .status(VALIDATED)
        .totalCredits(BigDecimal.valueOf(60.0));
  }

  public static YearlyResult axelYearlyResultL1() {
    return new YearlyResult()
        .level(L1)
        .weightedAverage(BigDecimal.valueOf(12.45))
        .obtainedCredits(BigDecimal.valueOf(60.0))
        .courseResults(List.of())
        .status(VALIDATED)
        .totalCredits(BigDecimal.valueOf(60.0));
  }

  public static YearlyResult axelYearlyResultL2() {
    return new YearlyResult()
        .level(L2)
        .weightedAverage(BigDecimal.valueOf(17.45))
        .obtainedCredits(BigDecimal.valueOf(60.0))
        .courseResults(List.of())
        .status(VALIDATED)
        .totalCredits(BigDecimal.valueOf(60.0));
  }

  public static YearlyResult axelYearlyResultL3() {
    return new YearlyResult()
        .level(L3)
        .weightedAverage(BigDecimal.valueOf(08.45))
        .obtainedCredits(BigDecimal.valueOf(29.0))
        .courseResults(List.of())
        .status(INVALIDATED)
        .totalCredits(BigDecimal.valueOf(60.0));
  }

  public static YearlyResult manitraYearlyResultL1() {
    return new YearlyResult()
        .level(L1)
        .weightedAverage(BigDecimal.valueOf(14.45))
        .obtainedCredits(BigDecimal.valueOf(60.0))
        .courseResults(List.of())
        .status(VALIDATED)
        .totalCredits(BigDecimal.valueOf(60.0));
  }

  public static YearlyResult manitraYearlyResultL2() {
    return new YearlyResult()
        .level(L2)
        .weightedAverage(BigDecimal.valueOf(14.45))
        .obtainedCredits(BigDecimal.valueOf(60.0))
        .courseResults(List.of())
        .status(VALIDATED)
        .totalCredits(BigDecimal.valueOf(60.0));
  }

  public static YearlyResult manitraYearlyResultL3() {
    return new YearlyResult()
        .level(L3)
        .weightedAverage(BigDecimal.valueOf(12.45))
        .obtainedCredits(BigDecimal.valueOf(30.0))
        .courseResults(List.of())
        .status(IN_PROGRESS)
        .totalCredits(BigDecimal.valueOf(60.0));
  }

  public static YearlyResult freddyYearlyResultL1() {
    return new YearlyResult()
        .level(L1)
        .weightedAverage(BigDecimal.valueOf(16.45))
        .obtainedCredits(BigDecimal.valueOf(60.0))
        .courseResults(List.of())
        .status(VALIDATED)
        .totalCredits(BigDecimal.valueOf(600.0));
  }

  public static YearlyResult freddyYearlyResultL2() {
    return new YearlyResult()
        .level(L2)
        .weightedAverage(BigDecimal.valueOf(12.45))
        .obtainedCredits(BigDecimal.valueOf(52.0))
        .courseResults(List.of())
        .status(VALIDATED)
        .totalCredits(BigDecimal.valueOf(60.0));
  }

  public static YearlyResult freddyYearlyResultL3() {
    return new YearlyResult()
        .level(L3)
        .weightedAverage(BigDecimal.valueOf(12.45))
        .obtainedCredits(BigDecimal.valueOf(30.0))
        .courseResults(List.of())
        .status(IN_PROGRESS)
        .totalCredits(BigDecimal.valueOf(60.0));
  }
}
