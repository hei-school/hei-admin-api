package school.hei.haapi.endpoint.rest.validator;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import school.hei.haapi.model.exception.BadRequestException;

import java.util.Objects;
import java.util.function.Consumer;

@Component
public class CreateTranscriptVersionValidator implements Consumer<MultipartFile> {
    @Override public void accept(MultipartFile multipartFile) {
        if (multipartFile.isEmpty()){
            throw new BadRequestException("pdf required");
        }
        if (!Objects.equals(multipartFile.getContentType(), "application/pdf")){
            throw new BadRequestException("only pdf file accepted");
        }
        if (multipartFile.getSize()>1048576) {
            throw new BadRequestException("pdf size must be less than 1MB");
        }
    }
}
