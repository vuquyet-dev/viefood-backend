package com.viefood.viefoodidentitysvc.controller;

import com.viefood.base.common.BaseResponse;
import com.viefood.viefoodidentitysvc.dto.req.UserReq;
import com.viefood.viefoodidentitysvc.dto.res.UserRes;
import com.viefood.viefoodidentitysvc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<BaseResponse<UserRes>> registerUser(@RequestBody UserReq request)
    {
        UserRes userRes = userService.register(request);
        return ResponseEntity.ok(BaseResponse.success(userRes));
    }
}
