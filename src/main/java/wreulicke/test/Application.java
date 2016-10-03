package wreulicke.test;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Application {
  
  public static void main(String[] args) {
    String context="/test"; 
    int port=8080;
    Injector injector=Guice.createInjector(new ApplicationModule());
    injector.getInstance(Application.class).start(context, port);
  }
  
  @Inject
  private HttpServer server;
  
  @Inject
  private ExecutorService service;
  
  public void start(String context){
    Handler handler = (request, response) -> {
      String uri=request.getRequestURI().substring(context.length());
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
    server.getServerConfiguration().addHttpHandler(handler.get(), context);
    server.getServerConfiguration().addHttpHandler(handler2.get());
  }
  
  public void start(String context, int port){
    server.addListener(new NetworkListener("grizzly", "0.0.0.0", port));
    start(context);
    try {
      server.start();
      System.in.read();
    } catch (Exception e) {
      e.printStackTrace();
    }finally {
      server.shutdown();
    }
    
  }
}