package school.hei.haapi.endpoint.rest.mapper;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class FileMapper {
    public static ResponseEntity<ByteArrayResource> customFileResponse(byte[] file, String filename, String contentType) {
        ByteArrayResource resource = new ByteArrayResource(file);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
