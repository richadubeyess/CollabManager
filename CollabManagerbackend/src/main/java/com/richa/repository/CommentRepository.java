package com.richa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.richa.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByIssueId(Long issueId);
}

