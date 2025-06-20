package com.demo.Project.Manger.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.Project.Manger.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	User findByEmail(String email);
}
