package com.pfm.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public final class UserDetails {

  private Long id;
  private String username;
  private String firstName;
  private String lastName;
  private String token;
}
