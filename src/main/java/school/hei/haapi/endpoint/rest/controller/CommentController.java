package school.hei.haapi.endpoint.rest.controller;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CommentMapper;
import school.hei.haapi.endpoint.rest.model.Comment;
import school.hei.haapi.endpoint.rest.model.CreateComment;
import school.hei.haapi.endpoint.rest.model.OrderDirection;
import school.hei.haapi.endpoint.rest.validator.CommentValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CommentService;

@RestController
@AllArgsConstructor
public class CommentController {

  private final CommentService commentService;
  private final CommentMapper commentMapper;
  private final CommentValidator commentValidator;

  @GetMapping("/comments")
  public List<Comment> getComments(
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize,
      @RequestParam(name = "student_ref", required = false) String studentRef,
      @RequestParam(name = "timestamp_direction", required = false, defaultValue = "DESC")
          OrderDirection timestampDirection) {
    return commentService
        .getComments(
            page, pageSize, Sort.Direction.fromString(timestampDirection.toString()), studentRef)
        .stream()
        .map(commentMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @GetMapping("/students/{student_id}/comments")
  public List<Comment> getStudentComments(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(value = "observer_id", required = false) String observerId,
      @RequestParam PageFromOne page,
      @RequestParam(value = "page_size") BoundedPageSize pageSize) {
    return commentService.getStudentComments(studentId, observerId, page, pageSize).stream()
        .map(commentMapper::toRest)
        .collect(toUnmodifiableList());
  }

  @PostMapping("/students/{student_id}/comments")
  public Comment postComments(
      @PathVariable(name = "student_id") String studentId,
      @RequestBody CreateComment createComment) {
    commentValidator.accept(createComment);
    return commentMapper.toRest(commentService.postComment(commentMapper.toDomain(createComment)));
  }
}
