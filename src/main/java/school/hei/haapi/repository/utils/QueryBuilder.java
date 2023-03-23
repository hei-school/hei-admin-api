package school.hei.haapi.repository.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class QueryBuilder {
    public String getCourseAndFilterQuery(String code,String name,Integer credits,String firstName,String lastName){
        String query = "select c from course c where ";
        if(code != null){
            query = query.concat("c.code = "+code+" ");
        }
        if(name != null){
            query.concat("c.name = "+name+" ");
        }
        if(credits != null){
            query.concat("c.credits = "+credits+" ");
        }
        return query;
    }
}
