package school.hei.haapi.service.utils;

import java.util.List;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.exception.BadRequestException;

@Component
public class FileValidator implements Consumer<String> {

  private List<String> allowedFileType() {
    return List.of(
        // documents allowed
        "pdf",
        "doc",
        "docx",
        "xls",
        "xlsx",
        "gsheet",
        "gdoc",
        // pictures allowed
        "png",
        "jpg",
        "jpeg",
        "svg",
        "MOV");
  }

  @Override
  public void accept(String fileType) {
    if (!allowedFileType().contains(fileType)) {
      throw new BadRequestException("File not supported");
    }
  }
}
