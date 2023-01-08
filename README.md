# Keycloak-register-event-listener guide 
for Keycloak version 20+

This extension is designed to synchronize the database of users of the Keycloak with the database of your application.  
You must fill in a few parameters to run this extension in your own environment.


## CustomRequest.java. 

1. In file src/main/java/ru/spi/eventlistenerprovider/provider/CustomRequest.java you must specife variable endPointCreateUser - This is a restEndpoint in your application that will accept the data of the newly created user and add it to your base
2. To ensure the security of your endpoint, you will need to check the validity of the JWT token on the side of the external application, which will be generated and sent along with the new user.  
3. The token is generated using the RS256 algorithm, so you need a public key from your Keycloak realm.  
<img width="1476" alt="keyKey" src="https://user-images.githubusercontent.com/35899629/211143777-de462c72-7502-4017-bd03-58c4f0b70c57.png">


Example: 

```

public static String KEYCLOAK_PUBLIC_KEY = Publick key from keyclock 

/**
   * Endpint to sync user with keycloak DB
   *
   * @param token {@code String} generated token from keycloak
   * @param userDTO {@code UserDTO} user DTO
   * @throws NoSuchAlgorithmException if something went wrong with request
   * @throws InvalidKeySpecException if something went wrong with request
   */
  @PostMapping(value = "/createUser")
  public void syncUsersWithKeycloak(
      @RequestHeader(value = "token") String token, @RequestBody UserDTO userDTO) throws Exception {

    log.info(messageSource.getMessage("message.jwt.check", null, Locale.US));
    jwtUtil.validateToken(KEYCLOAK_PUBLIC_KEY, token);
    log.info(messageSource.getMessage("message.jwt.isValid", null, Locale.US));

    if (userDTO != null) {
      userService.createUser(userDTO);
    }
  }

```

```
/**
   * @param keycloakPublicKey{@code String} public key generated in keycloak web app and stored on
   *     application.properties file
   * @param token {@code String} toke from request header
   * @throws NoSuchAlgorithmException If something went wrong
   * @throws InvalidKeySpecException If something went wrong
   * @author NVN
   * @since 2022.12.28
   */
  public void validateToken(String keycloakPublicKey, String token)
      throws Exception {
    X509EncodedKeySpec keySpec =
        new X509EncodedKeySpec(Base64.getDecoder().decode(keycloakPublicKey));
    KeyFactory kf = KeyFactory.getInstance("RSA");
    PublicKey publicKey = kf.generatePublic(keySpec);

    Jwts.parserBuilder()
        .setSigningKey(publicKey) // <---- publicKey, not privateKey
        .build()
        .parseClaimsJws(token);

    try {
      Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
    } catch (Exception e) {
      log.error(messageSource.getMessage(
          "message.jwt.isInvalid", null, Locale.US), e);
      throw new Exception();
      // if you get error, that means token is invalid.
    }
  }
```


4. In the configuration file of the Keycloak at this path - /keycloak-20.0.1/conf/keycloak.conf  you must add a new parameter "app-url" - to the end of the file with a value that will point to the url of your server where the application is located. This parameter is read inside the src/main/java/ru/spi/eventlistenerprovider/provider/CustomRequest.appURL() method

Example: 

keycloak.conf 
```
app-url=http://192.168.1.179:8080/app
```

<img width="879" alt="appUrl" src="https://user-images.githubusercontent.com/35899629/211144157-7cd7f0dd-689f-4cca-83fd-7596289b1d22.png">

appURL()
```
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



