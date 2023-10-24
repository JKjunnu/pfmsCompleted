package com.nal.pfms.backend.controllers;


import com.nal.pfms.backend.config.UserAuthenticationProvider;
import com.nal.pfms.backend.dtos.CredentialsDto;
import com.nal.pfms.backend.dtos.PasswordDto;
import com.nal.pfms.backend.dtos.RegisterDto;
import com.nal.pfms.backend.dtos.UserDto;
import com.nal.pfms.backend.entity.User;
import com.nal.pfms.backend.entity.VerificationToken;
import com.nal.pfms.backend.event.RegistrationCompleteEvent;
import com.nal.pfms.backend.mappers.UserMapper;
import com.nal.pfms.backend.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private UserAuthenticationProvider userAuthProvider;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<UserDto> loginUser(@RequestBody CredentialsDto credentialsDto){
        User userEntity = userService.findUserByEmail(credentialsDto.getEmail());

        if(!userEntity.isEnabled()){
            throw new IllegalStateException("User Not Registered");
        }else{
            if(!userService.validateUserPassword(userEntity, credentialsDto)){
                throw new IllegalStateException("Credentials Don't Match");
            }else {
//                UserDto userResponse = new UserDto();
//                userResponse.setId(userEntity.getId());
//                userResponse.setEmail(userEntity.getEmail());
//                userResponse.setRole(userEntity.getRole());
//                userResponse.setEnabled(userEntity.isEnabled());
//                userResponse.setFirstName(userEntity.getFirstName());
//                userResponse.setLastName(userEntity.getLastName());
//                userResponse.setPassword(userEntity.getPassword());
                UserDto userResponse = userMapper.toUserDto(userEntity);
                userResponse.setToken(userAuthProvider.createToken(userResponse));
                return ResponseEntity.ok(userResponse);
            }
        }


    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody RegisterDto registerDto, final HttpServletRequest request) {
        User userEntity = userService.registerUser(registerDto);
        publisher.publishEvent(new RegistrationCompleteEvent(
                userEntity,
                applicationUrl(request)
        ));

//        UserDto userResponse = new UserDto();
//        userResponse.setId(user.getId());
//        userResponse.setEmail(user.getEmail());
//        userResponse.setRole(user.getRole());
//        userResponse.setEnabled(user.isEnabled());
//        userResponse.setFirstName(user.getFirstName());
//        userResponse.setLastName(user.getLastName());
//        userResponse.setPassword(user.getPassword());

        UserDto userResponse = userMapper.toUserDto(userEntity);
        userResponse.setToken(userAuthProvider.createToken(userResponse));


        return ResponseEntity.created(URI.create("/users/" + userResponse.getId())).body(userResponse);
    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {
        String result = userService.validateVerificationToken(token);
        if(result.equalsIgnoreCase("valid")) {
            return "User Verified Successfully";
        }
        return "Bad User";
    }


    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken,
                                          HttpServletRequest request) {
        VerificationToken verificationToken
                = userService.generateNewVerificationToken(oldToken);
        User user = verificationToken.getUser();
        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
        return "Verification Link Sent";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordDto passwordDto, HttpServletRequest request) {
        User user = userService.findUserByEmail(passwordDto.getEmail());
        String url = "";
        if(user!=null) {
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user,token);
            url = passwordResetTokenMail(user,applicationUrl(request), token);
        }
        return url;
    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token,
                               @RequestBody PasswordDto passwordDto) {
        String result = userService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")) {
            return "Invalid Token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if(user.isPresent()) {
            userService.changePassword(user.get(), passwordDto.getNewPassword());
            return "Password Reset Successfully";
        } else {
            return "Invalid Token";
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordDto passwordDto){
        User user = userService.findUserByEmail(passwordDto.getEmail());
        if(!userService.checkIfValidOldPassword(user, passwordDto.getOldPassword())) {
            return "Invalid Old Password";
        }
        //Save New Password
        userService.changePassword(user, passwordDto.getNewPassword());
        return "Password Changed Successfully";
    }




    //Rebuild code in event listener
    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url =
                applicationUrl
                        + "/savePassword?token="
                        + token;

        //sendVerificationEmail()
        log.info("Click the link to Reset your Password: {}",
                url);
        return url;
    }


    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        String url =
                applicationUrl
                        + "/verifyRegistration?token="
                        + verificationToken.getToken();

        //sendVerificationEmail()
        log.info("Click the link to verify your account: {}",
                url);
    }


    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();
    }
}
