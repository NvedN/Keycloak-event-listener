package ru.spi.eventlistenerprovider.provider;


import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class UserDTO {

  public UUID id;
  public String username;
  public String email;
  public Long createdTimestamp;
  public String firstName;
  public String lastName;

  public UserDTO(UUID userId) {
    this.id = userId;
  }

  @Override
  public String toString() {
    return "{"
        + "\"id\":\""
        + id
        + '\"'
        + ", \"username\":\""
        + username
        + '\"'
        + ", \"email\":\""
        + email
        + '\"'
        + ", \"createdTimestamp\":\""
        + createdTimestamp
        + '\"'
        + ", \"firstName\":\""
        + firstName
        + '\"'
        + ", \"lastName\":\""
        + lastName
        + '\"'
        + "}";
  }
}
