package school.hei.haapi.endpoint.rest.mapper;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.RetakeExamSession;
import school.hei.haapi.model.RetakeExam;
import school.hei.haapi.repository.RetakeExamRepository;

@Component
@AllArgsConstructor
public class RetakeExamSessionMapper {

  @Autowired RetakeExamRepository retakeExamRepository;

  public RetakeExamSession toRest(school.hei.haapi.model.RetakeExamSession retakeExamSession) {
    return new RetakeExamSession()
        .id(retakeExamSession.getId())
        .dateFrom(retakeExamSession.getDateFrom())
        .dateTo(retakeExamSession.getDateTo());
  }

  public school.hei.haapi.model.RetakeExamSession toDomain(RetakeExamSession retakeExamSession) {
    List<RetakeExam> retakeExams =
        retakeExamRepository.findByRetakeExamSessionId(retakeExamSession.getId());
    return school.hei.haapi.model.RetakeExamSession.builder()
        .id(retakeExamSession.getId())
        .dateFrom(retakeExamSession.getDateFrom())
        .dateTo(retakeExamSession.getDateTo())
        .retakeExams(retakeExams)
        .build();
  }

  public List<RetakeExamSession> toRestList(
      List<school.hei.haapi.model.RetakeExamSession> retakeExamSessions) {
    return retakeExamSessions.stream().map(this::toRest).toList();
  }
}
