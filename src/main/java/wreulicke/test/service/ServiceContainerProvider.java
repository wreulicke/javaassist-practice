package wreulicke.test.service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.inject.Provider;

public class ServiceContainerProvider implements Provider<ServiceContainer> {

  @Override
  public ServiceContainer get() {
    return (uri) -> {
      System.out.println(uri);
      Service<?> xService=(x) -> {
        System.out.println(x);
        return CompletableFuture.completedFuture("test");
      };
      return Optional.of(xService);
    };
  }

}
