package school.hei.haapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;

import java.io.Serializable;
import java.time.Instant;

import static javax.persistence.GenerationType.*;


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
    @Column(name = "id", nullable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @ManyToOne
    @JoinColumn(name = "place_id")
    private Place place;
    @FutureOrPresent(message = "startingDateTime must be in future or present")
    private Instant startingDateTime;
    @FutureOrPresent(message = "endingDateTime must be in future or present")
    private Instant endingDateTime;
    @Column(nullable = false)
    private String eventType;
    private String title;
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

}