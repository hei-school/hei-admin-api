package school.hei.haapi.endpoint.rest.controller;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.service.AwsService;

import java.util.Map;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*" , maxAge = 3600)
@AllArgsConstructor
public class FileController {

  private static final String MESSAGE_1 = "Uploaded the file successfully";
  private static final String FILE_NAME = "fileName";
  private final AwsService awsService;

  @GetMapping
  public ResponseEntity<Object> findByName(@RequestBody(required = false) Map<String ,String> params){
    return ResponseEntity
            .ok()
            .cacheControl(CacheControl.noCache())
            .header("Content-type" , "application/octet-stream")
            .header("Content-disposition" , "attachment; filename=\"" + params.get(FILE_NAME) + "\"")
            .body(new InputStreamResource(awsService.findByName(params.get(FILE_NAME))));
  }

  @PostMapping
  public ResponseEntity<Object> save(@RequestParam("file") MultipartFile multipartFile){
    awsService.save(multipartFile);
    return new ResponseEntity<>(MESSAGE_1 ,HttpStatus.OK);
  }
}