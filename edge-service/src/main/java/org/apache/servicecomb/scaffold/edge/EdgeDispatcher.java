package org.apache.servicecomb.scaffold.edge;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.Map;

import org.apache.servicecomb.edge.core.AbstractEdgeDispatcher;
import org.apache.servicecomb.edge.core.CompatiblePathVersionMapper;
import org.apache.servicecomb.edge.core.EdgeInvocation;
import org.apache.servicecomb.serviceregistry.definition.DefinitionConst;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;

import com.netflix.config.DynamicPropertyFactory;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;

/**
 * This is a default edge dispatcher match two style path url :
 * 1. /serviceName/version/operation
 * like /user-service/v1/login?name=xxx&password=xxx
 * 2. /serviceName/operation
 * like /user-service/login?name=xxx&password=xxx
 * this style will use latest version of active instance
 */
public class EdgeDispatcher extends AbstractEdgeDispatcher {
  private final CompatiblePathVersionMapper versionMapper = new CompatiblePathVersionMapper();

  public int getOrder() {
    return 10000;
  }

  public void init(Router router) {
    String regex = "/([^\\\\/]+)/([^\\\\/]+)/(.*)";
    router.routeWithRegex(regex).handler(CookieHandler.create());
    router.routeWithRegex(regex).handler(createBodyHandler());
    router.routeWithRegex(regex).failureHandler(this::onFailure).handler(this::onRequest);

    regex = "/([^\\\\/]+)/(.*)";
    router.routeWithRegex(regex).handler(CookieHandler.create());
    router.routeWithRegex(regex).handler(createBodyHandler());
    router.routeWithRegex(regex).failureHandler(this::onFailure).handler(this::onRequest);
  }

  protected void onRequest(RoutingContext context) {
    Map<String, String> pathParams = context.pathParams();
    String serviceName = pathParams.get("param0");
    String versionRule;
    String path;
    if (pathParams.size() == 3) {
      String pathVersion = pathParams.get("param1");
      path = "/" + pathParams.get("param2");
      versionRule = versionMapper.getOrCreate(pathVersion).getVersionRule();
    } else if (pathParams.size() == 2) {
      versionRule = DefinitionConst.VERSION_RULE_ALL;
      path = "/" + pathParams.get("param1");
    } else {
      throw new InvocationException(BAD_REQUEST,
          "request url must be /serviceName/version/operation or /serviceName/operation");
    }

    EdgeInvocation edgeInvocation = new EdgeInvocation();
    edgeInvocation.setVersionRule(versionRule);

    serviceName = DynamicPropertyFactory.getInstance()
        .getStringProperty("edge.routes." + serviceName, serviceName).get();

    edgeInvocation.init(serviceName, context, path, httpServerFilters);
    edgeInvocation.edgeInvoke();
  }
}