package com.organization;

import com.organization.Controller.EmployeeController;
import com.organization.Repository.EmployeeRepository;
import javafx.application.Application;
import jdk.net.SocketFlow;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;

import java.awt.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {EmployeeApplication.class, EmployeeController.class})

public class EmployeeApplicationTests extends AbstractTransactionalTestNGSpringContextTests {
    @Autowired
    MockMvc mockMvc;

    private String path = "/rest/employees";

    @Autowired
    EmployeeRepository employeeRepository;
    // Test GetAllEmployee
    @Test
        public void getAllEmployeeTest() throws Exception{
        mockMvc.perform(get(path))
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].empName").value("Thor"))
                .andExpect(jsonPath("$[0].jobTitle").value("Director"));
    }

    // Test if getAllEmployee method returns  empty list
    @Test
    public void getAllEmployeeNullTest() throws  Exception{
        employeeRepository.deleteAll();
        mockMvc.perform(get(path))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    //Test the Response Status of GetAll Employee method
    @Test
    public void getAllEmployeeStatusTest() throws Exception{
        mockMvc.perform(get(path))
                .andDo(print())
                .andExpect(status().isOk());
    }

    //Test getEmployeeById and also check its Response status
    @Test
    public void testGetEmployeeById() throws Exception {
        mockMvc.perform(get(path+"/{id}",1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isMap());
    }

    //Test getEmployee( int id) for if id is negative
    @Test
    public void testGetEmployeeByIdForInvalidEmployeeId() throws Exception{
        mockMvc.perform(get(path+"/{id}",-1))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // Test getEmployee(Int Id) for Invalid empId
    @Test
    public void testGetEmployeeByForEmployeeIdNotPresent() throws Exception{
        mockMvc.perform(get(path+"/{id}",20))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // Test deleteEmployeeById(int id) if id passed as argument is Valid or Not
    @Test
    public void testDeleteEmployeeByIdForInvalidNegativeEmployeeId() throws Exception{
        mockMvc.perform(delete(path+"/{id}",-2))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // Test deleteEmployeeById(int id) if Employee Is not present for given Employee Email Id
    @Test
    public void testDeleteEmployeeByIdForEmployeeIdNotPresent() throws Exception{
        mockMvc.perform(delete(path+"/{id}",20))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // Test deleteEmployeeById(int id) along with its Response Status
    @Test
    public void testDeleteEmployeeById() throws Exception{
        mockMvc.perform(delete(path+"/{id}",10))
                .andDo(print())
                .andExpect(status().isOk());
    }

    //Test deleteEmployeeById(int id) for deleting Director with multiple Subordinates
    @Test
    public void TestForDeletingDirectorWithSubordinates() throws Exception{
        mockMvc.perform(delete(path+"/{id}",1))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    //Test deleteEmployeeById(int id) for deleting Director without  any Subordinates
    @Test
    public void TestForDeletingDirectorWithoutSubordinates() throws Exception{
        for (int i=2;i<=10;i++){
            employeeRepository.deleteById(i);
        }
        mockMvc.perform(delete(path+"/{id}",1))
                .andDo(print())
                .andExpect(status().isOk());
    }
}