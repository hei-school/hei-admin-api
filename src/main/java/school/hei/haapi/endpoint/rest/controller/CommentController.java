package school.hei.haapi.endpoint.rest.controller;


import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.hei.haapi.endpoint.rest.mapper.CommentMapper;
import school.hei.haapi.endpoint.rest.model.Comment;
import school.hei.haapi.endpoint.rest.model.CreateComment;
import school.hei.haapi.endpoint.rest.validator.CommentValidator;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.CommentService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;
    private final CommentValidator commentValidator;

    @GetMapping("/students/{studentId}/comments")
    public List<Comment> getComments(@PathVariable String studentId, @RequestParam(value = "observer_id", required = false) String observerId, @RequestParam PageFromOne page,
                                     @RequestParam(value = "page_size") BoundedPageSize pageSize){
        return commentService.getComments(studentId, observerId, page, pageSize).stream()
                .map(commentMapper::toRest)
                .collect(Collectors.toUnmodifiableList());
    }

    @PostMapping("/students/{studentId}/comments")
    public Comment postComments(@PathVariable String studentId, @RequestBody CreateComment createComment){
        commentValidator.accept(createComment);
        return commentMapper.toRest(commentService.postComment(commentMapper.toDomain(createComment)));
    }
}
