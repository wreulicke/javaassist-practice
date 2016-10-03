package wreulicke.test.application;

import java.util.concurrent.ExecutorService;

import org.glassfish.grizzly.threadpool.GrizzlyExecutorService;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

import com.google.inject.Provider;

public class ExecutorServiceProvider implements Provider<ExecutorService> {
  ExecutorService service;

  public ExecutorServiceProvider() {
    service = GrizzlyExecutorService.createInstance(ThreadPoolConfig.defaultConfig()
      .copy()
      .setCorePoolSize(5)
      .setMaxPoolSize(10));
  }

  @Override
  public ExecutorService get() {
    return service;
  }


}
