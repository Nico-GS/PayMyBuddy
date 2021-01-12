package com.payMyBuddy.controllers;

import com.payMyBuddy.DTO.IdentifyDTO;
import com.payMyBuddy.DTO.PasswordDTO;
import com.payMyBuddy.DTO.UserDTO;
import com.payMyBuddy.model.User;
import com.payMyBuddy.repository.UserRepository;
import com.payMyBuddy.services.UserService;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    TestRestTemplate restTemplate = new TestRestTemplate();
    HttpHeaders httpHeaders = new HttpHeaders();

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private User createUser(String email){
        User user = new User();
        user.setPseudo("Nicolas");
        user.setEmail(email);
        user.setPassword("password");
        return user;
    }

    @Test
    @DisplayName("Create user in DB with sold to zero")
    public void createUserInDBWithSoldToZero(){
        User newUser = createUser("test@nicolas.test");
        assertTrue(userService.getByEmail(newUser.getEmail()).isEmpty());

        HttpEntity<User> entity = new HttpEntity<>(newUser, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        assertTrue(userService.getByEmail(newUser.getEmail()).isPresent());
        assertEquals(0, userRepository.findByEmail(newUser.getEmail()).get().getAmount());
        // Verify if password is crypt in DB
        assertNotEquals(newUser.getPassword(),userService.getByEmail(newUser.getEmail()).get().getPassword() );
    }

    @Test
    @DisplayName("Create user return bad request is user email is already in DB")
    public void createUserReturnBadRequestIfEmailIsAlreadyInDB(){
        User newUser = createUser("test@nicolas.com");
        HttpEntity<User> entity = new HttpEntity<>(newUser, httpHeaders);
        restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );
        assertEquals(1, userService.getAll().size());
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Verify that no other user was created
        assertEquals(1, userService.getAll().size());
    }

    @Test
    @DisplayName("Update user pseudo if user is in DB")
    public void updatePseudoIfUserAlreadyExistInDB(){
        User newUser = createUser("nicolas@test.com");
        HttpEntity<User> entity = new HttpEntity<>(newUser, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        User alreadyInDb = userService.getByEmail(newUser.getEmail()).get();
        assertEquals("Nicolas",alreadyInDb.getPseudo());
        UserDTO pseudoUser = new UserDTO();
        pseudoUser.setEmail(newUser.getEmail());
        pseudoUser.setPseudo("Nicolas");

        HttpEntity<UserDTO> entity2 = new HttpEntity<>(pseudoUser, httpHeaders);
        response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.PUT, entity2, String.class
        );
        assertEquals(pseudoUser.getPseudo(), userService.getByEmail(newUser.getEmail()).get().getPseudo());
    }

    @Test
    @DisplayName("Return bad request for update pseudo user if user isn't in DB")
    public void updatePseudoBadRequestIfUserNotExistingInDB(){
        User newUser = createUser("test@nicolas.com");
        HttpEntity<User> entity = new HttpEntity<>(newUser, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        UserDTO pseudoUser = new UserDTO();
        pseudoUser.setEmail("test@test.com");
        pseudoUser.setPseudo("Nico");

        HttpEntity<UserDTO> entity2 = new HttpEntity<>(pseudoUser, httpHeaders);
        response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.PUT, entity2, String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("When user identify OK return true when credential ok")
    public void returnTrueWhenUserIdentifyIsCorrectCredential(){
        User newUser = createUser("nicolas@test.com");
        HttpEntity<User> entity = new HttpEntity<>(newUser, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        IdentifyDTO identifyDTO = new IdentifyDTO();
        identifyDTO.setEmail(newUser.getEmail());
        identifyDTO.setPassword(newUser.getPassword());

        HttpEntity<IdentifyDTO> entity2 = new HttpEntity<>(identifyDTO, httpHeaders);
        ResponseEntity response2 = restTemplate.exchange(
                createURLWithPort("/identify"), HttpMethod.POST, entity2, String.class
        );

        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    @Test
    @DisplayName("When user credential is incorrect return false")
    public void returnFalseWhenUserCredentialIsIncorrect(){
        User newUser = createUser("nicolas@test.com");
        HttpEntity<User> entity = new HttpEntity<>(newUser, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        IdentifyDTO identifyDto = new IdentifyDTO();
        identifyDto.setEmail(newUser.getEmail());
        identifyDto.setPassword("WrongPassword");

        HttpEntity<IdentifyDTO> entity2 = new HttpEntity<>(identifyDto, httpHeaders);
        ResponseEntity response2 = restTemplate.exchange(
                createURLWithPort("/identify"), HttpMethod.POST, entity2, String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    @DisplayName("When user email is not in DB return bad request")
    public void whenUserEmailIsNotInDBReturnBadRequestIdentify(){
        User newUser = createUser("nicolas@nicolas.com");
        HttpEntity<User> entity = new HttpEntity<>(newUser, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        IdentifyDTO identifyDTO = new IdentifyDTO();
        identifyDTO.setEmail("Mauvais@Email.com");
        identifyDTO.setPassword(newUser.getEmail());

        HttpEntity<IdentifyDTO> entity2 = new HttpEntity<>(identifyDTO, httpHeaders);
        ResponseEntity response2 = restTemplate.exchange(
                createURLWithPort("/identify"), HttpMethod.POST, entity2, String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    @DisplayName("Password modify in DB OK if user credentials is correct")
    public void updatePasswordInDBOkWhenUserCredentialsIsCorrect(){
        User newUser = createUser("nicolas@nicolas.test");
        HttpEntity<User> entity = new HttpEntity<>(newUser, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        String oldPassword = userService.getByEmail(newUser.getEmail()).get().getPassword();
        PasswordDTO passwordToUpdate = new PasswordDTO();
        passwordToUpdate.setEmail(newUser.getEmail());
        passwordToUpdate.setOldPassword(newUser.getPassword());
        passwordToUpdate.setNewPassword("UpdateNewPassword");

        HttpEntity<PasswordDTO> entity1 = new HttpEntity<>(passwordToUpdate, httpHeaders);
        ResponseEntity response2 = restTemplate.exchange(
                createURLWithPort("/userPassword"), HttpMethod.PUT, entity1, String.class
        );

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotEquals(oldPassword,userService.getByEmail(newUser.getEmail()).get().getPassword());
    }

    @Test
    @DisplayName("Update password bad request if old password is incorrect")
    public void updatePasswordBadRequestIfOldPasswordIsIncorrect(){
        User newUser = createUser("nico@las.com");
        HttpEntity<User> entity = new HttpEntity<>(newUser, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        PasswordDTO passwordToUpdate = new PasswordDTO();
        passwordToUpdate.setEmail(newUser.getEmail());
        passwordToUpdate.setOldPassword("OldPassword");
        passwordToUpdate.setNewPassword("NewPassword");

        HttpEntity<PasswordDTO> entity1 = new HttpEntity<>(passwordToUpdate, httpHeaders);
        ResponseEntity response2 = restTemplate.exchange(
                createURLWithPort("/userPassword"), HttpMethod.PUT, entity1, String.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    @DisplayName("Update password bad request if email is not found in DB")
    public void updatePasswordBadRequestIfEmailNotFoundInDB(){
        User newUser = createUser("ni@colas.com");
        HttpEntity<User> entity = new HttpEntity<>(newUser, httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        PasswordDTO passwordToUpdate = new PasswordDTO();
        passwordToUpdate.setEmail("WrongEmail@test.com");
        passwordToUpdate.setOldPassword(newUser.getPassword());
        passwordToUpdate.setNewPassword("NewPassword");

        HttpEntity<PasswordDTO> entity1 = new HttpEntity<>(passwordToUpdate, httpHeaders);
        ResponseEntity response2 = restTemplate.exchange(
                createURLWithPort("/userPassword"), HttpMethod.PUT, entity1, String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    @DisplayName("Add friend OK if the two users exist in DB")
    public void addFriendOkIfTwoUsersExistInDb(){
        User user1 = createUser("nicolas@test.com");
        User user2 = createUser("jacques@test.com");

        HttpEntity<User> entity = new HttpEntity<>(user1, httpHeaders);
        restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        entity = new HttpEntity<>(user2, httpHeaders);
        restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );
        // Check if no connection exist
        user1 = userService.getByEmail("nicolas@test.com").get();
        assertTrue(user1.getListFriend().isEmpty());

        // Create connection between user1 and user2
        HttpEntity entity2 = new HttpEntity(httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("/addFriend/1/2"), HttpMethod.PUT, entity, String.class
        );

        // Check if connection is created
        user1 = userService.getByEmail("nicolas@test.com").get();
        user2 = userService.getByEmail("jacques@test.com").get();
        assertTrue(user1.getListFriend().contains(user2));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("Add friend Bad Request if user not exist in DB")
    public void addFriendBadRequestIfUserNotExistInDb(){
        User user1 = createUser("nicolas@test.com");

        // Create the first user in DB
        HttpEntity<User> entity = new HttpEntity<>(user1, httpHeaders);
        restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        // Check if no connection exists
        user1 = userService.getByEmail("nicolas@test.com").get();
        assertTrue(user1.getListFriend().isEmpty());

        // Create connection between user1 and user2
        HttpEntity entity2 = new HttpEntity(httpHeaders);
        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("/addFriend/1/2"), HttpMethod.PUT, entity, String.class
        );

        // Check if no connection is created
        user1 = userService.getByEmail("nicolas@test.com").get();
        assertTrue(user1.getListFriend().isEmpty());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Add friend return Bad Request if connection already exist")
    public void addFriendReturnBadRequestIfConnectionAlreadyExist(){
        User user1 = createUser("nicolas@test.com");
        User user2 = createUser("jacques@test.com");

        HttpEntity<User> entity = new HttpEntity<>(user1, httpHeaders);
        restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        entity = new HttpEntity<>(user2, httpHeaders);
        restTemplate.exchange(
                createURLWithPort("user"), HttpMethod.POST, entity, String.class
        );

        HttpEntity entity2 = new HttpEntity(httpHeaders);
        restTemplate.exchange(
                createURLWithPort("/addFriend/1/2"), HttpMethod.PUT, entity, String.class
        );

        ResponseEntity response = restTemplate.exchange(
                createURLWithPort("/addFriend/1/2"), HttpMethod.PUT, entity, String.class
        );

        user1 = userService.getByEmail("nicolas@test.com").get();
        assertEquals(1, user1.getListFriend().size());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
