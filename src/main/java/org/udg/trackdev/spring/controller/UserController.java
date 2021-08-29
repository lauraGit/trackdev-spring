package org.udg.trackdev.spring.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.entity.views.PrivacyLevelViews;
import org.udg.trackdev.spring.service.UserService;


import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.security.Principal;

// This class is used to manage users and sign up
@RequestMapping(path = "/users")
@RestController
public class UserController extends BaseController {

    @Autowired
    UserService userService;

    /**
     *  Returns the public profile of any user.
     * @param principal The current authenticated entity
     * @param username The username of the user to request.
     * @return The User identified by username
     */
    @GetMapping(path = "/all/{username}")
    @JsonView(PrivacyLevelViews.Public.class)
    public User getPublic(Principal principal, @PathVariable("username") String username) {
        super.checkLoggedIn(principal);
        return userService.getByUsername(username);
    }

    @PostMapping(path = "/register")
    public ResponseEntity register(Principal principal, @Valid @RequestBody RegisterT ru) {
        checkNotLoggedIn(principal);
        userService.register(ru.username, ru.email, ru.password);
        return okNoContent();
    }

    static class RegisterT {
        @NotBlank
        @Size(min = 4, max = User.USERNAME_LENGTH)
        @Pattern(regexp = "[a-zA-Z0-9]+")
        public String username;

        @NotBlank
        @Email
        @Size(max = User.EMAIL_LENGTH)
        public String email;

        @NotBlank
        @Size(min = 8, max = 50)
        public String password;
    }

}
