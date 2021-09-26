package study.querydsl.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter
public class UsersEntity {

    @Id @GeneratedValue
    @Column(name = "userId")
    private Long id;

    private String userName;

    private int userAge;

    private String address;

}
