package school.hei.haapi.model.dto;

import school.hei.haapi.endpoint.rest.model.Statistics;

public record StatisticsDto(
    StatisticsDetailsDto women, StatisticsDetailsDto men, long totalGroups, long totalStudents) {
  public Statistics toRestStatistics(StatisticsStudentAlternatingDto alternatingStatistics) {
    return new Statistics()
        .men(men.toRestStatisticsDetails())
        .women(women.toRestStatisticsDetails())
        .totalGroups(totalGroups)
        .totalStudents(totalStudents)
        .studentsAlternating(alternatingStatistics.toRestStatisticsStudentsAlternating());
  }
}
