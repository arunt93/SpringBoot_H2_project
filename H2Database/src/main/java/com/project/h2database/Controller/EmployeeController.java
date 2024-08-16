package com.project.h2database.Controller;

import com.project.h2database.Entity.Employee;
import com.project.h2database.Service.EmployeeInterface;
import com.project.h2database.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    EmployeeInterface employeeService;

    @PostMapping("/save")
    public String saveEmployee(@RequestBody Employee employee){
        Employee emp = employeeService.saveEmployee(employee);
        return emp.toString();
    }

    @GetMapping("/get-by-id")
    public Employee getEmpById(@RequestParam(name = "id")Integer id){
       return employeeService.getEmployeeById(id);
    }

    @GetMapping("/get-all")
    public List<Employee> getAllEmployee(){
        return employeeService.getAllEmployee();
    }
    @DeleteMapping("/delete")

    public void deleteEmp(@RequestParam Integer id){
        employeeService.deleteEmployee(id);
    }
}
