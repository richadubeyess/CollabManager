package com.richa.DTO;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import com.richa.model.Project;
import com.richa.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueDTO {

    private Long id;
    private String title;
    private String description;
    private String status;
    private Long projectID;
    private String priority;
    private LocalDate dueDate;
    private List<String> tags = new ArrayList<>();
    private Project project;



    private User assignee;


}
