package com.nal.pfms.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Data
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name="first_name", nullable = false)
    private String firstName;

    @Column(name="last_name")
    private String lastName;


    @Column(name="email", nullable = false, unique = true)
    private String email;


    @Column(length = 60, nullable = false)
    private String password;

    @Column(name="role")
    private String role;

    @Column(name="enabled")
    private boolean enabled = false;
}
