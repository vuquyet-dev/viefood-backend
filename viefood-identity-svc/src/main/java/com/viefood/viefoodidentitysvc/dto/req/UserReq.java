package com.viefood.viefoodidentitysvc.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserReq {

    @NotBlank
    @Size(min = 8, max = 50)
    private String username;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;
}
