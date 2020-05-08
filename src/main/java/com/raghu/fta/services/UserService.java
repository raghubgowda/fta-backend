package com.raghu.fta.services;

import com.raghu.fta.dao.UsersRepo;
import com.raghu.fta.exceptions.EntityNotFoundException;
import com.raghu.fta.exceptions.ServiceException;
import com.raghu.fta.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UsersRepo usersRepo;

    public UserService(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    public List<User> getUsers(){
        List<User> users;
        try{
            users = usersRepo.findAll();
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while fetching the users from the database. Error: "+e.getMessage());
        }
        return users;
    }

    public User getUserById(String id){
        User user;
        Optional<User> userOptional;

        try{
            userOptional = usersRepo.findById(id);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while fetching the user with id "+id+". Error: "+e.getMessage());
        }

        if(userOptional.isPresent()){
            user = userOptional.get();
        }
        else{
            throw new EntityNotFoundException("User with id "+id+" not found");
        }
        return user;
    }

    public User getUserByEmail(String email){
        List<User> usersByEmail;

        try{
            usersByEmail = usersRepo.findUsersByEmail(email);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while authenticating the user with email "+email+". Error: "+e.getMessage());
        }

        if(usersByEmail == null || usersByEmail.isEmpty()){
            throw new EntityNotFoundException("User with email "+email+" not found");
        }

        return usersByEmail.get(0);
    }

    public boolean isEmailIdInUse(String email){
        return !usersRepo.findUsersByEmail(email).isEmpty();
    }

    public User updateUser(User user){
        User updatedUser;

        //Check if the user exists before updating it.
        getUserById(user.getId());

        try{
            updatedUser = usersRepo.save(user);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while updating the user details. Error: "+e.getMessage());
        }
        return updatedUser;
    }

    public User saveUser(User user){
        User savedUser;

        try{
            savedUser = usersRepo.save(user);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while saving the user details. Error: "+e.getMessage());
        }
        return savedUser;
    }

    public void deleteUser(String userId) {
        try{
            usersRepo.deleteById(userId);
        }
        catch (Exception e){
            logger.error(e.getMessage());
            throw new ServiceException("Exception while deleting the user with id "+userId+". Error: "+e.getMessage());
        }
    }

    private List<SimpleGrantedAuthority> getAuthority() {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if(user == null){
            throw new UsernameNotFoundException("Please login in");
        }
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), getAuthority());
    }
}
