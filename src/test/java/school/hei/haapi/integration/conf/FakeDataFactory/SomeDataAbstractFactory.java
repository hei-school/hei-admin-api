package school.hei.haapi.integration.conf.FakeDataFactory;

import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class SomeDataAbstractFactory<T, B> {
  protected static final Faker faker = new Faker();
  protected final B builder;

  public abstract T build();
}
