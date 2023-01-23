# Keycloak-register-event-listener guide 
for Keycloak version 20+

This extension is designed to synchronize the database of users of the Keycloak with the database of your application.  
You must fill in a few parameters to run this extension in your own environment.


## CustomRequest.java. 

1. In file `src/main/java/ru/spi/eventlistenerprovider/provider/CustomRequest.java` you must specify variable `endPointCreateUser` and `endPointDeleteUser` - This is a restEndpoint in your application that will accept the data of the newly created user and add it to your base

Example: 

```java

public static String KEYCLOAK_PUBLIC_KEY = "Publick_key_from_keyclock" 

/**
   * Endpoint to sync user with keycloak DB
   *
   * @param userDTO {@code UserDTO} user DTO
   * @author NVN
   * @since 2023.01.17
   */
  @PostMapping(value = "/createUser")
  public void createOrUpdateUserFromKeycloak(@RequestBody UserDTO userDTO) {
    userService.createUser(userDTO);
  }
  
  
  /**
   * Endpoint to delete user from DB
   *
   * @param userDTO {@code UserDTO} user DTO
   * @author NVN
   * @since 2023.01.17
   */
  @PostMapping(value = "/deleteUser")
  public void deleteUser(@RequestBody UserDTO userDTO) {
    userService.deleteUser(userDTO.getId());
  }
```



4. In the configuration file of the Keycloak at this path - `/keycloak-20.0.1/conf/keycloak.conf`  you must add a new parameter "app-url" - to the end of the file with a value that will point to the url of your server where the application is located. This parameter is read inside the `src/main/java/ru/spi/eventlistenerprovider/provider/CustomRequest.appURL()` method

Example: 

keycloak.conf 
```
app-url=http://localhost:8080/app
```

<img width="879" alt="appUrl" src="https://user-images.githubusercontent.com/35899629/211144157-7cd7f0dd-689f-4cca-83fd-7596289b1d22.png">

appURL()
```java
/**
   * Method to read url from keycloak config file
   *
   * @return url to backend app
   * @author NVN
   * @since 2022.12.29
   */
  public static String appURL() {
    String path = System.getProperty("jboss.server.config.dir");
    Properties prop = new Properties();
    String fileName = path + "/keycloak.conf";
    try (FileInputStream fis = new FileInputStream(fileName)) {
      prop.load(fis);
    } catch (IOException ignored) {
      log.error("can't read file", ignored);
    }
    return prop.getProperty("app-url");
  }
```

## Jar 

And finally you can build JAR and use this keycloak event listener



