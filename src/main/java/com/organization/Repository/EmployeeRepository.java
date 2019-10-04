package com.organization.Repository;

import com.organization.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    public List<Employee> findAllByOrderByDesignation_LevelIdAscEmpNameAsc(); // Declaration of Abstract method for finding details of all employee

}
