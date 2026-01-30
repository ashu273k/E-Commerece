package com.ashu.E_Commerece.dto.user;

import com.ashu.E_Commerece.model.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user profile update request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    private String firstName;
    private String lastName;
    private String phone;
    private Address address;
}
