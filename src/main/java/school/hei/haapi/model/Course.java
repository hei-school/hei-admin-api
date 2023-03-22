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
import javax.validation.constraints.NotBlank;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"course\"")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;

    @NotBlank(message = "Code is mandatory")
    @Column(nullable = false, unique = true)
    private String code;

    @NotBlank(message = "Name is mandatory")
    private String name;

    private int credits;

    @Column(name = "total_hours")
    private int totalHours;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User mainTeacher;
}
