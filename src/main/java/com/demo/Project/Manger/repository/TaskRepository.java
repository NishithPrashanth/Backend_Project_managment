package com.demo.Project.Manger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.Project.Manger.entity.Project;
import com.demo.Project.Manger.entity.Task;
import com.demo.Project.Manger.entity.User;

public interface TaskRepository extends JpaRepository<Task, Long>{
	
	 List<Task> findByProject(Project project);

	    // OR (if you only have the projectId and don't want to fetch the full Project object)
	    List<Task> findByProjectId(Long projectId);
	    List<Task> findByAssignedTo(User user);

}
