package com.payMyBuddy.controllers;

import com.payMyBuddy.DTO.IdentifyDTO;
import com.payMyBuddy.DTO.PasswordDTO;
import com.payMyBuddy.DTO.UserDTO;
import com.payMyBuddy.model.User;
import com.payMyBuddy.services.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class UserController {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserService userService;

    Logger LOGGER = LoggerFactory.getLogger(UserController.class);


    @GetMapping({"/", "/home"})
    public String getHome() {
        return "<h1> Welcome to the app PayMyBuddy </h1>";
    }

    /**
     * Create new user in DB
     * @param user the user to be create in DB
     * @return 200 successful | 400 failed
     */
    @PostMapping(value = "/user")
    public ResponseEntity createUser(@RequestBody User user){
        // Create if user email isn't in DB
        if(userService.getByEmail(user.getEmail()).isEmpty()){
            userService.createUser(user);
            LOGGER.info("Success create User");
            return ResponseEntity.ok(modelMapper.map(user, UserDTO.class));
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Get user by mail
     * @param email the user's email
     * @return a userDto, BAD_REQUEST if the user doesn't exist.
     */
    @GetMapping(value ="/getUser")
    public ResponseEntity<UserDTO> getByEmail(@RequestParam("email") String email){
        Optional<User> user = userService.getByEmail(email);
        if(user.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        LOGGER.info("Success find user by email");
        return ResponseEntity.ok(modelMapper.map(user.get(), UserDTO.class));
    }

    /**
     * Update user pseudo
     * @param userDTO user's email and new nickname
     * @return 200 success | 400 failed
     */
    @PutMapping(value = "/user")
    public ResponseEntity<UserDTO> updatePseudo(@RequestBody UserDTO userDTO){
        if(userService.getByEmail(userDTO.getEmail()).isPresent()){
            userService.updateNickname(userDTO);
            LOGGER.info("Success update user pseudo");
            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Verify identity of the user with its credentials
     * @param idDTO contains user's email and password sent for verification
     * @return 200 if credentials corrects | 400 if failed
     */
    @PostMapping(value = "/identify")
    public ResponseEntity identify(@RequestBody IdentifyDTO idDTO){
        if(userService.getByEmail(idDTO.getEmail()).isPresent()){
            if(userService.identify(idDTO)){
                LOGGER.info("Connection success");
                return ResponseEntity.ok().build();
            }
        }

        return ResponseEntity.badRequest().build();
    }

    /**
     * Update user password
     * @param passwordUpdate contains email, old password, new password
     * @return 200 success | 400 failed
     */
    @PutMapping(value = "/userPassword")
    public ResponseEntity updatePassword(@RequestBody PasswordDTO passwordUpdate){
        if (userService.getByEmail(passwordUpdate.getEmail()).isPresent()){
            if(userService.updatePassword(passwordUpdate)){
                LOGGER.info("Password update success");
                return ResponseEntity.ok().build();
            }
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * Add a friend, require two users
     * @param fromUser the owner's id
     * @param toUser the target's id
     * @return 201 success | 400 otherwise
     */
    @PutMapping(value = "/addFriend/{fromUser}/{toUser}")
    public ResponseEntity addFriend(@PathVariable Long fromUser, @PathVariable Long toUser){
        // If one user don't exist, return bad request
        if (userService.getById(fromUser).isEmpty() || userService.getById(toUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User owner = userService.getById(fromUser).get();
        User target = userService.getById(toUser).get();
        // Save connection in DB
        if (!owner.getListFriend().contains(target)){
            userService.addFriend(owner, target);
            LOGGER.info("Add friend success");
            return ResponseEntity.status(201).build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    /** Delete a friend, require two users
     *
     * @param fromUser the owner's id
     * @param toUser the target's id
     * @return 200 success | 400 otherwise
     */
    @PutMapping(value = "/removeFriend/{fromUser}/{toUser}")
    public ResponseEntity removeFriend(@PathVariable Long fromUser, @PathVariable Long toUser){
        // If one user don't exist, return bad request
        if (userService.getById(fromUser).isEmpty() || userService.getById(toUser).isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        User owner = userService.getById(fromUser).get();
        User target = userService.getById(toUser).get();
        // Delete connection
        if (owner.getListFriend().contains(target)){
            userService.removeFriend(owner, target);
            LOGGER.info("Remove friend success");
            return ResponseEntity.ok().build();
        }
        LOGGER.info(owner.getListFriend().toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
