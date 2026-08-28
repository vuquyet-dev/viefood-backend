package com.viefood.viefoodidentitysvc.service.Impl;

import com.viefood.base.error.ErrorCode;
import com.viefood.base.error.ServiceException;
import com.viefood.base.logger.ApiLogger;
import com.viefood.viefoodidentitysvc.dto.req.UserReq;
import com.viefood.viefoodidentitysvc.dto.res.UserRes;
import com.viefood.viefoodidentitysvc.entity.User;
import com.viefood.viefoodidentitysvc.enums.UserRole;
import com.viefood.viefoodidentitysvc.enums.UserStatus;
import com.viefood.viefoodidentitysvc.repository.UserRepository;
import com.viefood.viefoodidentitysvc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger logger = ApiLogger.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public UserRes register(UserReq request) {
        if (userRepository.existsByUsername(request.getUsername()))
        {
            throw new ServiceException(ErrorCode.ERR_DATA_DUPLICATE, "Username already exists.", Map.of(
                    "username", request.getUsername()
            ));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        User saveUser = userRepository.save(user);
        logger.info("Register success username {}", request.getUsername());

        return UserRes.builder()
                .id(saveUser.getId())
                .username(saveUser.getUsername())
                .role(saveUser.getRole())
                .status(saveUser.getStatus())
                .build();
    }
}
