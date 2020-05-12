package com.raghu.fta.restcontrollers;

import com.raghu.fta.model.User;
import com.raghu.fta.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    private UserService userService;

    private static Logger logger = LoggerFactory.getLogger(UsersController.class);

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    ResponseEntity<?> getUsers(){
        logger.debug("Fetching all the users");

        List<User> users = userService.getUsers();

        logger.debug("Fetched all the users");

        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    ResponseEntity<?> getUer(@PathVariable String userId){
        logger.debug("Fetching the details for the user with id: {}", userId);

        User userById = userService.getUserById(userId);

        logger.debug("User with id: {} is found", userById);

        return new ResponseEntity<>(userById, HttpStatus.OK);
    }

    @PostMapping("/sign-up")
    ResponseEntity<?> addUser(@RequestBody User user){
        logger.debug("Adding user: {}", user);

        if(user.getId() != null){
            return new ResponseEntity<>("User id should be null", HttpStatus.BAD_REQUEST);
        }

        if(userService.isEmailIdInUse(user.getEmail())){
            return new ResponseEntity<>("Email id already in use", HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = new BCryptPasswordEncoder().encode(user.getPassword());
        user.setPassword(encodedPassword);

        User newUser = userService.saveUser(user);

        logger.debug("User added successfully {}", newUser);

        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PutMapping()
    ResponseEntity<?> updateUser(@RequestBody User user){
        logger.debug("Updating the user: {}", user);

        if(user.getId() == null || Strings.isEmpty(user.getId())){
            return new ResponseEntity<>("User id cannot be null or empty", HttpStatus.BAD_REQUEST);
        }

        userService.updateUser(user);

        logger.debug("User updated successfully");

        return new ResponseEntity<>("User updated successfully", HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    ResponseEntity<?> deleteUser(@PathVariable String userId){
        logger.debug("Deleting the user with id: {}", userId);

        userService.deleteUser(userId);

        logger.debug("User with id: {} deleted successfully", userId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
