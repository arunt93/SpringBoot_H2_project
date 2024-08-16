package com.project.h2database.Service;

import com.project.h2database.Entity.Employee;

import java.util.List;

public interface EmployeeInterface {
      Employee getEmployeeById(Integer id);

      Employee saveEmployee(Employee employee);

      List<Employee> getAllEmployee();

      void deleteEmployee(Integer id);

}
