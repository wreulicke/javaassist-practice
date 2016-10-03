package wreulicke.test.application;

import org.glassfish.grizzly.http.server.HttpServer;

import com.google.inject.Provider;

public class HttpServerProvider implements Provider<HttpServer>{
  @Override
  public HttpServer get() {
    return new HttpServer();
  }
  
}
