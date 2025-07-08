package com.demo.Project.Manger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.Project.Manger.entity.Project;
import com.demo.Project.Manger.entity.User;

public interface ProjectRepository extends JpaRepository<Project, Long> {
	List<Project> findByManager(User manager);

}
