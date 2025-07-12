package com.demo.Project.Manger.controller;

import com.demo.Project.Manger.dto.ProjectDTO;
import com.demo.Project.Manger.dto.ProjectDetailDTO;
import com.demo.Project.Manger.entity.Project;
import com.demo.Project.Manger.entity.TeamMember;
import com.demo.Project.Manger.entity.User;
import com.demo.Project.Manger.enums.Role;
import com.demo.Project.Manger.repository.ProjectRepository;
import com.demo.Project.Manger.repository.TeamMemberRepository;
import com.demo.Project.Manger.repository.UserRepository;
import com.demo.Project.Manger.service.EmailService;
import com.demo.Project.Manger.service.ProjectService;
import com.demo.Project.Manger.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:5173/")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    
    
    @Autowired
    private ProjectService projectService;

    @Autowired
    private JwtUtil jwtService;

    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody Project projectRequest, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // ✅ Find or create manager user
        User managerUser = userRepository.findByEmail(projectRequest.getManager().getEmail());

        if (managerUser == null) {
            // 🔐 Generate random password
            String rawPassword = generateRandomPassword(10);
            String encodedPassword = passwordEncoder.encode(rawPassword);

            // 🆕 Create new manager
            managerUser = new User();
            managerUser.setName(projectRequest.getManager().getName());
            managerUser.setEmail(projectRequest.getManager().getEmail());
            managerUser.setPassword(encodedPassword);
            managerUser.setRole(Role.PROJECT_MANAGER);

            userRepository.save(managerUser);

            // 📧 Send login credentials
            String loginUrl = "http://localhost:5173/";
            String subject = "New Project Assignment: " + projectRequest.getName();
            String message = String.format(
                "Hello %s,\n\nYou have been assigned a new project: %s.\n\nYour login credentials:\nEmail: %s\nPassword: %s\n\nLogin here: %s\n\nPlease change your password after logging in.",
                managerUser.getName(), projectRequest.getName(), managerUser.getEmail(), rawPassword, loginUrl
            );

            emailService.sendEmail(managerUser.getEmail(), subject, message);
        }

        // ✅ Associate the manager with the project
        Project projectToSave = new Project();
        projectToSave.setName(projectRequest.getName());
        projectToSave.setDescription(projectRequest.getDescription());
        projectToSave.setStartDate(projectRequest.getStartDate());
        projectToSave.setEndDate(projectRequest.getEndDate());
        projectToSave.setStatus(projectRequest.getStatus());
        projectToSave.setManager(managerUser); // Associate the foreign key relation

        // ✅ Save the project
        Project savedProject = projectRepository.save(projectToSave);

        return ResponseEntity.ok(savedProject);
    }
  
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<Project> projects = projectRepository.findAll();

        List<ProjectDTO> dtoList = projects.stream()
            .map(ProjectDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    
    
    
    @GetMapping("/my-projects")
    public ResponseEntity<List<ProjectDTO>> getProjectsForLoggedInUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        String token = authHeader.substring(7); // Remove "Bearer "
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(404).build(); // User not found
        }

        List<Project> myProjects = projectRepository.findByManager(user);

        // ✅ Convert to DTOs
        List<ProjectDTO> dtoList = myProjects.stream()
            .map(ProjectDTO::new)
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getProjectById(@PathVariable Long id) {
        Project project = projectRepository.findById(id).orElse(null);

        if (project == null) {
            return ResponseEntity.notFound().build();
        }

        ProjectDetailDTO dto = new ProjectDetailDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setStatus(project.getStatus());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setManagerName(project.getManager().getName());
        dto.setManagerEmail(project.getManager().getEmail());

        // ✅ Fetch team members from the table
        List<String> memberNames = teamMemberRepository.findByProject(project)
            .stream()
            .map(tm -> tm.getUser().getName())
            .collect(Collectors.toList());

        dto.setTeamMembers(memberNames);

        return ResponseEntity.ok(dto);
    }
    
 
    @GetMapping("/assigned-projects")
    public ResponseEntity<List<ProjectDTO>> getProjectsForTeamMember(Authentication authentication) {
       
    	User user = userRepository.findByEmail(authentication.getName());
    	if (user == null) {
    	    throw new UsernameNotFoundException("User not found");
    	}

     
        List<TeamMember> memberships = teamMemberRepository.findByUser(user);

        // 3. Convert each Project entity to a ProjectDTO to avoid circular nesting
        List<ProjectDTO> projectDTOs = memberships.stream()
            .map(tm -> new ProjectDTO(tm.getProject()))
            .toList();

        // 4. Return the list of safe, flat DTOs
        return ResponseEntity.ok(projectDTOs);
    }

}
