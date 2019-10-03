package com.organization.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.lang.Nullable;

import javax.persistence.*;

@Entity
@Table
public class Employee {

    @Id
    private Integer empId;
    private String empName;
    @OneToOne
    @JoinColumn
    Designation designation;
    @Nullable
    private Integer managerId;


    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
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

    public void setManagerId(@Nullable Integer managerId) {
        this.managerId = managerId;
    }
}
