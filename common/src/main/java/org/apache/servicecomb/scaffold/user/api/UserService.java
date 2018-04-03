package org.apache.servicecomb.scaffold.user.api;

import org.springframework.http.ResponseEntity;

public interface UserService {
  ResponseEntity<LogonResponseDTO> logon(LogonRequestDTO user);

  ResponseEntity<LoginResponseDTO> login(LoginRequestDTO user);

  double getDeposit(String userName);
}
