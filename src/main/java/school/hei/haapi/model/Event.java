package school.hei.haapi.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"event\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event implements Serializable {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private String id;

  @Column(name = "event_name",nullable = false)
  @NotBlank
  private String eventName;

  @Column(name = "event_type",nullable = false)
  @NotBlank
  private String eventType;

  @Column(name = "start_date",nullable = false)
  private Instant startDate;

  @Column(name = "end_date", nullable = false)
  private Instant endDate;

  @ManyToOne
  @JoinColumn(name = "responsible")
  private User responsible;

  @ManyToOne
  @JoinColumn(name = "place")
  private Place place;
}
