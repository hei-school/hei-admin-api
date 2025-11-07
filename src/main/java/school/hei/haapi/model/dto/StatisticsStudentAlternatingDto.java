package school.hei.haapi.model.dto;

import school.hei.haapi.endpoint.rest.model.StatisticsStudentsAlternating;

public interface StatisticsStudentAlternatingDto {
  default StatisticsStudentsAlternating toRestStatisticsStudentsAlternating() {
    return new StatisticsStudentsAlternating()
        .total(getTotal())
        .haveBeenWorking(getHaveBeenWorking())
        .willWork(getWillWork())
        .notWorking(getNotWorking())
        .working(getWorking());
  }

  long getTotal();

  long getWillWork();

  long getHaveBeenWorking();

  long getNotWorking();

  long getWorking();
}
