package school.hei.haapi.repository.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class QueryBuilder {
    public String getCourseAndFilterQuery(String code,String name,Integer credits,String firstName,String lastName){
        String query = "select c from course c join \"user\" u on u.id = c.user.id where ";
        if(code !=null){
            query=query.concat("c.code = "+code+" ");
        }
        if(name!=null){
            query=query.concat("c.name = "+name+" ");
        }
        if(credits!=null){
            query=query.concat("c.credits = "+credits+" ");
        }
        if(firstName!=null){
            query=query.concat("u.first_name = "+firstName+" ");
        }
        if(lastName!=null){
            query=query.concat("u.last_name = "+lastName);
        }

        return query;
    }
}
