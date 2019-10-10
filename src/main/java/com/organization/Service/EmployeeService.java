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
    public ResponseEntity getAllEmployee()
    {
        List<Employee> employeeList=employeeRepository.findAllByOrderByDesignation_LevelIdAscEmpNameAsc(); // Finding details of all Employee sorted by LevelId, EmployeeName
        if (employeeList.size() > 0){
            return new ResponseEntity(employeeList,HttpStatus.OK);  // Returning Non Empty List
        }
        else
        {
            return new ResponseEntity("No Employee Found",HttpStatus.NOT_FOUND);  // Returning Error Message For Empty List
        }

    }

    /*Method to get Details of employee  provided Employee Id. This Method returns details of Employee, His Manager details
    * ,his colleague details and also the details of Employee reporting to that particular Employee  */
    public ResponseEntity getEmployee(int  id) {
        if (id > 0)  // Checking if Id is positive or not
        {
            Map<String, List<Employee>> details = new LinkedHashMap<>();
            Optional<Employee> employee = employeeRepository.findById(id); // Getting Employee Details
            if(employee.isPresent())
            {
                List<Employee> employeeDetails = new ArrayList<>();
                employeeDetails.add(employee.get());
                details.put("Employee:", employeeDetails);
                if (employee.get().getManagerId() != null)  // Checking for Valid ManagerId
                {
                    Optional<Employee> managerDetails = employeeRepository.findById(employee.get().getManagerId()); // Getting Manager Details
                    List<Employee> managerValue = new ArrayList<>();
                    managerValue.add(managerDetails.get());
                    details.put("Manager:", managerValue);
                }
                List<Employee> employeeList = employeeRepository.findAllByOrderByDesignation_LevelIdAscEmpNameAsc();
                List<Employee> colleague = new ArrayList<>(); // Creating List to get Colleague details
                for (int i = 0; i < employeeList.size(); i++) {
                    // Condition to get Colleague details
                    if (employeeList.get(i).getManagerId() == employee.get().getManagerId() && employee.get().getEmpId() != employeeList.get(i).getEmpId()) {
                        colleague.add(employeeList.get(i));
                    }
                }
                if (colleague.size() != 0) //Checking the Number of Colleagues
                {
                    details.put("Colleague ", colleague);
                }
                List<Employee> subordinate = new ArrayList<>(); // Creation of List to get Subordinate Details
                for (int i = 0; i < employeeList.size(); i++) {
                    if (employeeList.get(i).getManagerId() == employee.get().getEmpId()) // Condition for getting Subordinate details
                    {
                        subordinate.add(employeeList.get(i));
                    }
                }
                if (subordinate.size() != 0)  // Checking the number of Subordinates
                {
                    details.put("Reporting To:", subordinate);
                }
                return new ResponseEntity(details, HttpStatus.OK); // Returning Employee Details for Valid Employee Id
            }
            else
            {
                return new ResponseEntity("Given Employee Id Is Not Present",HttpStatus.BAD_REQUEST);
            }

        }
        else
        {
            return new ResponseEntity("Invalid Employee Id",HttpStatus.BAD_REQUEST); // Error Message With Http Response Msg for Invalid Employee Id
        }
    }

/*Method to delete an employee from table provided with employee id */
    public ResponseEntity deleteEmployeeById(int id)
    {
        if(id>0) {
            Optional<Employee> employeeDetails = employeeRepository.findById(id);
            if (employeeDetails.isPresent()) {
                List<Employee> employeeList = employeeRepository.findAll();
                int c = 0;
                for (int i = 0; i < employeeList.size(); i++) {
                    if (employeeList.get(i).getManagerId() == employeeDetails.get().getEmpId()) // Counting Number of Subordinates
                    {
                        c = c + 1;
                    }
                }
                if (employeeDetails.get().getEmpId() == 1 & c >= 1) // More then One Subordinate exits so can't be deleted
                {
                    return new ResponseEntity("Director With Multiple Subordinates Cannot Be Deleted", HttpStatus.BAD_REQUEST);
                } else if (employeeDetails.get().getEmpId() == 1 && c == 0)  //Director with no Subordinate so it can be deleted
                {
                    employeeRepository.deleteById(id);
                    return new ResponseEntity("Director With No Subordinates Successfully Deleted", HttpStatus.OK);
                } else if (employeeDetails.get().getEmpId() != 1 && c == 0) // Employee with no subordinates can be deleted
                {
                    employeeRepository.deleteById(id);
                    return new ResponseEntity(" Employee With No Subordinates Successfully Deleted", HttpStatus.OK);
                } else  //Employee with multiple Subordinates can be deleted, ManagerId of Subordinate changes
                {
                    int j = employeeDetails.get().getManagerId();
                    for (int i = 0; i < employeeList.size(); i++) {
                        if (employeeList.get(i).getManagerId() == employeeDetails.get().getEmpId()) // changing ManagerId of Subordinate
                        {
                            employeeList.get(i).setManagerId(j);
                            employeeRepository.save(employeeList.get(i));
                        }
                    }
                    employeeRepository.deleteById(id);
                    return new ResponseEntity("Employee Is Successfully Deleted And Parent Details of His Subordinates Successfully Changed", HttpStatus.OK);
                }
            } else {
                return new ResponseEntity("Id Not Found", HttpStatus.BAD_REQUEST);
            }
        }
        else
        {
            return new ResponseEntity("Invalid Employee Id",HttpStatus.BAD_REQUEST);
        }
    }

/* Method to add new employee */
    public ResponseEntity addEmployee(EmployeePost employee) {
        Employee emp = new Employee();                                 // Creating New Employee Object
        if (employee.getJobTitle() == null){                           // Checking If Employee Job Title is Passed in Request Body or Not
            return new ResponseEntity("JobTitle Cannot be Null",HttpStatus.BAD_REQUEST);
        }
        if(employee.getEmpName() == null){                             // Checking if Employee Name is passed in Request Body or Not
            return new ResponseEntity("New Employee Name Cannot be Null",HttpStatus.BAD_REQUEST);
        }
        Designation designation = designationRepository.findByJobTitle(employee.getJobTitle()); // Fetching Details of Designation From JobTile

        if (designation == null)                                                               // checking if JobTitle exist in Database or not
        {
            return new ResponseEntity("No Such Designation Exit For The Given JobTitle", HttpStatus.BAD_REQUEST);
        }

        emp.setEmpId(employee.getEmpId());       // Setting Employee Id
        emp.setEmpName(employee.getEmpName());  // Setting Employee Name
        emp.setDesignation(designation);       // Setting Employee Designation Details
        if(employeeRepository.findAll().size()!=0)
        {
            if (employee.getManagerId() == null)                       // if There is director then new Employee Manager Id cannot be Null
            {
                return new ResponseEntity("Manager Id Cannot Be Null", HttpStatus.BAD_REQUEST);
            }
            emp.setManagerId(employee.getManagerId());
            int newEmployeeLevelId = emp.getDesignation().getLevelId();   //Finding LevelId of New Employee to be Inserted
            List<Employee> allEmployee = employeeRepository.findAll();
            Employee parent = new Employee();
            for (int i = 0; i < allEmployee.size(); i++)
            {
                if (allEmployee.get(i).getEmpId() == employee.getManagerId())
                {
                    parent = allEmployee.get(i);
                }
            }
            int parentLevelId = parent.getDesignation().getLevelId();  // Finding the LevelId of Manager Of New Employee
            if (parentLevelId < newEmployeeLevelId)
            {
                employeeRepository.save(emp);                         //Saving new Employee details in Employee Repository
                return new ResponseEntity("New Employee Added", HttpStatus.OK);
            }
            else if (newEmployeeLevelId == 1)
            {
                return new ResponseEntity("Director Already Exist", HttpStatus.BAD_REQUEST);
            }
            else
                {
                return new ResponseEntity("New Employee Cannot Be Added Due To Same Designation Post", HttpStatus.FORBIDDEN);
            }
        }
        else
        {
                if(employee.getManagerId()==null)
                {
                    emp.setManagerId(null);                       // Manager Id In Case Of Very First employee can be null
                    employeeRepository.save(emp);                 // Saving The Employee Details in Repository
                    return new ResponseEntity("Very First Employee In EMS is Added", HttpStatus.OK);

                }
                else
                {
                    return new ResponseEntity("Very First Employee Cannot Have Manager Id",HttpStatus.BAD_REQUEST);
                }
        }
    }

    /* Method to Update an Employee Details*/
    public ResponseEntity updateEmployeeDetails(EmployeePost employee)
    {
        Employee employeeDetails = new Employee();
        List<Employee> employeeList= employeeRepository.findAll();
        if(employee.getEmpId() == null)
        {
            return  new ResponseEntity("Employee Id Cannot Be null",HttpStatus.BAD_REQUEST);
        }
        if(employee.getEmpId()> 0) {
            for (int i = 0; i < employeeList.size(); i++) {
                if (employeeList.get(i).getEmpId() == employee.getEmpId()) {
                    employeeDetails = employeeList.get(i);                      // Getting Employee Details after getting EmpId Passed in json body
                }
            }
            if(employeeDetails.getEmpId() == null)
            {
                return new ResponseEntity("Employee Details Not Present For Given Employee Id",HttpStatus.BAD_REQUEST);
            }
        }
        else
        {
            return new ResponseEntity("Invalid Employee Id Is Passed",HttpStatus.BAD_REQUEST);
        }
        if(employee.getReplace() == false) // Updating the Details of employee with given Changes
        {
            if(employee.getEmpName()!=null) {
                employeeDetails.setEmpName(employee.getEmpName());
            }
            else
            {
                employeeDetails.setEmpName(employeeDetails.getEmpName());
                employeeRepository.save(employeeDetails);
            }
            if(employee.getJobTitle()!=null)
            {
                Designation designation = designationRepository.findByJobTitle(employee.getJobTitle()); // fetching designation Details From Passed new job Title
                if(designation==null){                                                                   // if Job Title Exit or not
                    return new ResponseEntity("Such Designation Do not Exit",HttpStatus.BAD_REQUEST);
                }
                int newLevelId = designation.getLevelId();
                Optional<Employee> manager = employeeRepository.findById(employeeDetails.getManagerId());             // Getting Manager Details
                int parentLevelId = manager.get().getDesignation().getLevelId();
                int currentLevelId = employeeDetails.getDesignation().getLevelId();
                if((newLevelId>parentLevelId)&&(newLevelId<=currentLevelId))                                         //Comparing the New level Id with Employee Manager Id and his current level id
                {                                                                                                   // Assumption Employee Cannot given Lower designation
                    employeeDetails.setDesignation(designationRepository.findByJobTitle(employee.getJobTitle()));  // Setting The New Designation for Employee
                    employeeDetails.setJobTitle(employee.getJobTitle());
                    employeeRepository.save(employeeDetails);

                }
                else
                {
                    return new ResponseEntity("Level Id Must Not Greater then His Manager Level Id And Lower Then His Current Level Id",HttpStatus.BAD_REQUEST);
                }
            }
            else {
                employeeDetails.setJobTitle(employeeDetails.getJobTitle());
                employeeRepository.save(employeeDetails);
            }
            if (employee.getManagerId()!= null)
            {
                Optional<Employee> managerDetails = employeeRepository.findById(employee.getManagerId());
                if (!managerDetails.isPresent())                               // Checking for Invalid Manager Id
                {
                    return new ResponseEntity("Not a valid ManagerId",HttpStatus.BAD_REQUEST);
                }
                Designation designation = designationRepository.findByJobTitle(managerDetails.get().getJobTitle());
                int managerLevelId = designation.getLevelId();
                int employeeLevelId = employeeDetails.getDesignation().getLevelId();
                if (managerLevelId < employeeLevelId)
                {                                  // Matching Designation Details Of New Manager and Employee
                    employeeDetails.setManagerId(employee.getManagerId());
                    employeeRepository.save(employeeDetails);
                }
                else
                    {
                    return new ResponseEntity("Employee Cannot Have Same or Lower Designation As His/Her New manager",HttpStatus.BAD_REQUEST);
                    }
            }
            else
                {
                employeeDetails.setManagerId(employeeDetails.getManagerId());
                employeeRepository.save(employeeDetails);
               }
            return new ResponseEntity("Employee Details Changed without Replacement",HttpStatus.OK);
        }
        else if (employee.getReplace() == true)                                                                         // Replacing the Old Employee with New Employee
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
        else
        {
            return  new ResponseEntity(" Bad Request "+ employee.getReplace(),HttpStatus.BAD_REQUEST);
        }
    }
}

