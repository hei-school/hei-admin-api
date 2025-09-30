package school.hei.haapi.endpoint.rest.controller;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CorCommentMapper;
import school.hei.haapi.endpoint.rest.mapper.CorMapper;
import school.hei.haapi.endpoint.rest.model.Cor;
import school.hei.haapi.endpoint.rest.model.CorCommentInfo;
import school.hei.haapi.endpoint.rest.model.CorStatus;
import school.hei.haapi.endpoint.rest.model.CrupdateCor;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.model.pagination.PaginationFromPageAndPageSize;
import school.hei.haapi.service.CorCommentService;
import school.hei.haapi.service.CorService;

@RestController
@RequiredArgsConstructor
public class CorController {
  private final CorService corService;
  private final CorMapper corMapper;
  private final CorCommentMapper corCommentMapper;
  private final PaginationFromPageAndPageSize paginationFromPageAndPageSize;
  private final CorCommentService corCommentService;

  @GetMapping("/cors")
  public List<Cor> getCors(
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(name = "student_ref", required = false) String studentRef,
      @RequestParam(name = "group_ref", required = false) String groupRef,
      @RequestParam(name = "cor_status", required = false) List<CorStatus> statuses,
      @RequestParam(name = "page", required = false) PageFromOne page,
      @RequestParam(name = "page_size", required = false) BoundedPageSize pageSize) {
    return corMapper.toRest(
        corService.getCors(
            from,
            to,
            studentRef,
            groupRef,
            corMapper.toDomain(statuses),
            paginationFromPageAndPageSize.apply(page, pageSize)));
  }

  @GetMapping("/cors/{cor_id}")
  public Cor getCorById(@PathVariable(name = "cor_id") String corId) {
    return corMapper.toRest(corService.getById(corId));
  }

  @PostMapping("/cors/{cor_id}/comment")
  public Cor commentCorById(
      @PathVariable("cor_id") String corId, @RequestBody CorCommentInfo corCommentInfo) {
    return corMapper.toRest(
        corCommentService
            .addCommentByCorId(corId, corCommentMapper.toDomain(corCommentInfo))
            .getCor());
  }

  @GetMapping("/students/{student_id}/cors")
  public List<Cor> getStudentCors(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(name = "page", required = false, defaultValue = "1") PageFromOne page,
      @RequestParam(name = "page_size", required = false, defaultValue = "15")
          BoundedPageSize pageSize) {
    return corMapper.toRest(corService.findAllByStudentId(studentId, page, pageSize));
  }

  @PutMapping("/students/{student_id}/cors")
  public Cor crupdateStudentCors(
      @PathVariable(name = "student_id") String studentId, @RequestBody CrupdateCor cors) {
    return corMapper.toRest(corService.save(corMapper.toDomain(cors, studentId)));
  }
}
