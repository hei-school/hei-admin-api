package school.hei.haapi;

import io.sentry.Sentry;
import io.sentry.Sentry.OptionsConfiguration;
import io.sentry.SentryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentryConf {

  private final String sentryDsn;
  private final String env;

  private static final double TRACES_SAMPLE_RATE = 1.0;

  public SentryConf(@Value("${sentry.dsn}") String sentryDsn, @Value("${sentry.environment}") String env) {
    this.sentryDsn = sentryDsn;
    this.env = env;
  }

  @Bean
  public OptionsConfiguration<SentryOptions> getSentryOptionsConf() {
    OptionsConfiguration<SentryOptions> optionsConf = options -> {
      options.setDsn(sentryDsn);
      options.setEnvironment(env);
      options.setTracesSampleRate(TRACES_SAMPLE_RATE);
    };
    Sentry.init(optionsConf);
    return optionsConf;
  }
}
