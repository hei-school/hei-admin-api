package school.hei.haapi.model.validator;

import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class FilenameValidator implements Consumer<String> {

  @Override
  public void accept(String filename) {
    String[] parsedFilename = filename.split("\\.");
    if (parsedFilename.length > 1) {
      throw new BadRequestException("File name must not contain an extension");
    }
  }
}
