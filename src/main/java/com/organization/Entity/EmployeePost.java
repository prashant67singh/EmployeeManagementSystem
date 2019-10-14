package com.organization.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/* This class is Created to Solve the purpose of post Request*/
public class EmployeePost {

    @JsonProperty("name")
    public String empName;
    @JsonProperty("id")
    public Integer empId;
    public Designation designation;
    public Integer managerId;
    public String jobTitle;

    public EmployeePost(String empName, Integer managerId, String jobTitle, Boolean replace) {
        this.empName = empName;
        this.managerId = managerId;
        this.jobTitle = jobTitle;
        this.replace = replace;
    }

    public Boolean replace = false;

    public EmployeePost() {

    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public Designation getDesignation() {
        return designation;
    }

    public void setDesignation(Designation designation) {
        this.designation = designation;
    }

    public Integer getManagerId() {
        return managerId;
    }

    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {  this.jobTitle = jobTitle;    }

    public Boolean getReplace() {
        return replace;
    }

    public void setReplace(Boolean replace) {
        this.replace = replace;
    }
}
