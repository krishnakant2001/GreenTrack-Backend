package com.greentrack.carbon_tracker_api.handlers;

import com.greentrack.carbon_tracker_api.entities.User;
import com.greentrack.carbon_tracker_api.security.JwtService;
import com.greentrack.carbon_tracker_api.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) token.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        log.info(oAuth2User.getAttribute("email"));

        User user = userService.getUserByEmail(email);

        if(user == null) {
            //create new user with OAuth2
            User newUser = User.builder()
                    .firstName(oAuth2User.getAttribute("given_name"))
                    .lastName(oAuth2User.getAttribute("family_name"))
                    .email(email)
                    .roles(new HashSet<>(Collections.singleton("ROLE_USER")))
                    .region("IN") // default region India
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            user = userService.savedNewUser(newUser);
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);

        String frontEndUrl = "http://localhost:3000/dashboard?token=" + accessToken;

        getRedirectStrategy().sendRedirect(request, response, frontEndUrl);

    }
}
