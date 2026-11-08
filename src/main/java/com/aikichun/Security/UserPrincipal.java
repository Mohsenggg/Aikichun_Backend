package com.aikichun.Security;

import com.aikichun.user.entites.AccountStatus;
import com.aikichun.user.entites.Role;
import com.aikichun.user.entites.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final Role role;
    private final AccountStatus accountStatus;
    private final LocalDate subscriptionStartDate;
    private final LocalDate subscriptionEndDate;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash() != null ? user.getPasswordHash() : "";
        this.role = user.getRole();
        this.accountStatus = user.getAccountStatus();
        this.subscriptionStartDate = user.getSubscriptionStartDate();
        this.subscriptionEndDate = user.getSubscriptionEndDate();
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountStatus != AccountStatus.BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.accountStatus != AccountStatus.BLOCKED;
    }

    public boolean isSubscriptionActive(LocalDate today) {
        if (this.accountStatus != AccountStatus.ACTIVE) {
            return false;
        }
        if (this.subscriptionStartDate == null || this.subscriptionEndDate == null) {
            return false;
        }
        return !today.isBefore(this.subscriptionStartDate) && !today.isAfter(this.subscriptionEndDate);
    }

    public boolean isSubscriptionActive() {
        return isSubscriptionActive(LocalDate.now());
    }
}
