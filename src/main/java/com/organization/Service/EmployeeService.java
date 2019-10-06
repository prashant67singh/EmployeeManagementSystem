package com.organization.Service;

import com.organization.Entity.Designation;
import com.organization.Entity.Employee;
import com.organization.Entity.EmployeePost;
import com.organization.Repository.DesignationRepository;
import com.organization.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeService {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    DesignationRepository designationRepository;

    /*Method to get all Employees in database*/
    public List<Employee> getAllEmployee() {
        // Finding details of all Employee sorted by LevelId, EmployeeName
        List<Employee> employeeList=employeeRepository.findAllByOrderByDesignation_LevelIdAscEmpNameAsc();
        return  employeeList;
    }

    /*Method to get Details of employee  provided Employee Id. This Method returns details of Employee, His Manager details
    * ,his colleague details and also the details of Employee reporting to that particular Employee  */
    public Map<String,List<Employee>> getEmployee(int id)
    {
        Map<String,List<Employee>> details = new LinkedHashMap<>();
        Optional<Employee> employee = employeeRepository.findById(id); // Getting Employee Details
        List<Employee> employeeDetails= new ArrayList<>();
        employeeDetails.add(employee.get());
        details.put("Employee:", employeeDetails);
        if(employee.get().getManagerId()!=null)  // Checking for Valid ManagerId
        {
            Optional<Employee> managerDetails = employeeRepository.findById(employee.get().getManagerId()); // Getting Manager Details
            List<Employee> managerValue = new ArrayList<>();
            managerValue.add(managerDetails.get());
            details.put("Manager:", managerValue);
        }
        List<Employee> employeeList = employeeRepository.findAllByOrderByDesignation_LevelIdAscEmpNameAsc();
        List<Employee> colleague = new ArrayList<>(); // Creating List to get Colleague details
        for(int i=0;i<employeeList.size();i++)
        {
            // Condition to get Colleague details
            if(employeeList.get(i).getManagerId()== employee.get().getManagerId() && employee.get().getEmpId() != employeeList.get(i).getEmpId())
            {
                colleague.add(employeeList.get(i));
            }
        }
        if(colleague.size()!=0)
        {
            details.put("Colleague ", colleague);
        }
        List<Employee> subordinate= new ArrayList<>(); // Creation of List to get Subordinate Details
        for(int i=0;i<employeeList.size();i++)
        {
            if(employeeList.get(i).getManagerId()== employee.get().getEmpId()) // Condition for getting Subordinate details
            {
                subordinate.add(employeeList.get(i));
            }
        }
        if(subordinate.size()!=0)
        {
            details.put("Reporting To:", subordinate);
        }
        return details;
    }

/*Method to delete an employee from table provided with employee id */
    public ResponseEntity deleteEmployeeById(int id)
    {
        Optional<Employee> employeeDetails= employeeRepository.findById(id);

        if(employeeDetails.isPresent())
        {
            List<Employee> employeeList=employeeRepository.findAll();
            int c=0;
            for(int i=0;i<employeeList.size();i++)
            {
                if (employeeList.get(i).getManagerId() == employeeDetails.get().getEmpId()) // Counting Number of Subordinates
                 {
                    c=c+1;
                }
            }
            if(employeeDetails.get().getEmpId()==1 & c>=1) // More then One Subordinate exits so can't be deleted
            {
                return new ResponseEntity("Cannot Be Deleted",HttpStatus.BAD_REQUEST);
            }
            else if(employeeDetails.get().getEmpId() == 1 && c ==0)  //Director with no Subordinate so it can be deleted
            {
                employeeRepository.deleteById(id);
                return new ResponseEntity("Successfully Deleted",HttpStatus.OK);
            }
            else if(employeeDetails.get().getEmpId()!=1 && c==0) // Employee with no subordinates can be deleted
            {
                employeeRepository.deleteById(id);
                return new ResponseEntity("Successfully Deleted",HttpStatus.OK);
            }
            else  //Employee with multiple Subordinates can be deleted, ManagerId of Subordinate changes
                {
                int j=employeeDetails.get().getManagerId();
                for(int i=0;i<employeeList.size();i++)
                {
                    if(employeeList.get(i).getManagerId() == employeeDetails.get().getEmpId()) // changing ManagerId of Subordinate
                    {
                        employeeList.get(i).setManagerId(j);
                        employeeRepository.save(employeeList.get(i));
                    }
                }
                employeeRepository.deleteById(id);
                return new ResponseEntity("Parent Changed and Details Successfully Deleted",HttpStatus.OK);
            }
        }
        else {
            return new ResponseEntity("Id Not Found", HttpStatus.BAD_REQUEST);
        }
    }

/* Method to add new employee */
    public ResponseEntity addEmployee(EmployeePost employee) {
        Employee emp = new Employee();                                 // Creating New Employee Object
        Designation designation = designationRepository.findByJobTitle(employee.getJobTitle()); // Fetching Details of Designation From JobTile
        emp.setEmpId(employee.getEmpId());
        emp.setManagerId(employee.getManagerId());
        emp.setEmpName(employee.getEmpName());
        emp.setDesignation(designation);
        if(employee.getManagerId()==null){
            return new ResponseEntity("Manager Id Cannot be Null",HttpStatus.FORBIDDEN);
        }
        int newEmployeeLevelId = emp.getDesignation().getLevelId();   //Finding LevelId of New Employee to be Inserted
        List<Employee> allEmployee = employeeRepository.findAll();
        Employee parent = new Employee();
        for (int i = 0; i < allEmployee.size(); i++) {
            if (allEmployee.get(i).getEmpId() == employee.getManagerId()) {
                parent = allEmployee.get(i);
            }
        }
        int parentLevelId = parent.getDesignation().getLevelId();  // Finding the LevelId of Manager Of New Employee
        if (parentLevelId < newEmployeeLevelId) {
            employeeRepository.save(emp);                         //Saving new Employee details in Employee Repository
            return new ResponseEntity("New Employee Added", HttpStatus.OK);
        } else if (newEmployeeLevelId == 1)
        {
            return new ResponseEntity("Director Already Exist",HttpStatus.BAD_REQUEST);
        }
        else {
            return new ResponseEntity("New Employee Cannot Be Added", HttpStatus.FORBIDDEN);
        }
    }

    /* Method to Update an Employee Details*/
    public ResponseEntity updateEmployeeDetails(EmployeePost employee)
    {
        Employee employeeDetails = new Employee();
        List<Employee> employeeList= employeeRepository.findAll();
        for(int i=0;i<employeeList.size();i++)
        {
         if(employeeList.get(i).getEmpId()== employee.getEmpId()){
             employeeDetails=employeeList.get(i);                      // Getting Employee Details after getting EmpId Passed in json body
         }
        }
        if(employee.getReplace()==false) // Updating the Details of employee with given Changes
        {
            if(employee.getEmpName()!=null) {
                employeeDetails.setEmpName(employee.getEmpName());
            }
            else
            {
                return new ResponseEntity("Employee Name Cannot Be Empty",HttpStatus.BAD_REQUEST);
            }
            if(employee.getJobTitle()!=null)
            {
                employeeDetails.setJobTitle(employee.getJobTitle());
            }
            else {
                return new ResponseEntity("Job Title Cannot Be Empty",HttpStatus.BAD_REQUEST);
            }
            if (employee.getManagerId()!= null)
            {
                employeeDetails.setManagerId(employee.getManagerId());
            }
            else
            {
                return new ResponseEntity("Manager Id Cannot Be Empty",HttpStatus.BAD_REQUEST);
            }
            employeeRepository.save(employeeDetails);
            return new ResponseEntity("Employee Details Changed without Replacement",HttpStatus.OK);
        }
        else                // Replacing the Old Employee with New Employee
            {
              Employee newEmployee= new Employee();
              Designation designation = designationRepository.findByJobTitle(employee.getJobTitle());
              int oldLevelId=employeeDetails.getDesignation().getLevelId();
              int newLevelId=designation.getLevelId();
              if(newLevelId == oldLevelId)
              {
                  newEmployee.setEmpId(employeeList.size()+1);
                  newEmployee.setEmpName(employee.getEmpName());
                  newEmployee.setDesignation(designation);
                  newEmployee.setManagerId(employeeDetails.getManagerId());
                  employeeRepository.save(newEmployee);
               // Conditions for changing the ManagerId of Subordinates
                  List<Employee> subordinateList =new ArrayList<>();
                  for(int i=0; i<employeeList.size();i++)
                  {
                      if(employeeList.get(i).getManagerId() == employeeDetails.getEmpId()){
                          subordinateList.add(employeeList.get(i));
                      }
                  }
                  for(int i=0;i<subordinateList.size();i++)
                  {
                      subordinateList.get(i).setManagerId(newEmployee.getEmpId());
                      employeeRepository.save(subordinateList.get(i));
                  }
                  employeeRepository.deleteById(employeeDetails.getEmpId()); // Deleting the old Employee
                  return new ResponseEntity("New Employee Added With Replacement",HttpStatus.OK);
              }
              else
              {
                  return new ResponseEntity("Higher Level Id, So Employee Cannot Be Added",HttpStatus.BAD_REQUEST);
              }

        }
    }
}

