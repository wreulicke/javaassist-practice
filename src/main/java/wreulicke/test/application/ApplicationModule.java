package wreulicke.test.application;

import java.util.concurrent.ExecutorService;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import wreulicke.test.service.Service;
import wreulicke.test.service.ServiceContainer;
import wreulicke.test.service.ServiceContainerProvider;

public class ApplicationModule extends AbstractModule{

  @Override
  protected void configure() {
    bind(ExecutorService.class).toProvider(ExecutorServiceProvider.class);
    Multibinder<Service> containers=Multibinder.newSetBinder(binder(), Service.class);
    bind(ServiceContainer.class).toProvider(ServiceContainerProvider.class);
  }

}
