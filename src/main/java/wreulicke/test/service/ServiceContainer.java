package wreulicke.test.service;

import java.util.Optional;

public interface ServiceContainer<T> {
  Optional<Service<T>> find(String uri);
}
