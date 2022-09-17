package school.hei.haapi.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.model.Present;
import school.hei.haapi.service.RekognitionAppFacialService;

import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
public class AppFacialController {
    public RekognitionAppFacialService rekognitionAppFacialService;

    @PostMapping("/events/{id_event}/event_participants/presence")
    public List<Present> presenceVerification(
            @PathVariable String id_event,
            @RequestParam("image") MultipartFile multipartFile,
            @RequestParam Float similarity) throws IOException {
        return rekognitionAppFacialService.facialPresence(id_event,multipartFile,similarity);
    }


/*
    @GetMapping("/app-facial/object-list")
    public List<S3ObjectSummary> getAll() {
        return s3AppFacialService.getAll();
    }
    */

/*
    @GetMapping("/app-facial/object-as-byte")
    public byte[] getFileByNameAsByte(@RequestParam String name) throws IOException {
        return s3AppFacialService.getFileByNameAsByte(name);
    }
    @GetMapping("/app-facial/object")
    public S3ObjectInputStream getFileByName(@RequestParam String name) {
        return s3AppFacialService.getFileByName(name);
    }
*/
}
