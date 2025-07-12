package com.demo.Project.Manger.controller;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.demo.Project.Manger.dto.TaskDTO;
import com.demo.Project.Manger.dto.TaskRequestDTO;
import com.demo.Project.Manger.entity.Project;
import com.demo.Project.Manger.entity.Task;
import com.demo.Project.Manger.entity.TeamMember;
import com.demo.Project.Manger.entity.User;
import com.demo.Project.Manger.enums.Role;
import com.demo.Project.Manger.repository.ProjectRepository;
import com.demo.Project.Manger.repository.TaskRepository;
import com.demo.Project.Manger.repository.TeamMemberRepository;
import com.demo.Project.Manger.repository.UserRepository;
import com.demo.Project.Manger.service.EmailService;
import com.demo.Project.Manger.service.TaskService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class TaskController {

    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private TeamMemberRepository teamMemberRepository;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TaskService taskService;

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @GetMapping("/projects/{projectId}/tasks")
    public List<TaskDTO> getTasksByProject(@PathVariable Long projectId) {
        return taskService.getTasksByProjectId(projectId);
    }
    
    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<String> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> statusUpdate) {
        String newStatus = statusUpdate.get("status");
        taskService.updateTaskStatus(taskId, newStatus);
        return ResponseEntity.ok("Status updated successfully");
    }
    	
    
    @GetMapping("/my-tasks")
    public ResponseEntity<?> getMyTasks(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        List<Task> tasks = taskRepository.findByAssignedTo(user);
        List<TaskDTO> dtoList = tasks.stream().map(TaskDTO::fromEntity).toList();
        return ResponseEntity.ok(dtoList);
    }
    
    @GetMapping("/projects/{projectId}/my-tasks")
    public ResponseEntity<List<TaskDTO>> getMyTasksInProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByEmail(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Task> tasks = taskRepository.findByProjectIdAndAssignedTo(projectId, user);
        List<TaskDTO> dtoList = tasks.stream().map(TaskDTO::fromEntity).toList();
        return ResponseEntity.ok(dtoList);
    }
    
 // PUT  /api/tasks/{id}/completed   – body: { "completed": true | false }
    @PutMapping("/tasks/{id}/completed")
    public ResponseEntity<TaskDTO> setCompletedFlag(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload,
            @AuthenticationPrincipal UserDetails me) {

        System.out.println("🔧 Received request to update task ID: " + id);
        boolean completedFlag = payload.getOrDefault("completed", true);
        System.out.println("👉 Completed flag from request: " + completedFlag);
        System.out.println("👤 Authenticated user: " + me.getUsername());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> {
                    System.out.println("❌ Task not found with ID: " + id);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Task not found");
                });

        if (!task.getAssignedTo().getEmail().equals(me.getUsername())) {
            System.out.println("⛔ Forbidden: Task not assigned to this user");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        task.setCompleted(completedFlag);
        task.setStatus(completedFlag ? "Done" : "To Do");
        taskRepository.save(task);

        System.out.println("✅ Task updated successfully. New status: " + task.getStatus());
        return ResponseEntity.ok(TaskDTO.fromEntity(task));
    }


    
    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@RequestBody TaskRequestDTO dto) {
        // Step 1: Check if user exists
        User assignee = userRepository.findByEmail(dto.getAssigneeEmail());

        if (assignee == null) {
            String rawPassword = generateRandomPassword(10);
            assignee = new User();
            assignee.setName(dto.getAssigneeName());
            assignee.setEmail(dto.getAssigneeEmail());
            assignee.setPassword(passwordEncoder.encode(rawPassword));
            assignee.setRole(Role.TEAM_MEMBER);
            userRepository.save(assignee);

            // Send email
            String subject = "You've been assigned a new task: " + dto.getTitle();
            String loginLink = "http://localhost:5173/";
            String msg = String.format("""
                Hello %s,

                You have been assigned a new task: %s

                Credentials:
                Email: %s
                Password: %s

                Login here: %s

                Regards,
                PM-Flow Team
            """, assignee.getName(), dto.getTitle(), dto.getAssigneeEmail(), rawPassword, loginLink);

            emailService.sendEmail(assignee.getEmail(), subject, msg);
        }

        // Step 2: Fetch project
        Project project = projectRepository.findById(dto.getProjectId()).orElse(null);
        if (project == null) {
            return ResponseEntity.badRequest().body("Project not found");
        }

        // Step 3: Add to team if not already added
        String assigneeEmail = assignee.getEmail();
        boolean alreadyExists = teamMemberRepository.findByProject(project)
            .stream()
            .anyMatch(tm -> tm.getUser().getEmail().equals(assigneeEmail));

        if (!alreadyExists) {
            TeamMember newMember = new TeamMember(project, assignee);
            teamMemberRepository.save(newMember);
        }

        // Step 4: Create and save task
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : "To Do");
        task.setDueDate(dto.getDueDate());
        task.setCompleted(false);
        task.setAssignedTo(assignee);
        task.setProject(project);

        Task saved = taskRepository.save(task);

        // ✅ Convert to DTO to avoid infinite recursion
        TaskDTO responseDto = TaskDTO.fromEntity(saved);
        return ResponseEntity.ok(TaskDTO.fromEntity(saved));
    }

}
