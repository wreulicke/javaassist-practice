package wreulicke.test.application;

import java.io.IOException;
import java.io.Writer;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;

import com.google.inject.Guice;
import com.google.inject.Injector;

import wreulicke.test.Task;
import wreulicke.test.service.Service;
import wreulicke.test.service.ServiceContainer;

public class Application {

  public static void main(String[] args) {
    String context = "/test";
    int port = 8080;
    Injector injector = Guice.createInjector(new ApplicationModule());
    injector.getInstance(Application.class)
      .start(context, port);
  }

  @Inject
  private HttpServer server;

  @Inject
  private ExecutorService service;

  // @Inject
  // private UrlResolver resolver;

  @Inject
  private ServiceContainer container;

  public void start(String context) {
    Handler handler = (request, response) -> {
      // FIXME String uri=resolver.resolve(request.getRequestURI());
      String uri = request.getRequestURI()
        .substring(context.length());
      response.suspend();
      Optional<Service<?>> xxx = container.find(uri);
      xxx.ifPresent(s -> {
        s.apply("test content")
          .whenCompleteAsync((a, b) -> {
            try {
              if (b != null) {
                throw b;
              }
              else {
                try (Writer writer = response.getWriter()) {
                  response.setStatus(200);
                  writer.write(a.toString());
                }
              }
            } catch (Throwable e) {
              response.setStatus(400);
              try (Writer writer = response.getWriter()) {
                writer.write("internal server error");
              } catch (IOException e1) {
              }
            } finally {
              response.resume();
            }
          }, service);
      });
    };
    Handler handler2 = (request, response) -> {
      response.suspend();
      Task task = () -> {
        response.setContentType("text/plain");
        try (Writer writer = response.getWriter()) {
          writer.write("no content");
          response.setStatus(400);
        }
      };
      CompletableFuture.runAsync(task.get(), service)
        .thenRun(() -> response.resume());
    };
    server.getServerConfiguration()
      .addHttpHandler(handler.get(), context);
    server.getServerConfiguration()
      .addHttpHandler(handler2.get());
  }

  public void start(String context, int port) {
    server.addListener(new NetworkListener("grizzly", "0.0.0.0", port));
    start(context);
    try {
      server.start();
      System.in.read();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      server.shutdown();
    }

  }
}
