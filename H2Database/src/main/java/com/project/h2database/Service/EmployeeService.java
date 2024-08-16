package com.project.h2database.Service;

import com.project.h2database.Entity.Employee;
import com.project.h2database.Repository.EmployeeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService implements EmployeeInterface{

    @Autowired
    EmployeeRepo employeeRepo;
    @Override
    public Employee getEmployeeById(Integer id) {
        Employee emp = new Employee();
        Optional<Employee> byId = employeeRepo.findById(id);
        if(byId.isPresent()){
              emp = byId.get();
        }
        return emp;
    }

    @Override
    public Employee saveEmployee(Employee employee) {
        return employeeRepo.save(employee);
    }

    @Override
    public List<Employee> getAllEmployee() {
        return employeeRepo.findAll();
    }

    @Override
    public void deleteEmployee(Integer id) {
        employeeRepo.deleteById(id);
    }
}
