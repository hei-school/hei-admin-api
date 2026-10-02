package school.hei.haapi.service.utils;

import static com.google.zxing.BarcodeFormat.QR_CODE;
import static com.google.zxing.EncodeHintType.CHARACTER_SET;
import static com.google.zxing.EncodeHintType.ERROR_CORRECTION;
import static com.google.zxing.EncodeHintType.MARGIN;
import static com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import school.hei.haapi.model.exception.ApiException;

@Component
public class QrCodeGenerator implements Function<String, String> {
  private static final int SIZE_IN_PIXELS = 300;

  @Override
  public String apply(String content) {
    try {
      BitMatrix matrix =
          new QRCodeWriter()
              .encode(
                  content,
                  QR_CODE,
                  SIZE_IN_PIXELS,
                  SIZE_IN_PIXELS,
                  Map.of(ERROR_CORRECTION, M, MARGIN, 0, CHARACTER_SET, "UTF-8"));
      var outputStream = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);
      return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    } catch (WriterException | IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }
}
