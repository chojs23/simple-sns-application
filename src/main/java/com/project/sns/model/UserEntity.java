package com.project.sns.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table
public class UserEntity {

    @Id
    private Integer id;

    @Column(name = "userName")
    private String userName;
    @Column(name = "password")
    private String password;
}
