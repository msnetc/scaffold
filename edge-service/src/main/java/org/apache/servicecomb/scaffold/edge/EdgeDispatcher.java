package org.apache.servicecomb.scaffold.edge;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.edge.core.AbstractEdgeDispatcher;
import org.apache.servicecomb.edge.core.EdgeInvocation;
import org.apache.servicecomb.scaffold.edge.darklaunch.DarkLaunchRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.config.DynamicPropertyFactory;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.CookieHandler;

/**
 *match /serviceName/operation
 */
public class EdgeDispatcher extends AbstractEdgeDispatcher {
  private static final ObjectMapper OBJ_MAPPER = new ObjectMapper();

  private static final Logger LOGGER = LoggerFactory.getLogger(EdgeDispatcher.class);

  //private final Map<String, DynamicStringProperty> properties = new ConcurrentHashMap<>();

  private final Map<String, DynamicDarkLaunchSetting> rules = new ConcurrentHashMap<>();

  public int getOrder() {
    return 10000;
  }

  public void init(Router router) {
    String regex = "/([^\\\\/]+)/(.*)";
    router.routeWithRegex(regex).handler(CookieHandler.create());
    router.routeWithRegex(regex).handler(createBodyHandler());
    router.routeWithRegex(regex).failureHandler(this::onFailure).handler(this::onRequest);
  }

  private void onRequest(RoutingContext context) {
    Map<String, String> pathParams = context.pathParams();
    final String service = pathParams.get("param0");
    String path = "/" + pathParams.get("param1");

    final String serviceName = DynamicPropertyFactory.getInstance()
        .getStringProperty("edge.routes." + service + ".service", service).get();

    String config = DynamicPropertyFactory.getInstance()
        .getStringProperty("edge.routes." + service + ".dark-launch-rules", "").getValue();
    DynamicDarkLaunchSetting setting;
    if (!rules.containsKey(service)) {
      setting = rules
          .computeIfAbsent(service, s -> new DynamicDarkLaunchSetting(config, parseRule(serviceName, config)));
    } else {
      setting = rules.get(service);
      //如果灰度发布配置变化则更新配置，Java Chassis 1.0.0-m1中apollo尚不支持callBack通知
      if (!setting.getConfig().equals(config)) {
        setting = new DynamicDarkLaunchSetting(config, parseRule(serviceName, config));
        rules.put(service, setting);
      }
    }

    EdgeInvocation edgeInvocation = new EdgeInvocation();
    edgeInvocation.setVersionRule(setting.getRule().matchVersion(context.request().headers().entries()));

    edgeInvocation.init(serviceName, context, path, httpServerFilters);
    edgeInvocation.edgeInvoke();
  }

  private DarkLaunchRule parseRule(String serviceName, String config) {
    LOGGER.info("Service : {} apply DarkLaunchRule : {}", serviceName, config);
    try {
      if (StringUtils.isNotEmpty(config)) {
        return OBJ_MAPPER.readValue(config, DarkLaunchRule.class);
      }
    } catch (IOException e) {
      LOGGER.error("parse rule failed", e);
    }
    return new DarkLaunchRule();
  }
}