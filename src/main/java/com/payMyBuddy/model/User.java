package com.payMyBuddy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 45)
    private String pseudo;

    @Column(nullable = false, precision = 18, scale = 2)
    private double amount;

    @JoinTable(name="connections", joinColumns = {
            @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)
    }, inverseJoinColumns = {
            @JoinColumn(name = "target_id", referencedColumnName = "id", nullable = false)
    })
    @ManyToMany(fetch = FetchType.EAGER)
    private List<User> listFriend;


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", pseudo='" + pseudo + '\'' +
                ", sold=" + amount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Double.compare(user.amount, amount) == 0 &&
                id.equals(user.id) &&
                email.equals(user.email) &&
                password.equals(user.password) &&
                pseudo.equals(user.pseudo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, password, pseudo, amount);
    }
}
