package school.hei.haapi.endpoint.rest.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileUploadValidator {
    //@Value("${spring.servlet.multipart.max-file-size}")
    public static String MAX_FILE_SIZE = "2MB";
}
