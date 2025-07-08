package com.demo.Project.Manger.dto;



import com.demo.Project.Manger.entity.Project;

public class ProjectDTO {

    private Long id;
    private String name;
    private String description;
    private String status;
    private String startDate;
    private String endDate;
    private String managerName;
    private String managerEmail;

    // ✅ Constructor that maps from Project entity
    public ProjectDTO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.status = project.getStatus();
        this.startDate = project.getStartDate() != null ? project.getStartDate().toString() : null;
        this.endDate = project.getEndDate() != null ? project.getEndDate().toString() : null;
        this.managerName = project.getManager() != null ? project.getManager().getName() : null;
        this.managerEmail = project.getManager() != null ? project.getManager().getEmail() : null;
    }

    // ✅ Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }
}
