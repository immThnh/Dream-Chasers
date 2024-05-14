package com.example.demo.auth;

import com.example.demo.cloudinary.CloudService;
import com.example.demo.dto.*;
import com.example.demo.entity.data.Comment;
import com.example.demo.entity.data.Notification;
import com.example.demo.entity.data.Progress;
import com.example.demo.entity.user.Role;
import com.example.demo.entity.user.User;
import com.example.demo.jwt.JwtService;
import com.example.demo.jwt.Token;
import com.example.demo.mail.MailRequest;
import com.example.demo.repository.TokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.data.CourseRepository;
import com.example.demo.repository.data.LessonRepository;
import com.example.demo.repository.data.NotificationRepository;
import com.example.demo.repository.data.ProgressRepository;
import com.example.demo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final CourseRepository courseRepository;
    private final ProgressRepository progressRepository;

    private final JwtService jwtService;
    private final CloudService cloudService;
    private final PasswordEncoder passwordEncoder;
    private  final NotificationService notificationService;

    public ResponseObject removeAllNotificationsByEmail(String email) {
        if(!email.contains("@")) {
            email += "@gmail.com";
        }
        var user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).content("User not found ").build();
        }
        var result = notificationService.removeAllNotificationsByEmail(user.getId());
        if (result.size() > 0) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("Error while remove all notification").build();
        }
        user.setNotifications(new ArrayList<>());
        userRepository.save(user);
        return ResponseObject.builder().status(HttpStatus.OK).content(result).mess("Remove all notification successfully").build();
    }
    public ResponseObject readAllNotification(String email) {
        if(!email.contains("@")) {
            email += "@gmail.com";
        }
        var user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).content("User not found ").build();
        }
        var result = notificationService.readAllNotification(user.getId());
        if (result == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).content("Error while read notification").build();
        }
        return ResponseObject.builder().status(HttpStatus.OK).content(result).build();
    }
    public ResponseObject readNotification(String email, int id) {
        if(!email.contains("@")) {
            email += "@gmail.com";
        }
        var user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).content("User not found ").build();
        }
        var result = notificationService.readNotification(id);
        if (result == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).content("Error while read notification").build();
        }
        return ResponseObject.builder().status(HttpStatus.OK).content(result).build();
    }
    public ResponseObject getAllNotificationsByEmail(String email) {
        if(!email.contains("@")) {
            email += "@gmail.com";
        }
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).content("User not found").build();
        }
        return ResponseObject.builder().status(HttpStatus.OK).content(notificationService.getAllNotificationsByEmail(user.getId())).build();
    }


    public ResponseObject updateLessonsIds(String alias, int courseId, List<Integer> lessonIds) {
        var email = alias + "@gmail.com";
        System.out.println(alias + courseId + lessonIds);
        var user = userRepository.findByEmail(email).orElse(null);
        if(user == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("User not found").build();
        }
        var progress = progressRepository.findByCourseIdAndUser(courseId, user).orElse(null);
        if (progress == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("User has not registered for the course ").build();
        }
        progress.setLessonIds(lessonIds);
        progressRepository.save(progress);
        return ResponseObject.builder().status(HttpStatus.OK).build();
    }

    public ResponseObject getProgressByCourseId(String alias, int courseId) {
        var email = alias + "@gmail.com";
        var user = userRepository.findByEmail(email).orElse(null);
        if(user == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("User not found").build();
        }
        var progress = progressRepository.findByCourseIdAndUser(courseId, user).orElse(null);
        if (progress == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("User has not registered for the course ").build();
        }
        var progressDTO = ProgressDTO.builder().course(progress.getCourse()).lessonIds(progress.getLessonIds()).build();

        return ResponseObject.builder().status(HttpStatus.OK).content(progressDTO).build();

    }

    public boolean register(RegisterRequest request) {
        if(isUsedEmail(request.getEmail())) return  false;
        saveUser(request);
        return true;
    }

    public ResponseObject authenticate(AuthenticationRequest auth) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(auth.getEmail(), auth.getPassword()));
            var user = userRepository.findByEmail(auth.getEmail()).orElse(null);
            if(user == null) {
                return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).content("User is not exist.").build();
            }
            Token token = new Token(jwtService.generateToken(user));
            user.setToken(token);
            var userDTO = getUserDTOFromUser(user);
            userDTO.setToken(token.getToken());
            return ResponseObject.builder().status(HttpStatus.OK).content(userDTO).build();
        }
        catch (AuthenticationException ex) {
            System.out.println(ex.getMessage() + "Xác thực người dùng thất bại!");
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("error while authentication user with error " + ex.getMessage()).build();

        }
    }

    public UserDTO getUserDTOFromUser(User user) {
        return UserDTO.builder()
                .avatar(user.getAvatar())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    public ResponseObject getAllByPage(int page, int size) {
        var result = userRepository.findAll(PageRequest.of(page, size));
        return ResponseObject.builder().status(HttpStatus.OK).content(result).build();
    }

    public ResponseObject getAllUser() {
        var users = userRepository.findAll();
        return ResponseObject.builder().status(HttpStatus.OK).mess("Get data successfully").status(HttpStatus.OK).content(users).build();
    }

    public ResponseObject getAllRole() {
        return ResponseObject.builder().status(HttpStatus.OK).content(Role.values()).build();
    }

    public ResponseObject getUserByRole(String role, int page, int size) {
        if(Objects.equals(role, "All"))
            return ResponseObject.builder().status(HttpStatus.OK).content(userRepository.findAll(PageRequest.of(page, size))).build();
        return ResponseObject.builder().status(HttpStatus.OK).content(userRepository.findByRole(role, PageRequest.of(page, size))).build();
    }

    public ResponseObject getUserByEmail(String email) {
        if(!email.contains("@")) {
            email+="@gmail.com";
        }
        var user = userRepository.findByEmail(email).orElse(null);
        var userDTO = getUserDTOFromUser(user);
        if(user == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("Username not found").build();
        }
        return ResponseObject.builder().status(HttpStatus.OK).content(userDTO).mess("Successfully").build();
    }

    private void saveUser(RegisterRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)
                .build();
        userRepository.save(user);
    }


    public boolean isUsedEmail(String email) {
        var user = userRepository.findByEmail(email).orElse(null);
        System.out.println(user);
        return user != null;
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        var user = userRepository.findByPhoneNumber(phoneNumber).orElse(null);
        if(user == null) return false;
        return true;
    }
    public String getVerifyCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        String characters = "1234567890";
        for(int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }

    public boolean isValidCode(MailRequest request) {
        User user = findUserByEmail(request.getEmail());
        if(user == null || user.getCode().isEmpty()) return false;
            return user.getCode().get(request.getCode()).isAfter(LocalDateTime.now());
    }

    public boolean resetPassword(ResetPasswordRequest request) {
        var user = findUserByEmail(request.getEmail());
        if(user == null) return false;
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return true;
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public boolean saveCode(MailRequest request, String code) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if(user == null) return false;
        Map<String, LocalDateTime> token = new HashMap<String, LocalDateTime>();
        token.put(code, LocalDateTime.now().plus(48, ChronoUnit.HOURS));
        user.setCode(token);
        userRepository.save(user);
        return true;
    }
    public boolean saveCode(OtpVerifyRequest request, String code) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber()).orElse(null);
        if(user == null) return false;
        Map<String, LocalDateTime> token = new HashMap<String, LocalDateTime>();
        token.put(code, LocalDateTime.now().plus(48, ChronoUnit.HOURS));
        user.setCode(token);
        userRepository.save(user);
        return true;
    }

    public ResponseObject getUserByToken(String token) {
        String userName;
        try {
            userName = jwtService.extractUserName(token);
        }
        catch (Exception e) {
            System.out.println("getUserByToken: " + e.getMessage());
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("Username not found").build();
        }
        var user = userRepository.findByEmail(userName).orElse(null);
        if(user == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("Username not found").build();
        }
        return ResponseObject.builder().status(HttpStatus.OK).mess("Get user data successfully").build();
    }

    public ResponseObject updateProfile(UserDTO userDTO, MultipartFile avatar)  {
        var user = userRepository.findByEmail(userDTO.getEmail()).orElse(null);
        if(user == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("Username not found").build();
        }
        if(avatar != null) {
            try {
                user.setAvatar(cloudService.uploadImage(avatar.getBytes()));
            }
            catch (IOException ex) {
                System.out.println("updateProfile:  " + ex.getMessage());
                return ResponseObject.builder().status(HttpStatus.OK).mess("Error occurred when updating profile").content(userDTO).build();
            }
        }
        userDTO.setAvatar(user.getAvatar());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        userRepository.save(user);
        return ResponseObject.builder().status(HttpStatus.OK).mess("Update successfully").content(userDTO).build();
    }

    public ResponseObject updatePassword(PasswordDTO passwordDTO) {
        var user = userRepository.findByEmail(passwordDTO.getEmail()).orElse(null);
        if(user == null) {
            return ResponseObject.builder().mess("User not found").status(HttpStatus.BAD_REQUEST).build();
        }
        if(!passwordEncoder.matches( passwordDTO.getOldPassword(), user.getPassword())) {
            return ResponseObject.builder().mess("Old password not correct").status(HttpStatus.BAD_REQUEST).build();
        }
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);
        return ResponseObject.builder().status(HttpStatus.OK).mess("Update password successfully").build();
    }

    public ResponseObject enrollCourse(EnrollDTO enrollDTO) {

        var user = userRepository.findByEmail(enrollDTO.getEmail()).orElse(null);
        if (user == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("User not found").build();
        }

        var temp = progressRepository.findByCourseIdAndUser(enrollDTO.getCourseId(), user).orElse(null);
        if(temp != null) {
            return ResponseObject.builder().status(HttpStatus.OK).mess("Continue studying").content(temp).build();
        }

        var course = courseRepository.findById(enrollDTO.getCourseId()).orElse(null);
        if (course == null) {
            return ResponseObject.builder().status(HttpStatus.BAD_REQUEST).mess("Course not found").build();
        }

        Progress progress = Progress.builder().course(course).user(user).lessonIds(enrollDTO.getLessonIds()).build();
        progressRepository.save(progress);
        return ResponseObject.builder().status(HttpStatus.OK).mess("Enroll course successfully").content(progress).build();

    }

}
