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
public class  EmployeeController {

    @Autowired
    EmployeeService employeeService;

        @GetMapping("/employees")
        public ResponseEntity getAllEmployee(){
             return employeeService.getAllEmployee();
         }

        @GetMapping("/employees/{id}")
        public ResponseEntity getEmployee(@PathVariable("id") int id){
            return employeeService.getEmployee(id);
        }

        @DeleteMapping("/employees/{id}")
         public ResponseEntity deleteEmployeeById(@PathVariable("id")int id){
            return employeeService.deleteEmployeeById(id);
        }

        @PostMapping("/employees")
         public ResponseEntity addEmployee(@RequestBody EmployeePost employee){
            return employeeService.addEmployee(employee);
        }

        @PutMapping("/employees/{id}")
        public ResponseEntity updateEmployeeDetails(@PathVariable("id")int id,@RequestBody EmployeePost employee){
            return employeeService.updateEmployeeDetails(id,employee);
        }
}
