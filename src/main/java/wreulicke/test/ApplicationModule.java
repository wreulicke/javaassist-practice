package wreulicke.test;

import java.util.concurrent.ExecutorService;

import com.google.inject.AbstractModule;

public class ApplicationModule extends AbstractModule{

  @Override
  protected void configure() {
    bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class);
  }

}
