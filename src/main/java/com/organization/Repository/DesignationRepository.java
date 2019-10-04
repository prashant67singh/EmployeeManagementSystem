package com.organization.Repository;

import com.organization.Entity.Designation;
import com.organization.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesignationRepository extends CrudRepository<Designation, Integer> {


    public Designation findByJobTitle(String jobTitle); // fetching Designation Details for POST REST API CALL
}