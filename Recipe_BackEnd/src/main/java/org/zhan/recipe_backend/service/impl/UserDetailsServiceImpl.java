package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.zhan.recipe_backend.common.RoleEnum;
import org.zhan.recipe_backend.entity.User;
import org.zhan.recipe_backend.repository.UserRepository;
import org.zhan.recipe_backend.security.RecipeUserDetails;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public RecipeUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        return new RecipeUserDetails(user);
    }
}

