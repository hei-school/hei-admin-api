package school.hei.haapi.service;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Cor;
import school.hei.haapi.model.CorComment;
import school.hei.haapi.model.CorComment.CorStatus;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.repository.CorRepository;
import school.hei.haapi.repository.dao.CorDao;

@Service
@AllArgsConstructor
public class CorService {
  private final CorRepository corRepository;
  private final PaginationFromPageAndPageSize paginationFromPageAndPageSize;
  private final CorDao corDao;

  public List<Cor> getCors(
      Instant from,
      Instant to,
      String studentRef,
      String groupRef,
      List<CorStatus> statuses,
      Pageable pageable) {
    return corDao.findByCriteria(from, to, studentRef, groupRef, statuses, pageable);
  }

  public List<Cor> getByStudentId(String studentId, PageFromOne page, BoundedPageSize size) {
    return corRepository.findAllByConcernedStudentId(
        studentId, paginationFromPageAndPageSize.apply(page, size));
  }

  /** Cor status cannot be manually changed by this function, must add comment to change it */
  public Cor save(Cor cor) {
    var id = cor.getId();

    if (id != null) corRepository.findById(id).ifPresent(value -> cor.setStatus(value.getStatus()));

    return corRepository.save(cor);
  }

  public Cor addComment(String corId, CorComment comment) {
    return addComment(getById(corId), comment);
  }

  public Cor addComment(Cor cor, CorComment comment) {
    var savedCor = getById(cor.getId());
    savedCor.addComment(comment);
    return corRepository.save(savedCor);
  }

  public Cor getById(String id) {
    return corRepository.findById(id).orElseThrow(() -> new BadRequestException("Cor not found"));
  }
}
