package school.hei.haapi.service.utils;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.haapi.file.bucket.BucketComponent;
import school.hei.haapi.model.User;

class BadgeImageProcessorTest {
  private BucketComponent bucketComponent;
  private BadgeImageProcessor subject;

  @BeforeEach
  void setUp() {
    bucketComponent = mock(BucketComponent.class);
    subject = new BadgeImageProcessor(bucketComponent, new ClassPathResourceResolver());
  }

  @Test
  void profile_picture_is_cropped_to_the_portrait_frame_with_rounded_corners() throws Exception {
    var landscape = new BufferedImage(800, 600, TYPE_INT_RGB);
    var graphics = landscape.createGraphics();
    graphics.setColor(Color.BLUE);
    graphics.fillRect(0, 0, 800, 600);
    graphics.dispose();
    var file = File.createTempFile("photo", ".png");
    ImageIO.write(landscape, "png", file);
    when(bucketComponent.download(anyString())).thenReturn(file);

    var photo = decode(subject.profilePictureOf(studentWithPicture()).orElseThrow());

    assertEquals(300, photo.getWidth());
    assertEquals(400, photo.getHeight());
    assertTrue(brightness(photo.getRGB(0, 0)) > 200);
    var center = new Color(photo.getRGB(150, 200));
    assertTrue(center.getBlue() > 200 && center.getRed() < 60);
    assertFalse(file.exists());
  }

  @Test
  void no_profile_picture_without_key_or_when_download_fails() {
    var withoutPicture = new User();
    assertTrue(subject.profilePictureOf(withoutPicture).isEmpty());
    verify(bucketComponent, never()).download(anyString());

    when(bucketComponent.download(anyString())).thenThrow(new RuntimeException("NoSuchKey"));
    assertTrue(subject.profilePictureOf(studentWithPicture()).isEmpty());
  }

  @Test
  void logo_is_computed_once() throws Exception {
    var logo = subject.logo();

    assertSame(logo, subject.logo());
    var image = decode(logo);
    assertEquals(360, image.getWidth());
    assertTrue(image.getColorModel().hasAlpha());
  }

  private static User studentWithPicture() {
    var student = new User();
    student.setRef("STD26001");
    student.setProfilePictureKey("STUDENT/STD26001/PROFILE_PICTURE_STD26001.png");
    return student;
  }

  private static int brightness(int rgb) {
    var color = new Color(rgb);
    return (color.getRed() + color.getGreen() + color.getBlue()) / 3;
  }

  private static BufferedImage decode(String base64) throws Exception {
    return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));
  }
}
