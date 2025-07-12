package com.demo.Project.Manger.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.demo.Project.Manger.entity.Project;
import com.demo.Project.Manger.entity.TeamMember;
import com.demo.Project.Manger.entity.User;
import com.demo.Project.Manger.repository.ProjectRepository;
import com.demo.Project.Manger.repository.TeamMemberRepository;
import com.demo.Project.Manger.repository.UserRepository;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;

    // ✅ Method to fetch project by ID or throw error
    public Project findById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }
    
    public List<Project> getProjectsAssignedToUser(String email) {
    	User user = userRepository.findByEmail(email);
    	if (user == null) {
    	    throw new UsernameNotFoundException("User not found");
    	}

        List<TeamMember> memberships = teamMemberRepository.findByUser(user);

        return memberships.stream()
                .map(TeamMember::getProject)
                .collect(Collectors.toList());
    }
}