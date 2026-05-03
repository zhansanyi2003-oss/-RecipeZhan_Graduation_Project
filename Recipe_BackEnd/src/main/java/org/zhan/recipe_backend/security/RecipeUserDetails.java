package org.zhan.recipe_backend.security; // 放在 security 包下

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.zhan.recipe_backend.common.RoleEnum;
import org.zhan.recipe_backend.entity.User; // 引入你的实体类 // 引入你的枚举

import java.util.Collection;
import java.util.Collections;

// 🌟 重点：必须是 public class，这样 Controller 才能 import 它！
public class RecipeUserDetails implements UserDetails {

    private final User user; // 包装你数据库里的真实用户

    public RecipeUserDetails(User user) {
        this.user = user;
    }

    // 🌟 重点：提供一个方法，让 Controller 能把真实的 User 掏出来
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 如果没有角色，默认给 USER
        RoleEnum role = user.getRole() != null ? user.getRole() : RoleEnum.USER;
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return user.getPassword(); }
    @Override
    public String getUsername() { return user.getUsername(); }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}