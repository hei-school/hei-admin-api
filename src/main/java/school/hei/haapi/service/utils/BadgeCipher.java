package school.hei.haapi.service.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import school.hei.haapi.endpoint.rest.model.EncryptedBadge;
import school.hei.haapi.model.exception.ApiException;

/**
 * Encrypts what a scanned badge shows, so that it is not readable as is in the network tools of a
 * browser. The key comes from the public id: whoever has the link can still read it, the page does.
 */
@Component
@AllArgsConstructor
public class BadgeCipher {
  private static final String TRANSFORMATION = "AES/GCM/NoPadding";
  private static final String KEY_PREFIX = "hei-badge:";
  private static final int IV_BYTES = 12;
  private static final int TAG_BITS = 128;
  private static final SecureRandom RANDOM = new SecureRandom();

  private final ObjectMapper objectMapper;

  public EncryptedBadge encrypt(Object payload, String publicId) {
    try {
      var iv = new byte[IV_BYTES];
      RANDOM.nextBytes(iv);
      var cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, keyOf(publicId), new GCMParameterSpec(TAG_BITS, iv));
      var ciphertext = cipher.doFinal(objectMapper.writeValueAsBytes(payload));
      var ivAndCiphertext = ByteBuffer.allocate(iv.length + ciphertext.length);
      ivAndCiphertext.put(iv).put(ciphertext);
      return new EncryptedBadge()
          .payload(Base64.getEncoder().encodeToString(ivAndCiphertext.array()));
    } catch (GeneralSecurityException | IOException e) {
      throw new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION, e);
    }
  }

  public <T> T decrypt(EncryptedBadge encrypted, String publicId, Class<T> payloadClass) {
    try {
      var ivAndCiphertext = Base64.getDecoder().decode(encrypted.getPayload());
      var cipher = Cipher.getInstance(TRANSFORMATION);
      cipher.init(
          Cipher.DECRYPT_MODE,
          keyOf(publicId),
          new GCMParameterSpec(TAG_BITS, ivAndCiphertext, 0, IV_BYTES));
      var json = cipher.doFinal(ivAndCiphertext, IV_BYTES, ivAndCiphertext.length - IV_BYTES);
      return objectMapper.readValue(json, payloadClass);
    } catch (GeneralSecurityException | IOException e) {
      throw new ApiException(ApiException.ExceptionType.SERVER_EXCEPTION, e);
    }
  }

  private static SecretKeySpec keyOf(String publicId) throws GeneralSecurityException {
    var seed = (KEY_PREFIX + publicId.toLowerCase(Locale.ROOT)).getBytes(UTF_8);
    return new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(seed), "AES");
  }
}
