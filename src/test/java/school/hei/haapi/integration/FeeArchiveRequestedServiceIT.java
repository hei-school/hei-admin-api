package school.hei.haapi.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static school.hei.haapi.model.User.Role.ADMIN;
import static school.hei.haapi.model.User.Status.DISABLED;
import static school.hei.haapi.model.User.Status.ENABLED;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import school.hei.haapi.endpoint.event.model.FeeArchiveRequested;
import school.hei.haapi.integration.conf.FacadeITMockedThirdParties;
import school.hei.haapi.mail.Email;
import school.hei.haapi.mail.Mailer;
import school.hei.haapi.model.User;
import school.hei.haapi.repository.UserRepository;
import school.hei.haapi.service.event.FeeArchiveRequestedService;

@Testcontainers
@AutoConfigureMockMvc
class FeeArchiveRequestedServiceIT extends FacadeITMockedThirdParties {
  @Autowired private FeeArchiveRequestedService subject;
  @Autowired private UserRepository userRepository;
  @MockBean private Mailer mailer;
  private User enabledAdmin;
  private User disabledAdmin;

  private static User admin(User.Status status) {
    return User.builder()
        .ref("ADM" + UUID.randomUUID())
        .firstName("Admin")
        .lastName("HEI")
        .email(UUID.randomUUID() + "@hei.school")
        .status(status)
        .entranceDatetime(Instant.now())
        .role(ADMIN)
        .build();
  }

  private static FeeArchiveRequested archiveRequestedEvent() {
    return FeeArchiveRequested.builder()
        .feeId(UUID.randomUUID().toString())
        .studentRef("STD" + UUID.randomUUID())
        .studentFirstName("John")
        .studentLastName("Doe")
        .totalAmount(200_000)
        .dueDatetime(Instant.now())
        .comment("Tuition")
        .build();
  }

  @BeforeEach
  void setUp() {
    doNothing().when(mailer).accept(any(Email.class));
    enabledAdmin = userRepository.save(admin(ENABLED));
    disabledAdmin = userRepository.save(admin(DISABLED));
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteById(enabledAdmin.getId());
    userRepository.deleteById(disabledAdmin.getId());
  }

  @Test
  void notifies_enabled_admins_only() {
    subject.accept(archiveRequestedEvent());

    var emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer, times(1)).accept(emailCaptor.capture());
    var sentEmail = emailCaptor.getValue();
    var recipients = new java.util.ArrayList<String>();
    recipients.add(sentEmail.to().toString());
    sentEmail.cc().forEach(address -> recipients.add(address.toString()));

    assertTrue(recipients.contains(enabledAdmin.getEmail()));
    assertFalse(recipients.contains(disabledAdmin.getEmail()));
  }
}
