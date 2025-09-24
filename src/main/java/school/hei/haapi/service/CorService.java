package school.hei.haapi.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.Cor;
import school.hei.haapi.model.CorComment;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.exception.BadRequestException;
import school.hei.haapi.repository.CorRepository;

@Service
@RequiredArgsConstructor
public class CorService {
  private final CorRepository corRepository;

  public List<Cor> getByStudentId(String studentId, PageFromOne page, BoundedPageSize size) {
    var pageable =
        PageRequest.of(
            page.getValue() - 1, size.getValue(), Sort.by("creationDatetime").descending());
    return corRepository.findAllByConcernedStudentId(studentId, pageable);
  }

  public Cor save(Cor cor) {
    var savedCor = corRepository.findById(cor.getId());

    if (savedCor.isPresent() && !cor.getStatus().equals(savedCor.get().getStatus()))
      throw new IllegalArgumentException(
          "Cor status cannot be manually changed, must add comment to change it");

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
