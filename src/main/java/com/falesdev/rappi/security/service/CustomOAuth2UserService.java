package com.falesdev.rappi.security.service;

import com.falesdev.rappi.domain.document.User;
import com.falesdev.rappi.security.RappiUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>{

    private final OAuth2UserManagementService oauth2UserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = getEmailFromAttributes(attributes, provider);
        String name = getNameFromAttributes(attributes, provider);
        String picture = getPictureFromAttributes(attributes, provider);

        User user = oauth2UserService.createOrUpdateOAuth2User(email, name, picture, provider);

        return new RappiUserDetails(user,attributes);
    }

    private String getEmailFromAttributes(Map<String, Object> attributes, String provider) {
        if ("google".equalsIgnoreCase(provider)) {
            return (String) attributes.get("email");
        }
        throw new OAuth2AuthenticationException("Proveedor no soportado");
    }

    private String getNameFromAttributes(Map<String, Object> attributes, String provider) {
        if ("google".equalsIgnoreCase(provider)) {
            return (String) attributes.get("name");
        }
        return "Usuario";
    }

    private String getPictureFromAttributes(Map<String, Object> attributes, String provider) {
        if ("google".equalsIgnoreCase(provider)) {
            return (String) attributes.get("picture");
        }
        return null;
    }
}
