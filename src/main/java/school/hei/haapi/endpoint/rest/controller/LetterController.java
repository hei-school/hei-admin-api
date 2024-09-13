package school.hei.haapi.endpoint.rest.controller;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.LetterMapper;
import school.hei.haapi.endpoint.rest.model.Letter;
import school.hei.haapi.endpoint.rest.model.LetterStatus;
import school.hei.haapi.endpoint.rest.model.PagedLettersResponse;
import school.hei.haapi.endpoint.rest.model.UpdateLettersStatus;
import school.hei.haapi.model.BoundedPageSize;
import school.hei.haapi.model.PageFromOne;
import school.hei.haapi.service.LetterService;

@RestController
@AllArgsConstructor
public class LetterController {

  private final LetterService letterService;
  private final LetterMapper letterMapper;

  @GetMapping(value = "/letters")
  public PagedLettersResponse getLetters(
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize,
      @RequestParam(name = "ref", required = false) String ref,
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "status", required = false) LetterStatus status,
      @RequestParam(name = "student_ref", required = false) String studentRef) {
    return letterService.getLetters(ref, studentRef, status, name, page, pageSize);
  }

  @PutMapping(value = "/letters")
  public List<Letter> updateLetter(@RequestBody List<UpdateLettersStatus> letters) {
    return letterService.updateLetter(letters).stream().map(letterMapper::toRest).toList();
  }

  @GetMapping(value = "/letters/{id}")
  public Letter getLetter(@PathVariable String id) {
    return letterMapper.toRest(letterService.getLetterById(id));
  }

  @GetMapping(value = "/students/{student_id}/letters")
  public List<Letter> getStudentLetters(
      @PathVariable(name = "student_id") String studentId,
      @RequestParam(name = "status", required = false) LetterStatus status,
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return letterService.getLettersByStudentId(studentId, status, page, pageSize).stream()
        .map(letterMapper::toRest)
        .toList();
  }

  @PostMapping(value = "/students/{student_id}/letters", consumes = MULTIPART_FORM_DATA_VALUE)
  public Letter createLetter(
      @PathVariable String student_id,
      @RequestPart(name = "description") String description,
      @RequestPart(name = "filename") String filename,
      @RequestPart(name = "file_to_upload") MultipartFile file) {
    return letterMapper.toRest(letterService.createLetter(student_id, description, filename, file));
  }
}
