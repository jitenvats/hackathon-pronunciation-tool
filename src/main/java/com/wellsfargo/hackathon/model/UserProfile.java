package com.wellsfargo.hackathon.model;

import lombok.Data;

@Data
public class UserProfile {

    private String id;
    private String status;
    private Profile profile;
}

@Data
class Profile {
    private String firstName;
    private String lastName;
    private String login;
    private String email;

}