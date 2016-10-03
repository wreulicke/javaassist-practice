package wreulicke.test.service;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface Service<T> {
  // TODO content直す
  CompletionStage<T> apply(String content);
}
