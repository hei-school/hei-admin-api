package school.hei.haapi.endpoint.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static school.hei.haapi.integration.testData.StudentTestData.axel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import school.hei.haapi.endpoint.event.model.SendLetterEmail;
import school.hei.haapi.endpoint.event.model.UpdateLetterEmail;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.event.SendLetterEmailService;
import school.hei.haapi.service.event.UpdateLetterEmailService;
import school.hei.haapi.service.utils.Base64Converter;
import school.hei.haapi.service.utils.ClassPathResourceResolver;

public class LetterEventIT extends FacadeITMockedThirdParties {

  @Autowired UpdateLetterEmailService updateLetterEmailService;
  @Autowired SendLetterEmailService sendLetterEmailService;
  @Autowired UserRepository userRepository;
  @MockBean Mailer mailerMock;
  @MockBean Base64Converter base64Converter;
  @MockBean ClassPathResourceResolver classPathResourceResolver;

  private User student;

  @BeforeEach
  void setUp() {
    student = userRepository.save(axel());
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteById(student.getId());
  }

  static SendLetterEmail send() {
    return SendLetterEmail.builder()
        .id("test_id")
        .studentRef("ref")
        .description("description")
        .studentEmail("email")
        .receiver("contact@mail.hei.school")
        .build();
  }

  /** The service resolves the student by email, so it has to be one that actually exists. */
  private UpdateLetterEmail letterFor(User student) {
    return UpdateLetterEmail.builder()
        .id("letter1_id")
        .email(student.getEmail())
        .description("Certificat de residence")
        .ref("letter1_ref")
        .build();
  }

  @Test
  void should_invoke_event_producer_when_pinging_manager() {
    sendLetterEmailService.accept(send());

    verify(mailerMock, times(1)).accept(any());
  }

  @Test
  void should_invoke_event_producer_when_pinging_student() {
    updateLetterEmailService.accept(letterFor(student));

    verify(mailerMock, times(1)).accept(any());
  }
}
