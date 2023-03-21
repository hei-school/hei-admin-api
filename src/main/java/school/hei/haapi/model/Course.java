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

import javax.persistence.*;


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

    @Column(unique=true)
    private String code;



    @OneToMany
    @JoinColumn(name = "student_id" , nullable = false)
    private Student student;


    private String name;

    private int credits;

    private int total_hours;

    @ManyToOne
    @JoinColumn(name = "main_teacher_id", nullable = false)
    private User main_teacher;


    public String getCourseName() {
        return null;
    }

    public Object getStatus() {
        return null;
    }

}
