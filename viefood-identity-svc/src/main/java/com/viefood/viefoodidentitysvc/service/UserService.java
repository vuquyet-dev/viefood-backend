package com.viefood.viefoodidentitysvc.service;

import com.viefood.viefoodidentitysvc.dto.req.UserReq;
import com.viefood.viefoodidentitysvc.dto.res.UserRes;
import org.springframework.stereotype.Service;

public interface UserService {

    UserRes register(UserReq request);
}
