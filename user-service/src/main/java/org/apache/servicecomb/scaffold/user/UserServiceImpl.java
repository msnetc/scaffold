package org.apache.servicecomb.scaffold.user;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.scaffold.user.api.LoginRequestDTO;
import org.apache.servicecomb.scaffold.user.api.LoginResponseDTO;
import org.apache.servicecomb.scaffold.user.api.LogonRequestDTO;
import org.apache.servicecomb.scaffold.user.api.LogonResponseDTO;
import org.apache.servicecomb.scaffold.user.api.UserService;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestSchema(schemaId = "user")
@RequestMapping(path = "/")
public class UserServiceImpl implements UserService {
  private final UserRepository repository;
  private final TokenStore tokenStore;

  @Autowired
  public UserServiceImpl(UserRepository repository, TokenStore tokenStore) {
    this.repository = repository;
    this.tokenStore = tokenStore;
  }

  @Override
  @PostMapping(path = "logon")
  public ResponseEntity<LogonResponseDTO> logon(@RequestBody LogonRequestDTO user) {
    if (validateUser(user)) {
      UserEntity dbUser = repository.findByName(user.getName());
      if (dbUser == null) {
        UserEntity entity = new UserEntity(user.getName(), user.getPassword(), 0D);
        repository.save(entity);
        return new ResponseEntity<>(new LogonResponseDTO(entity.getId()), HttpStatus.OK);
      }
      throw new InvocationException(BAD_REQUEST, "user name had exist");
    }
    throw new InvocationException(BAD_REQUEST, "incorrect user");
  }

  @Override
  @PostMapping(path = "login")
  public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO user) {
    if (validateUser(user)) {
      UserEntity dbUser = repository.findByName(user.getName());
      if (dbUser != null) {
        if (dbUser.getPassword().equals(user.getPassword())) {
          String token = tokenStore.generate(user.getName());
          HttpHeaders headers = generateAuthenticationHeaders(token);
          //add authentication header
          return new ResponseEntity<>(new LoginResponseDTO(), headers, HttpStatus.OK);
        }
        throw new InvocationException(BAD_REQUEST, "wrong password");
      }
      throw new InvocationException(BAD_REQUEST, "user name not exist");
    }
    throw new InvocationException(BAD_REQUEST, "incorrect user");
  }

  @Override
  @GetMapping(path = "getDeposit")
  public double getDeposit(String userName) {
    if (StringUtils.isNotEmpty(userName)) {
      UserEntity dbUser = repository.findByName(userName);
      if (dbUser != null) {
        return dbUser.getDeposit();
      }
      throw new InvocationException(BAD_REQUEST, "user name not exist");
    }
    throw new InvocationException(BAD_REQUEST, "incorrect user name");
  }

  private boolean validateUser(LogonRequestDTO user) {
    return user != null && StringUtils.isNotEmpty(user.getName()) && StringUtils.isNotEmpty(user.getPassword());
  }

  private boolean validateUser(LoginRequestDTO user) {
    return user != null && StringUtils.isNotEmpty(user.getName()) && StringUtils.isNotEmpty(user.getPassword());
  }

  private HttpHeaders generateAuthenticationHeaders(String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(AUTHORIZATION, token);
    return headers;
  }
}
