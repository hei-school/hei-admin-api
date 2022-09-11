package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
@Entity
public class EventParticipant implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private String idEventParticipant;

  @Enumerated(EnumType.STRING)
  @NotBlank
  @NotEmpty
  @Builder.Default
  private Status status = Status.EXCEPTED;

  @OneToOne
  @JoinColumn(name = "id_participant")
  private User participant;

  @ManyToOne
  @JoinColumn(name = "id_event")
  @Cascade(CascadeType.REMOVE)
  private Event event;
  public enum Status {
    EXCEPTED,
    MISSING,
    HERE
  }

}
