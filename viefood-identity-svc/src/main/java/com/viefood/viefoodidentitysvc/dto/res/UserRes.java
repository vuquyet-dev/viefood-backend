package com.viefood.viefoodidentitysvc.dto.res;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.viefood.viefoodidentitysvc.enums.UserRole;
import com.viefood.viefoodidentitysvc.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "username", "role", "status"})
public class UserRes {
    private Long id;
    private String username;
    private UserRole role;
    private UserStatus status;
}
