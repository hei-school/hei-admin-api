package school.hei.haapi.service;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.StudentLevel;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.RetakeExamSession;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.exception.NotFoundException;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.repository.RetakeExamSessionRepository;
import school.hei.haapi.repository.dao.RetakeExamSessionDao;

@Component
@AllArgsConstructor
public class RetakeExamSessionService {
  private final RetakeExamSessionRepository retakeExamSessionRepository;
  private final RetakeExamSessionDao retakeExamSessionDao;

  public RetakeExamSession getById(String id) {
    return retakeExamSessionRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Retake exam session not found"));
  }

  public List<RetakeExamSession> getRetakeExamSessions(
      String title,
      List<StudentLevel> studentLevels,
      PageFromOne page,
      BoundedPageSize pageSize,
      Instant from,
      Instant to) {
    PaginationFromPageAndPageSize pagination = new PaginationFromPageAndPageSize();
    Pageable pageable = pagination.apply(page, pageSize);
    return retakeExamSessionDao.filterByCriteria(title, studentLevels, pageable, from, to);
  }

  public RetakeExamSession save(RetakeExamSession retakeExamSession) {
    if (retakeExamSession.getDateFrom().isAfter(retakeExamSession.getDateTo()))
      throw new BadRequestException("Session start date must be before end date");
    return retakeExamSessionRepository.save(retakeExamSession);
  }
}
