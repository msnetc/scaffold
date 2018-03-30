package org.apache.servicecomb.scaffold.user.api;

public class LoginRequestDTO {
  private String name;

  private String password;

  public String getName() {
    return name;
  }

  public String getPassword() {
    return password;
  }

  public LoginRequestDTO() {
  }

  public LoginRequestDTO(String name, String password) {
    this.name = name;
    this.password = password;
  }
}
