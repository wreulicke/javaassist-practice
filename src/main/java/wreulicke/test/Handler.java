package wreulicke.test;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

public interface Handler {
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