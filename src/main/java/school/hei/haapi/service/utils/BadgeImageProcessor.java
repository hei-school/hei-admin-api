package school.hei.haapi.service.utils;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_INTERPOLATION;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static school.hei.haapi.model.exception.ApiException.ExceptionType.SERVER_EXCEPTION;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.User;
import school.hei.haapi.model.exception.ApiException;

@Slf4j
@Component
public class BadgeImageProcessor {
  private static final int PHOTO_WIDTH_IN_PIXELS = 300;
  private static final int PHOTO_HEIGHT_IN_PIXELS = 400;
  private static final int PHOTO_CORNER_RADIUS_IN_PIXELS = 36;
  private static final int LOGO_WIDTH_IN_PIXELS = 360;
  private static final int PARALLEL_DOWNLOADS = 6;

  private final BucketComponent bucketComponent;
  private final ClassPathResourceResolver classPathResourceResolver;
  private String logo;

  public BadgeImageProcessor(
      BucketComponent bucketComponent, ClassPathResourceResolver classPathResourceResolver) {
    this.bucketComponent = bucketComponent;
    this.classPathResourceResolver = classPathResourceResolver;
    ImageIO.scanForPlugins();
  }

  public Map<String, String> profilePicturesOf(List<User> students) {
    try (var executor = Executors.newFixedThreadPool(PARALLEL_DOWNLOADS)) {
      var photos = new LinkedHashMap<String, Future<Optional<String>>>();
      students.forEach(
          student -> photos.put(student.getId(), executor.submit(() -> profilePictureOf(student))));
      var result = new HashMap<String, String>();
      for (var photo : photos.entrySet()) {
        result.put(photo.getKey(), photo.getValue().get().orElse(null));
      }
      return result;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(SERVER_EXCEPTION, e);
    } catch (ExecutionException e) {
      throw new ApiException(SERVER_EXCEPTION, e);
    }
  }

  public Optional<String> profilePictureOf(User student) {
    var key = student.getProfilePictureKey();
    if (key == null || key.isBlank()) {
      return Optional.empty();
    }
    File downloaded = null;
    try {
      downloaded = bucketComponent.download(key);
      var image = ImageIO.read(downloaded);
      if (image == null) {
        log.warn("Unsupported profile picture format for student {}", student.getRef());
        return Optional.empty();
      }
      var photo =
          roundCorners(
              cropAndScale(image, PHOTO_WIDTH_IN_PIXELS, PHOTO_HEIGHT_IN_PIXELS, TYPE_INT_ARGB));
      return Optional.of(toBase64(photo, "jpg"));
    } catch (Exception e) {
      log.warn("Cannot load profile picture of student {}: {}", student.getRef(), e.getMessage());
      return Optional.empty();
    } finally {
      delete(downloaded);
    }
  }

  public synchronized String logo() {
    if (logo == null) {
      logo = loadLogo();
    }
    return logo;
  }

  private String loadLogo() {
    try (var inputStream = classPathResourceResolver.apply("HEI_logo", ".png").getInputStream()) {
      var squareLogo = ImageIO.read(inputStream);
      var mark =
          squareLogo.getSubimage(
              squareLogo.getWidth() * 15 / 100,
              squareLogo.getHeight() * 29 / 100,
              squareLogo.getWidth() * 70 / 100,
              squareLogo.getHeight() * 42 / 100);
      var height = LOGO_WIDTH_IN_PIXELS * mark.getHeight() / mark.getWidth();
      return toBase64(cropAndScale(mark, LOGO_WIDTH_IN_PIXELS, height, TYPE_INT_ARGB), "png");
    } catch (IOException e) {
      throw new ApiException(SERVER_EXCEPTION, e.getMessage());
    }
  }

  static BufferedImage cropAndScale(
      BufferedImage source, int targetWidth, int targetHeight, int imageType) {
    var targetRatio = (double) targetWidth / targetHeight;
    var cropWidth = source.getWidth();
    var cropHeight = source.getHeight();
    if ((double) cropWidth / cropHeight > targetRatio) {
      cropWidth = (int) Math.round(cropHeight * targetRatio);
    } else {
      cropHeight = (int) Math.round(cropWidth / targetRatio);
    }
    var x = (source.getWidth() - cropWidth) / 2;
    var y = (source.getHeight() - cropHeight) / 2;
    var width = cropWidth;
    var height = cropHeight;

    var target = new BufferedImage(targetWidth, targetHeight, imageType);
    draw(
        target,
        graphics -> {
          if (imageType == TYPE_INT_RGB) {
            fillWhite(graphics, targetWidth, targetHeight);
          }
          graphics.setRenderingHint(KEY_INTERPOLATION, VALUE_INTERPOLATION_BICUBIC);
          graphics.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
          graphics.drawImage(
              source, 0, 0, targetWidth, targetHeight, x, y, x + width, y + height, null);
        });
    return target;
  }

  static BufferedImage roundCorners(BufferedImage photo) {
    var width = photo.getWidth();
    var height = photo.getHeight();
    var arc = PHOTO_CORNER_RADIUS_IN_PIXELS * 2;

    var rounded = new BufferedImage(width, height, TYPE_INT_ARGB);
    draw(
        rounded,
        graphics -> {
          graphics.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
          graphics.setColor(Color.WHITE);
          graphics.fill(new RoundRectangle2D.Float(0, 0, width, height, arc, arc));
          graphics.setComposite(AlphaComposite.SrcIn);
          graphics.drawImage(photo, 0, 0, null);
        });

    var onWhite = new BufferedImage(width, height, TYPE_INT_RGB);
    draw(
        onWhite,
        graphics -> {
          fillWhite(graphics, width, height);
          graphics.drawImage(rounded, 0, 0, null);
        });
    return onWhite;
  }

  private static void draw(BufferedImage image, Consumer<Graphics2D> drawing) {
    var graphics = image.createGraphics();
    try {
      drawing.accept(graphics);
    } finally {
      graphics.dispose();
    }
  }

  private static void fillWhite(Graphics2D graphics, int width, int height) {
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, width, height);
  }

  private static String toBase64(BufferedImage image, String format) throws IOException {
    var outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, format, outputStream);
    return Base64.getEncoder().encodeToString(outputStream.toByteArray());
  }

  private static void delete(File file) {
    if (file != null && !file.delete()) {
      file.deleteOnExit();
    }
  }
}
