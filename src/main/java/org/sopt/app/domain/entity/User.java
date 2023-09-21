package org.sopt.app.domain.entity;

import java.util.Collection;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "app_users", schema = "app_dev")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @Column
    public String username;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, name = "is_opt_in")
    @ColumnDefault("false")
    private Boolean isOptIn;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserNotificationOption option;

    @Column(name = "playground_id", unique = true)
    private Long playgroundId;

    @Column(name = "playground_token")
    private String playgroundToken;

    @Builder
    public User(String username, String nickname, Long playgroundId) {
        this.username = username;
        // Default : 모든 알림 설정 OFF
        this.isOptIn = false;
        this.playgroundId = playgroundId;
    }

    // option 등록
    public void updateNotificationOption(UserNotificationOption option) {
        this.option = option;
    }

    public void updatePlaygroundUserInfo(String username, String playgroundToken) {
        this.username = username;
        this.playgroundToken = playgroundToken;
    }

    public void updateOptIn(Boolean isOptIn) {
        this.isOptIn = isOptIn;
    }

    // UserDetails Override Methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getPassword() {
        return null;
    }
}
