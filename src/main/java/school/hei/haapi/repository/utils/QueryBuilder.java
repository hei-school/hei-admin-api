package school.hei.haapi.repository.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class QueryBuilder {
    public String getCourseAndFilterQuery(
            String code,
            String name,
            Integer credits,
            String firstName,
            String lastName,
            String codeOrder,
            String creditOrder
            ){
        String query = "select c from course c join \"user\" u on u.id = c.user.id where ";
        int acc=0;
        if(code !=null){
            query=query.concat("lower(c.code) like lower(concat('%','"+code+"','%')) ");
            acc+=1;
        }
        if(name!=null){
            if(acc!=0){
                query=query.concat("and lower(c.name) like lower(concat('%','"+name+"','%')) ");
            }else {
                query=query.concat("lower(c.name) like lower(concat('%','"+name+"','%')) ");
                acc+=1;
            }
        }
        if(credits!=null){
            if(acc!=0){
                query=query.concat("and c.credits = "+credits+" ");
            }else {
                query=query.concat("c.credits = "+credits+" ");
                acc+=1;
            }
        }
        if(firstName!=null){
            if(acc!=0){
                query=query.concat("and lower(u.first_name) like lower(concat('%','"+firstName+"','%')) ");
            }else {
                query=query.concat("lower(u.first_name) like lower(concat('%','"+firstName+"','%')) ");
                acc+=1;
            }
        }
        if(lastName!=null){
            if(acc!=0){
                query=query.concat("and lower(u.last_name) like lower(concat('%','"+lastName+"','%')) ");
            }else {
                query=query.concat("lower(u.last_name) like lower(concat('%','"+lastName+"','%')) ");
            }
        }
        if(creditOrder!=null && codeOrder!=null){
            query = query.concat("order by c.credits "+creditOrder+",c.code "+codeOrder);
        }
        if(codeOrder!=null && creditOrder==null){
            query = query.concat("order by c.code "+codeOrder);
        }
        return query;
    }
}
