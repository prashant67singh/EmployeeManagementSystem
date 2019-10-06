package com.organization.Controller;

import com.organization.Entity.Employee;
import com.organization.Entity.EmployeePost;
import com.organization.Repository.EmployeeRepository;
import com.organization.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class  EmployeeController {

    @Autowired 
    EmployeeService employeeService;

        @GetMapping("/rest/employees")
        public List<Employee> getAllEmployee(){
            List<Employee> employees=employeeService.getAllEmployee();
            return employees;
         }

        @GetMapping("/rest/employees/{id}")
        public Map<String,List<Employee>> getEmployee(@PathVariable("id") int id){
            return employeeService.getEmployee(id);
        }

        @DeleteMapping("/rest/employees/{id}")
         public ResponseEntity deleteEmployeeById(@PathVariable("id")int id){
            return employeeService.deleteEmployeeById(id);
        }

        @PostMapping("/rest/employees")
         public ResponseEntity addEmployee(@RequestBody EmployeePost employee){
            return employeeService.addEmployee(employee);
        }

        @PutMapping("/rest/employees")
        public ResponseEntity updateEmployeeDetails(@RequestBody EmployeePost employee){
            return employeeService.updateEmployeeDetails(employee);
        }
}
