package com.expenseflow.security;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {

    private final String id;
    private final String email;
    private final String password;
    private final String fullName;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final LocalDateTime lastLogoutAt;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UserEntity user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        // User status must be ACTIVE to be enabled in Spring Security context
        this.enabled = user.getStatus() == UserStatus.ACTIVE;
        // User status must not be LOCKED
        this.accountNonLocked = user.getStatus() != UserStatus.LOCKED;
        this.lastLogoutAt = user.getLastLogoutAt();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }

    /**
     * Returns true if the given JWT issuedAt date is BEFORE the user's last logout.
     * This allows the filter to immediately invalidate any access token that was
     * issued in a previous session — no blacklist or Redis required.
     */
    public boolean isIssuedBeforeLogout(Date jwtIssuedAt) {
        if (lastLogoutAt == null || jwtIssuedAt == null) return false;
        LocalDateTime issuedAt = jwtIssuedAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return issuedAt.isBefore(lastLogoutAt);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
