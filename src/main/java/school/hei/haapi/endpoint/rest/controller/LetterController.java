package school.hei.haapi.endpoint.rest.controller;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.endpoint.rest.mapper.LetterMapper;
import school.hei.haapi.endpoint.rest.model.Letter;
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
  public List<Letter> getLetters(
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize,
      @RequestParam(name = "ref", required = false) String ref,
      @RequestParam(name = "student_ref", required = false) String studentRef) {
    return letterService.getLetters(ref, studentRef, page, pageSize).stream()
        .map(letterMapper::toRest)
        .toList();
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
      @RequestParam(name = "page") PageFromOne page,
      @RequestParam(name = "page_size") BoundedPageSize pageSize) {
    return letterService.getLettersByStudentId(studentId, page, pageSize).stream()
        .map(letterMapper::toRest)
        .toList();
  }

  @PostMapping(value = "/students/{student_id}/letters")
  public Letter createLetter(
      @PathVariable String student_id,
      @RequestParam String description,
      @RequestParam String filename,
      @RequestPart(name = "file_to_upload") MultipartFile file) {
    return letterMapper.toRest(letterService.createLetter(student_id, description, filename, file));
  }
}
