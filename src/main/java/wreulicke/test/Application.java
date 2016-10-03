package wreulicke.test;

import java.util.concurrent.ExecutorService;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.threadpool.GrizzlyExecutorService;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

public class Application {
  public static void main(String[] args) {
    ExecutorService service = GrizzlyExecutorService
        .createInstance(ThreadPoolConfig.defaultConfig().copy().setCorePoolSize(5).setMaxPoolSize(10));
    HttpServer server = new HttpServer();
    Handler handler = (request, response) -> {
      response.setContentType("text/plain");
      response.getWriter().write("xxx:"+request.getRequestURI());
    };
    Handler handler2 = (request, response) -> {
      response.suspend();
      Task task = () -> {
        try {
          response.setContentType("text/plain");
          response.getWriter().write("no content");
          response.setStatus(400);
        } finally {
          response.resume();
        }
      };
      service.submit(task.get());
    };
    server.addListener(new NetworkListener("grizzly", "0.0.0.0", 8080));
    server.getServerConfiguration().addHttpHandler(handler.get(), "/test");
    server.getServerConfiguration().addHttpHandler(handler2.get());
    try {
      server.start();
      System.in.read();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }

  public static interface Task {
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

  public static interface Handler {
    void handle(Request request, Response response) throws Exception;

    default HttpHandler get() {
      return new HttpHandler() {
        @Override
        public void service(Request request, Response response) throws Exception {
          handle(request, response);
        }
      };
    }
  }
}