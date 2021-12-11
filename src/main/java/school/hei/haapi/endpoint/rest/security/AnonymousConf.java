package school.hei.haapi.endpoint.rest.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Order(1)
public class AnonymousConf extends WebSecurityConfigurerAdapter {
  @Getter private String pingPath;

  public AnonymousConf(@Value("${school.hei.haapi.ping}") String pingPath) {
    this.pingPath = pingPath;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
    http
        // configuration only applies for following request(s)...
        .antMatcher(this.getPingPath())

        // ... which are secured as follows:
        // authentication
        .anonymous()
        // authorization
        .and()
        .authorizeRequests()
        .antMatchers(this.getPingPath())
        .permitAll()

        // disable superfluous protections
        .and()
        .csrf()
        .disable() // NOSONAR(csrf)
        .formLogin()
        .disable()
        .logout()
        .disable();
    // formatter:on
  }
}
