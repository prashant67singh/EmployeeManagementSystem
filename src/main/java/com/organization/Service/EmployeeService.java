package com.organization.Service;

import com.organization.Entity.Employee;
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

    /*Method to get all Employees in database*/
    public List<Employee> getAllEmployee() {
        List<Employee> employeeList=employeeRepository.findAllByOrderByDesignation_LevelIdAscEmpNameAsc();
        return  employeeList;
    }

    /*Method to get Details of employee  provided Employee Id. This Method returns details of Employee, His Manager details
    * ,his colleague details and also the details of Employee reporting to that particular Employee  */
    public Map<String,List<Employee>> getEmployee(int id){
        Map<String,List<Employee>> details = new LinkedHashMap<>();
        Optional<Employee> employee = employeeRepository.findById(id);
        List<Employee> employeeDetails= new ArrayList<>();
        employeeDetails.add(employee.get());
        details.put("Employee:", employeeDetails);
        if(employee.get().getManagerId()!=null) {
            Optional<Employee> managerDetails = employeeRepository.findById(employee.get().getManagerId());
            List<Employee> managerValue = new ArrayList<>();
            managerValue.add(managerDetails.get());
            details.put("Manager:", managerValue);
        }
        List<Employee> employeeList = employeeRepository.findAllByOrderByDesignation_LevelIdAscEmpNameAsc();
        List<Employee> colleague = new ArrayList<>();
        for(int i=0;i<employeeList.size();i++){
            if(employeeList.get(i).getManagerId()== employee.get().getManagerId() && employee.get().getEmpId() != employeeList.get(i).getEmpId()){
                colleague.add(employeeList.get(i));
            }
        }
        if(colleague.size()!=0) {
            details.put("Colleague ", colleague);
        }
        List<Employee> subordinate= new ArrayList<>();
        for(int i=0;i<employeeList.size();i++){
            if(employeeList.get(i).getManagerId()== employee.get().getEmpId()) {
                subordinate.add(employeeList.get(i));
            }
        }
        if(subordinate.size()!=0) {
            details.put("Reporting To:", subordinate);
        }
        return details;
    }

    /*Method to delete an employee from table provided with employee id */
    public ResponseEntity deleteEmployeeById(int id) {
        Optional<Employee> employeeDetails= employeeRepository.findById(id);
        if(employeeDetails.get().getEmpId()!= null) {
            System.out.println("ID FOUND");
            List<Employee> employeeList=employeeRepository.findAll();
            int c=0;
            for(int i=0;i<employeeList.size();i++) {
                if (employeeList.get(i).getManagerId() == employeeDetails.get().getEmpId()){
                    c=c+1;
                }
            }
            if(employeeDetails.get().getEmpId()==1 & c>=1){
                return new ResponseEntity(c,HttpStatus.BAD_REQUEST);
            }
            else if(employeeDetails.get().getEmpId() == 1 && c ==0){
                return new ResponseEntity("Successfull Deletion",HttpStatus.OK);
            }
            else if(employeeDetails.get().getEmpId()!=1 && c==0){
                return new ResponseEntity(HttpStatus.OK);
            }
            else{
                int j=employeeDetails.get().getManagerId();
                List<Employee> subordinate = new ArrayList<>();
                for
                return new ResponseEntity(c,HttpStatus.BAD_REQUEST);
            }
        }
        else {
            return new ResponseEntity("No Value Found",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        //    employeeRepository.deleteById(id);

    }
    }

