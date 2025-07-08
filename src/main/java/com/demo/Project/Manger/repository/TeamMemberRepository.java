package com.demo.Project.Manger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.Project.Manger.entity.Project;
import com.demo.Project.Manger.entity.TeamMember;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByProject(Project project);
}
