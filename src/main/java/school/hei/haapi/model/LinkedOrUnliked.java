package school.hei.haapi.model;

@Entity
@Table(name = "\"linked_or_unliked\"")
@TypeDef(name = "pgsql_enum", typeClass = PostgresEnumType.class)
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkedOrUnliked {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private String id;
    @OneToMany( mappedBy = "courseLinked")
    private String User_id;
    @OneToMany( mappedBy = "userStatus")
    private String course_id;
    private Status status;
    public enum Status{
        LINKED,UNLIKED
    }
}
