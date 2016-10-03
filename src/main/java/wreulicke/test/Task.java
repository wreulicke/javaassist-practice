package wreulicke.test;

public interface Task {
  void run() throws Exception;

  default Runnable get() {
    return () -> {
      try {
        run();
      } catch (Exception e) {
        e.printStackTrace();
      }
    };
  }
}