package com.organization.Service;

import com.organization.Entity.Designation;
import com.organization.Entity.Employee;
import com.organization.Entity.EmployeePost;
import com.organization.Repository.DesignationRepository;
import com.organization.Repository.EmployeeRepository;
import com.sun.xml.internal.fastinfoset.algorithm.BuiltInEncodingAlgorithm;
import javafx.concurrent.Worker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.apache.commons.lang3.text.WordUtils;


import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmployeeService {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    DesignationRepository designationRepository;

    /*-----------------------Method to get all Employees in database-------------------------------*/
    public ResponseEntity getAllEmployee()
    {
        List<Employee> employeeList=employeeRepository.findAllByOrderByDesignation_LevelIdAscEmpNameAsc(); // Finding details of all Employee sorted by LevelId, EmployeeName
        if (employeeList.size() > 0)
        {
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
            Map<String, Object> details = new LinkedHashMap<>();
            Optional<Employee> employeeTemp = employeeRepository.findById(id); // Getting Employee Details
            if(employeeTemp.isPresent())
            {
                Employee employee = employeeTemp.get();
                details.put("employee", employee);
                if (!(employee.getManagerId() == -1 || employee.getManagerId() == null))  // Checking for Valid ManagerId
                {
                    Optional<Employee> managerDetails = employeeRepository.findById(employee.getManagerId()); // Getting Manager Details
                    Employee managerValue = managerDetails.get();
                    details.put("manager", managerValue);
                }
                List<Employee> employeeList = employeeRepository.findAllByOrderByDesignation_LevelIdAscEmpNameAsc();
                List<Employee> colleague = new ArrayList<>(); // Creating List to get Colleague details
                for (int i = 0; i < employeeList.size(); i++)
                {
                    // Condition to get Colleague details
                    if (employeeList.get(i).getManagerId() == employee.getManagerId() && employee.getEmpId() != employeeList.get(i).getEmpId())
                    {
                        colleague.add(employeeList.get(i));
                    }
                }
                if (colleague.size() != 0) //Checking the Number of Colleagues
                {
                    details.put("colleagues", colleague);
                }
                List<Employee> subordinate = new ArrayList<>(); // Creation of List to get Subordinate Details
                for (int i = 0; i < employeeList.size(); i++)
                {
                    if (employeeList.get(i).getManagerId() == employee.getEmpId()) // Condition for getting Subordinate details
                    {
                        subordinate.add(employeeList.get(i));
                    }
                }
                if (subordinate.size() != 0)  // Checking the number of Subordinates
                {
                    details.put("subordinates", subordinate);
                }
                return new ResponseEntity(details, HttpStatus.OK); // Returning Employee Details for Valid Employee Id
            }
            else
            {
                return new ResponseEntity("Given Employee Id Is Not Present",HttpStatus.NOT_FOUND);
            }

        }
        else
        {
            return new ResponseEntity("Invalid Employee Id",HttpStatus.BAD_REQUEST); // Error Message With Http Response Msg for Invalid Employee Id
        }
    }

/*--------------------------Method to delete an employee from table provided with employee id -------------*/
    public ResponseEntity deleteEmployeeById(int id)
    {
        if(id>0) // Checking For Correct Id Type
        {
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
                    return new ResponseEntity("Employee Is Successfully Deleted And Parent Details of His Subordinates Successfully Changed", HttpStatus.NO_CONTENT);
                }
            } else {
                return new ResponseEntity("Id Not Found", HttpStatus.NOT_FOUND);
            }
        }
        else
        {
            return new ResponseEntity("Invalid Employee Id",HttpStatus.BAD_REQUEST);
        }
    }

/* ------------------------Method to add new employee (POST) ----------------------------------*/
    public ResponseEntity addEmployee(EmployeePost employee) {
        Employee emp = new Employee();                                                            // Creating New Employee Object
        if (employee.getJobTitle() == null)                    // Checking If Employee Job Title is Passed in Request Body or Not
        {
            return new ResponseEntity("JobTitle Cannot be Null",HttpStatus.BAD_REQUEST);
        }
        if(employee.getEmpName() == null)                     // Checking if Employee Name is passed in Request Body or Not
        {
            return new ResponseEntity("New Employee Name Cannot be Null",HttpStatus.BAD_REQUEST);
        }
        Pattern p = Pattern.compile("^[ A-Za-z]+$");         // Regex t check Employee Name Format
        Matcher m = p.matcher(employee.getEmpName());        // Matching New Employee Name With Regex
        boolean b = m.matches();
        if(b == false)                                      // Determining if Name Is In correct Format
        {
            return new ResponseEntity("Employee Name Format is not Valid",HttpStatus.BAD_REQUEST);
        }
        Designation designation = designationRepository.findByJobTitle(WordUtils.capitalize(employee.getJobTitle())); // Fetching Details of Designation From JobTile
                                                                                               // Using WordUtils.Capitalize to Capitalize first Letter of word in String
        if (designation == null)                                                               // checking if JobTitle exist in Database or not
        {
            return new ResponseEntity("No Such Designation Exit For The Given JobTitle", HttpStatus.BAD_REQUEST);
        }
        List<Employee> employeeList= employeeRepository.findAll();
        int j= employeeList.size()+1;
        emp.setEmpId(j);       // Setting Employee Id
        emp.setEmpName(WordUtils.capitalize(employee.getEmpName().toLowerCase()));  // Setting Employee Name in Form "John Wick"
        emp.setDesignation(designation);       // Setting Employee Designation Details
        if(employeeRepository.findAll().size()!=0)
        {
            if (employee.getManagerId() == null)                       // if There is director then new Employee Manager Id cannot be Null
            {
                return new ResponseEntity("Manager Id Cannot Be Null", HttpStatus.BAD_REQUEST);
            }
            if (!employeeRepository.findById(employee.managerId).isPresent()){
                return new ResponseEntity("Manager do not exist",HttpStatus.BAD_REQUEST);
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
                ResponseEntity responseEntity =getEmployee(j);
                System.out.print(emp);
                return new ResponseEntity(responseEntity.getBody(), HttpStatus.CREATED);
            }
            else if (newEmployeeLevelId == 1)
            {
                return new ResponseEntity("Director Already Exist", HttpStatus.BAD_REQUEST);
            }
            else
                {
                return new ResponseEntity("New Employee Cannot Be Added Due To Same Designation Post", HttpStatus.BAD_REQUEST);
            }
        }
        else
        {
                if(employee.getManagerId()==-1 || employee.getManagerId()==null)
                {
                    emp.setManagerId(-1);                       // Manager Id In Case Of Very First employee can be null
                    employeeRepository.save(emp);                 // Saving The Employee Details in Repository
                    return new ResponseEntity("Very First Employee In EMS is Added", HttpStatus.CREATED);

                }
                else
                {
                    return new ResponseEntity("Very First Employee Cannot Have Manager Id",HttpStatus.BAD_REQUEST);
                }
        }
    }
 /*---------------------- Method to Update an Employee Details (PUT) ---------------------------------------*/
    public ResponseEntity updateEmployeeDetails(int id,EmployeePost employee)
    {
        if((employee.getEmpName()==null&&employee.jobTitle==null&&employee.getManagerId()==null)||(employee.getEmpName()==""&&employee.jobTitle==""&&employee.getManagerId()==null)) // Checking If Request Body is Null
        {
            return new ResponseEntity("No Response Body",HttpStatus.BAD_REQUEST);
        }
        Employee employeeDetails = new Employee();
        List<Employee> employeeList= employeeRepository.findAll();
        if(id > 0)                               // Checking for Valid Employee Id
        {
            for (int i = 0; i < employeeList.size(); i++) {
                if (employeeList.get(i).getEmpId() == id) {
                    employeeDetails = employeeList.get(i);                      // Getting Employee Details after getting EmpId Passed in json body
                }
            }
            if(employeeDetails.getEmpId() == null)                        // Finding If Employee For Given Id is Present or Not
            {
                return new ResponseEntity("Employee Details Not Present For Given Employee Id",HttpStatus.BAD_REQUEST);
            }
        }
        else
        {
            return new ResponseEntity("Invalid Employee Id Is Passed",HttpStatus.BAD_REQUEST);
        }
        if(employee.getReplace()==false) // Updating the Details of employee with given Changes
        {
            if(employee.getEmpName()!=null) {
                Pattern p = Pattern.compile("^[ A-Za-z]+$");            // Regex t check Employee Name Format
                Matcher m = p.matcher(employee.getEmpName());           // Matching New Employee Name With Regex
                boolean b = m.matches();
                if(b==true){
                    employeeDetails.setEmpName(WordUtils.capitalize(employee.getEmpName()));  //Setting Employee Name To New Name as Requested
                }
                else{
                    return new ResponseEntity("Employee Name is not in Correct Format",HttpStatus.BAD_REQUEST);
                }
            }
            else
            {
                employeeDetails.setEmpName(employeeDetails.getEmpName());   // If New Name is Not provide Name is set to previous name
                employeeRepository.save(employeeDetails);
            }
            if(employee.getJobTitle()!=null) {
                if (employeeDetails.getJobTitle().equals("Director")) {
                    return new ResponseEntity("Director Designation Cannot be Updated", HttpStatus.BAD_REQUEST);
                }
                Designation designation = designationRepository.findByJobTitle(WordUtils.capitalize(employee.getJobTitle())); // fetching designation Details From Passed new job Title after setting job Title to DB format
                if (designation == null) {                                                                   // if Job Title Exit or not
                    return new ResponseEntity("Such Designation Do not Exit", HttpStatus.BAD_REQUEST);
                }
                int newLevelId = designation.getLevelId();
                Optional<Employee> manager = employeeRepository.findById(employeeDetails.getManagerId());             // Getting Manager Details
                int parentLevelId = manager.get().getDesignation().getLevelId();
//                int currentLevelId = employeeDetails.getDesignation().getLevelId();
                List<Employee> subordinate = new ArrayList<>();
                List<Employee> employeeListForSubordinates = employeeRepository.findAllByOrderByDesignation_LevelIdAscEmpNameAsc();
                for (int i = 0; i < employeeListForSubordinates.size(); i++) {
                    if (employeeListForSubordinates.get(i).getManagerId() == employeeDetails.getEmpId())               //Getting Subordinated of Employee in Order to Determine Till where Employee Designation can be Degraded
                    {
                        subordinate.add(employeeListForSubordinates.get(i));
                    }
                }
                if (subordinate.isEmpty()) {                                                             //If Emloyee Has No Subordinates Then designation can Be Degraded to lowest Designation
                    if ((newLevelId > parentLevelId)) {
                        employeeDetails.setDesignation(designationRepository.findByJobTitle(WordUtils.capitalize(employee.getJobTitle())));  // Setting The New Designation for Employee
                        employeeDetails.setJobTitle(WordUtils.capitalize(employee.getJobTitle()));
                        employeeRepository.save(employeeDetails);
                    }
                    else
                    {
                        return new ResponseEntity("Level Id Must Not Greater then His Manager Level Id And Lower Then His subordinate Level Id",HttpStatus.BAD_REQUEST);
                    }
                }
                else                                       //If Emloyee Has Subordinates Then designation can Be Degraded till designation higher then his Subordinate With Highest Designation
                    {
                    int permittedLevelId=subordinate.get(0).getDesignation().getLevelId();
                    if ((newLevelId > parentLevelId) && (newLevelId < permittedLevelId))                                         //Comparing the New level Id with Employee Manager Id and his current level id
                    {                                                                                                   // Assumption Employee Cannot given Lower designation
                        employeeDetails.setDesignation(designationRepository.findByJobTitle(WordUtils.capitalize(employee.getJobTitle())));  // Setting The New Designation for Employee
                        employeeDetails.setJobTitle(WordUtils.capitalize(employee.getJobTitle()));
                        employeeRepository.save(employeeDetails);

                    } else {
                        return new ResponseEntity("Level Id Must Not Greater then His Manager Level Id And Lower Then His subordinate Level Id", HttpStatus.BAD_REQUEST);
                    }
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
                if (managerLevelId < employeeLevelId)   // Matching Designation Details Of New Manager and Employee
                {
                    employeeDetails.setManagerId(employee.getManagerId());
                    employeeRepository.save(employeeDetails);
                }
                else
                    {
                    return new ResponseEntity("Employee Cannot Have Same or Higher Designation As His/Her New manager",HttpStatus.BAD_REQUEST);
                    }
            }
            else
                {
                employeeDetails.setManagerId(employeeDetails.getManagerId());
                employeeRepository.save(employeeDetails);
               }
            ResponseEntity responseEntity=getEmployee(employeeDetails.getEmpId()); // Creating New Object of Type ResponseEntity to fetch the details of newly Added Employ
            return new ResponseEntity(responseEntity.getBody(),HttpStatus.OK);     // Returning only body part of ResponseEntity object
        }
        else if (employee.getReplace() == true)                                                                         // Replacing the Old Employee with New Employee
            {
              Employee newEmployee= new Employee();
              Designation designation = designationRepository.findByJobTitle(WordUtils.capitalize(employee.getJobTitle())); // fetching designation Details From Passed new job Title after setting job Title to DB format
              if(designation==null)
              {
                  return new ResponseEntity("JobTitle Cannot be Null",HttpStatus.BAD_REQUEST);
              }
              if(employee.getEmpName()!=null) {
                  Pattern p = Pattern.compile("^[ A-Za-z]+$");
                  Matcher m = p.matcher(employee.getEmpName());
                  boolean b = m.matches();
                  if (b == false) {
                      return new ResponseEntity("Employee Name Is not in Correct Format", HttpStatus.BAD_REQUEST);
                  }
              }
              else{
                  return  new ResponseEntity("Employee Name Cannot be Empty or Null",HttpStatus.BAD_REQUEST);
              }
              int oldLevelId=employeeDetails.getDesignation().getLevelId();
              int newLevelId=designation.getLevelId();
              if(newLevelId == oldLevelId)
              {
                  newEmployee.setEmpId(employeeList.size()+1);
                  newEmployee.setEmpName(WordUtils.capitalize(employee.getEmpName().toLowerCase()));
                  newEmployee.setDesignation(designation);
                  if(employee.getManagerId() == null) {
                      newEmployee.setManagerId(employeeDetails.getManagerId());
                  }
                  else{
                      newEmployee.setManagerId(employee.getManagerId());
                  }
                  employeeRepository.save(newEmployee);
                  List<Employee> subordinateList =new ArrayList<>();     // Conditions for changing the ManagerId of Subordinates
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
                  ResponseEntity  responseEntity =getEmployee(newEmployee.getEmpId());
                  return new ResponseEntity(responseEntity.getBody(),HttpStatus.OK);
              }
              else
              {
                  return new ResponseEntity("Designation Not Same, So Employee Cannot Be Added",HttpStatus.BAD_REQUEST);
              }
        }
        else
        {
            return  new ResponseEntity(" Bad Request "+ employee.getReplace(),HttpStatus.BAD_REQUEST);
        }
    }
}

