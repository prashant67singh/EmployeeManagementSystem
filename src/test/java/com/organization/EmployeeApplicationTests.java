package com.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.organization.Controller.EmployeeController;
import com.organization.Entity.Employee;
import com.organization.Entity.EmployeePost;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.testng.annotations.Test;
import org.springframework.http.MediaType;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import springfox.documentation.spring.web.json.Json;

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

    // Test deleteEmployeeById(int id) along with its Response Status for Employee with no subordinates
    @Test
    public void testDeleteEmployeeByIdWithNoSubordinates() throws Exception{
        mockMvc.perform(delete(path+"/{id}",10))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // Test deleteEmployeeById(int id) along with its Response Status for Employee with multiple subordinates
    @Test
    public void testDeleteEmployeeByIdWithSubordinates() throws Exception{
        mockMvc.perform(delete(path+"/{id}",3))
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
    //Test addEmployee() for correct Json Body
    @Test
    public void TestForAddEmployee() throws Exception{
        EmployeePost employee =new EmployeePost();
        employee.setEmpName("Prashant");
        employee.setManagerId(1);
        employee.setJobTitle("Manager");
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // Test addEmployee() for null Manager Id
    @Test
    public void TestForAddEmployeeForNullManagerId() throws Exception{
        EmployeePost employee =new EmployeePost();
        employee.setEmpName("Prashant");
//        employee.setManagerId(1);
        employee.setJobTitle("Manager");
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // Test addEmployee() for null Employee Name
    @Test
    public void TestForAddEmployeeForNullEmployeeName() throws Exception{
        EmployeePost employee =new EmployeePost();
//        employee.setEmpName("Prashant");
        employee.setManagerId(1);
        employee.setJobTitle("Manager");
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // Test addEmployee() for null Employee JobTitle
    @Test
    public void TestForAddEmployeeForNullJobTile() throws Exception{
        EmployeePost employee =new EmployeePost();
        employee.setEmpName("Prashant");
        employee.setManagerId(1);
//        employee.setJobTitle("Manager");
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // Test addEmployee() , if Designation is assigned is not present in the  Designation Table
    @Test
    public void TestForAddEmployeeForInValidDesignation() throws Exception{
        EmployeePost employee =new EmployeePost();
        employee.setEmpName("Prashant");
        employee.setManagerId(1);
       employee.setJobTitle("manager");
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
    // Test addEmployee() , Another Director is Added if One Director already Exist
    @Test
    public void TestForAddEmployeeForAddingAnotherDirector() throws Exception{
        EmployeePost employee =new EmployeePost();
        employee.setEmpName("Prashant");
        employee.setManagerId(2);
        employee.setJobTitle("Director");
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // Test addEmployee() , Adding of Employee with Higher Or Same level Id as of his Manager
    @Test
    public void TestForAddEmployeeForAddingEmployeeWithHigherOrEqualDesgnLevel() throws Exception{
        EmployeePost employee =new EmployeePost();
        employee.setEmpName("Prashant");
        employee.setManagerId(2);
        employee.setJobTitle("Manager");
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    // Test addEmployee() , Adding first Employee in EMS with Null ManagerId
    @Test
    public void TestForAddEmployeeForAddingFirstEmployeeWithNullManagerId() throws Exception{
        employeeRepository.deleteAll();
        EmployeePost employee =new EmployeePost();
        employee.setEmpName("Prashant");
      //  employee.setManagerId(2);
        employee.setJobTitle("Manager");
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk());
    }
    // Test addEmployee() , Adding first Employee in EMS with Some ManagerId
    @Test
    public void TestForAddEmployeeForAddingFirstEmployeeWithManagerId() throws Exception{
        employeeRepository.deleteAll();
        EmployeePost employee =new EmployeePost();
        employee.setEmpName("Prashant");
        employee.setManagerId(2);
        employee.setJobTitle("Manager");
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // Test updateEmployee() for checking validity of Employee Id Passed
    @Test
    public  void testUpadtEmployeeForValidEmployeeId() throws Exception{
        EmployeePost employee = new EmployeePost();
        employee.setEmpId(-2);
        employee.setEmpName("Prashant Singh");
        employee.setJobTitle("Manager");
        employee.setManagerId(1);
        employee.setReplace(true);
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(put(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    // Test updateEmployee() for checking Employee Id Passed is Null
    @Test
    public  void testUpadtEmployeeForNullEmployeeId() throws Exception{
        EmployeePost employee = new EmployeePost();
        // employee.setEmpId(2);
        employee.setEmpName("Prashant Singh");
        employee.setJobTitle("Manager");
        employee.setManagerId(1);
        employee.setReplace(true);
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(put(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // Test updateEmployee() for checking of Employee Id Passed is present in Table
    @Test
    public  void testUpadteEmployeeForEmployeeIdNotPresent() throws Exception{
        EmployeePost employee = new EmployeePost();
        employee.setEmpId(12);
        employee.setEmpName("Prashant Singh");
        employee.setJobTitle("Manager");
        employee.setManagerId(1);
        employee.setReplace(true);
        ObjectMapper objectMapper =new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,false);
        ObjectWriter objectWriter =objectMapper.writer().withDefaultPrettyPrinter();
        String requestJson=objectWriter.writeValueAsString(employee);
        mockMvc.perform(put(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }



}