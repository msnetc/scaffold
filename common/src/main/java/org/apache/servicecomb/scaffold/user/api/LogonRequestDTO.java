package org.apache.servicecomb.scaffold.user.api;

public class LogonRequestDTO {
  private String name;

  private String password;

  public String getName() {
    return name;
  }

  public String getPassword() {
    return password;
  }

  public LogonRequestDTO() {
  }

  public LogonRequestDTO(String name, String password) {
    this.name = name;
    this.password = password;
  }
}
