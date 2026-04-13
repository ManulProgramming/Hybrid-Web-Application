package com.example.manultube.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(columnNames = "usermail"))
@Getter @Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 89, unique = true)
    private String usermail;

    @Column(nullable = false, length = 161)
    private String userpass;
}
