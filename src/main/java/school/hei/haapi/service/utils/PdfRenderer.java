package school.hei.haapi.service.utils;

import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import com.lowagie.text.DocumentException;
import java.io.ByteArrayOutputStream;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;
import school.hei.haapi.model.exception.ApiException;

@Component
public class PdfRenderer implements Function<String, byte[]> {
  @Override
  public byte[] apply(String html) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ITextRenderer renderer = new ITextRenderer();
    renderer.setDocumentFromString(html);

    renderer.layout();
    try {
      renderer.createPDF(outputStream);
    } catch (DocumentException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
    return outputStream.toByteArray();
  }
}
