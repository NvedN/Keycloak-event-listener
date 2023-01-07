package ru.spi.eventlistenerprovider.provider;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class UserDTO {

  public String username;
  public String email;
  public Long createdTimestamp;
  public String firstName;
  public String lastName;

  @Override
  public String toString() {
    return "{" +
        "\"username\":\"" + username + '\"' +
        ", \"email\":\"" + email + '\"' +
        ", \"createdTimestamp\":\"" + createdTimestamp + '\"'+
        ", \"firstName\":\"" + firstName + '\"' +
        ", \"lastName\":\"" + lastName + '\"' +
        "}";
  }
}
