package school.hei.haapi.service.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import school.hei.haapi.endpoint.rest.model.PublicStudent;
import school.hei.haapi.model.exception.ApiException;

class BadgeCipherTest {
  private static final String PUBLIC_ID = "7c1e4b9a-2f3d-4e8a-9b6c-1d2e3f4a5b6c";
  private final BadgeCipher subject = new BadgeCipher(new ObjectMapper().findAndRegisterModules());

  @Test
  void information_is_not_readable_as_is_but_the_page_can_decrypt_it() {
    var student = new PublicStudent().isValid(true).ref("STD26001").lastName("RAKOTO");

    var encrypted = subject.encrypt(student, PUBLIC_ID);

    assertFalse(encrypted.getPayload().contains("STD26001"));
    assertFalse(encrypted.getPayload().contains("RAKOTO"));
    assertEquals(student, subject.decrypt(encrypted, PUBLIC_ID.toUpperCase(), PublicStudent.class));
  }

  @Test
  void same_badge_is_encrypted_differently_each_time() {
    var student = new PublicStudent().ref("STD26001");

    assertNotEquals(
        subject.encrypt(student, PUBLIC_ID).getPayload(),
        subject.encrypt(student, PUBLIC_ID).getPayload());
  }

  @Test
  void another_public_id_cannot_decrypt() {
    var encrypted = subject.encrypt(new PublicStudent().ref("STD26001"), PUBLIC_ID);

    assertThrows(
        ApiException.class,
        () ->
            subject.decrypt(
                encrypted, "11111111-2222-4333-8444-555555555555", PublicStudent.class));
  }
}
