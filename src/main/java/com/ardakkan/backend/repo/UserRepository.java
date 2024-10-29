package com.ardakkan.backend.repo;

import com.ardakkan.backend.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Özel bir sorgu: Kullanıcıyı email ile bulmak
	Optional<User> findByEmail(String email);

}
