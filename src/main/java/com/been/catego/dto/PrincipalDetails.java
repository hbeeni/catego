package com.been.catego.dto;

import com.been.catego.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {

    private Long id;
    private String loginId;
    private String providerId;
    private String nickname;
    private Map<String, Object> oAuth2Attributes;

    public static PrincipalDetails of(Long id, String loginId, String providerId, String nickname) {
        return PrincipalDetails.of(id, loginId, providerId, nickname, Map.of());
    }

    public static PrincipalDetails of(Long id, String loginId, String providerId, String nickname,
                                      Map<String, Object> oAuth2Attributes) {
        return new PrincipalDetails(id, loginId, providerId, nickname, oAuth2Attributes);
    }

    public static PrincipalDetails from(User user) {
        return PrincipalDetails.of(user.getId(), user.getLoginId(), user.getProviderId(), user.getNickname());
    }

    public static PrincipalDetails from(User user, Map<String, Object> oAuth2Attributes) {
        return PrincipalDetails.of(user.getId(), user.getLoginId(), user.getProviderId(), user.getNickname(),
                oAuth2Attributes);
    }

    private PrincipalDetails(Long id, String loginId, String providerId, String nickname,
                             Map<String, Object> oAuth2Attributes) {
        this.id = id;
        this.loginId = loginId;
        this.providerId = providerId;
        this.nickname = nickname;
        this.oAuth2Attributes = oAuth2Attributes;
    }

    public String getProfileImageUrl() {
        return (String) oAuth2Attributes.get("picture");
    }

    @Override
    public String getName() {
        return loginId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2Attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(new SimpleGrantedAuthority(RoleType.USER.getRoleName()));
    }

    @Override
    public String getPassword() {
        return "password";
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public enum RoleType {

        USER("ROLE_USER");

        @Getter
        private final String roleName;

        RoleType(String roleName) {
            this.roleName = roleName;
        }
    }
}
