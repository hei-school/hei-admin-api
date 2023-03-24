package school.hei.haapi.model;

import lombok.*;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "\"course\"")
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;
    private String name;
    private String code ;
    private Integer credits;
    private Integer totalHours;

    @ManyToOne
    @JoinColumn(name="teacher_id")
    private User mainTeacher;


}
