package com.hotelnova.user;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    void save(User user);

    void update(User user);

    Optional<User> findById(int id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    void updateActiveStatus(int id, boolean active);
}