package school.hei.haapi.service.utils;

@Builder
@Getter
@Setter
public class UpdateStudentscourse{
    private String courseId;
    private statusType status;
    public enum statusType{
        LINKED,UNLIKED
    }
}