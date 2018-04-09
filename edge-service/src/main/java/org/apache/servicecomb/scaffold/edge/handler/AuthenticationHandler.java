package org.apache.servicecomb.scaffold.edge.handler;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.core.Handler;
import org.apache.servicecomb.core.Invocation;
import org.apache.servicecomb.core.definition.MicroserviceMeta;
import org.apache.servicecomb.foundation.vertx.http.VertxServerRequestToHttpServletRequest;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.apache.servicecomb.swagger.invocation.AsyncResponse;
import org.apache.servicecomb.swagger.invocation.InvocationType;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.web.client.RestTemplate;

public class AuthenticationHandler implements Handler {
  private static final String REST_REQUEST = "servicecomb-rest-request";

  //TODO: 同步调用，将改进为异步调用CseAsyncRestTemplate
  private final RestTemplate restTemplate = RestTemplateBuilder.create();

  @Override
  public void init(MicroserviceMeta microserviceMeta, InvocationType invocationType) {
  }

  @Override
  public void handle(Invocation invocation, AsyncResponse asyncResponse) throws Exception {
    //只有外部到Edge的调用才需要Token认证，Edge与内部微服务之间的调用不需要认证
    if (isEdgeProxyInvocation(invocation)) {
      if (!authenticateEdgeInvocation(invocation, asyncResponse)) {
        return;
      }
    }
    invocation.next(asyncResponse);
  }

  private boolean isEdgeProxyInvocation(Invocation invocation) {
    return invocation.getHandlerContext().containsKey(REST_REQUEST);
  }

  private boolean authenticateEdgeInvocation(Invocation invocation, AsyncResponse asyncResponse) {
    //不允许通过Edge直接调用validate验证，user-service的validate转为Edge内部专用
    if (isValidateInvocation(invocation)) {
      asyncResponse.consumerFail(new InvocationException(Status.BAD_REQUEST, "unsupported invoke validate by edge"));
      return false;
    } else if (!isValidationRequiredInvocation(invocation)) {
      String token = ((VertxServerRequestToHttpServletRequest) invocation.getHandlerContext()
          .get(REST_REQUEST)).getHeader(AUTHORIZATION);
      if (StringUtils.isNotEmpty(token)) {
        if (restTemplate.getForObject("cse://user-service/validate?token={token}", Boolean.class, token)) {
          return true;
        }
        asyncResponse.consumerFail(
            new InvocationException(Status.UNAUTHORIZED, "authentication failed, invalid token"));
        return false;
      }
      asyncResponse.consumerFail(
          new InvocationException(Status.UNAUTHORIZED, "authentication failed, missing AUTHORIZATION header"));
      return false;
    }
    return true;
  }

  private boolean isValidateInvocation(Invocation invocation) {
    return "user-service".equals(invocation.getMicroserviceName()) && "validate".equals(invocation.getOperationName());
  }

  //不是Login和Logon的一律需要校验Token
  private boolean isValidationRequiredInvocation(Invocation invocation) {
    return "user-service".equals(invocation.getMicroserviceName()) &&
        ("login".equals(invocation.getOperationName()) || "logon".equals(invocation.getOperationName()));
  }
}
