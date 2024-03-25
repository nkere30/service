package com.epam.rd.autocode.service;

import com.epam.rd.autocode.ConnectionSource;
import com.epam.rd.autocode.domain.Department;
import com.epam.rd.autocode.domain.Employee;
import com.epam.rd.autocode.domain.FullName;
import com.epam.rd.autocode.domain.Position;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmployeeServiceImpl implements EmployeeService{
    private static final String FIND_MANAGER = "SELECT * FROM employee WHERE id = ?";
    private static final String FIND_DEP = "SELECT * FROM department WHERE id = ?";
    private static final String GET_ALL_SORT_BY_HIRE_DATE = "SELECT * FROM employee ORDER BY hiredate";
    @Override
    public List<Employee> getAllSortByHireDate(Paging paging) {
        List<Employee> employees = new ArrayList<>();
        try (Connection connection = ConnectionSource.instance().createConnection()) {
            PreparedStatement statement = connection.prepareStatement(GET_ALL_SORT_BY_HIRE_DATE);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Employee employee = mapResultSetToEmployee(resultSet);
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    @Override
    public List<Employee> getAllSortByLastname(Paging paging) {
        return null;
    }

    @Override
    public List<Employee> getAllSortBySalary(Paging paging) {
        return null;
    }

    @Override
    public List<Employee> getAllSortByDepartmentNameAndLastname(Paging paging) {
        return null;
    }

    @Override
    public List<Employee> getByDepartmentSortByHireDate(Department department, Paging paging) {
        return null;
    }

    @Override
    public List<Employee> getByDepartmentSortBySalary(Department department, Paging paging) {
        return null;
    }

    @Override
    public List<Employee> getByDepartmentSortByLastname(Department department, Paging paging) {
        return null;
    }

    @Override
    public List<Employee> getByManagerSortByLastname(Employee manager, Paging paging) {
        return null;
    }

    @Override
    public List<Employee> getByManagerSortByHireDate(Employee manager, Paging paging) {
        return null;
    }

    @Override
    public List<Employee> getByManagerSortBySalary(Employee manager, Paging paging) {
        return null;
    }

    @Override
    public Employee getWithDepartmentAndFullManagerChain(Employee employee) {
        return null;
    }

    @Override
    public Employee getTopNthBySalaryByDepartment(int salaryRank, Department department) {
        return null;
    }

    private Employee mapResultSetToEmployee(ResultSet resultSet) throws SQLException {
        BigInteger id = resultSet.getBigDecimal("id").toBigInteger();
        String firstName = resultSet.getString("firstName");
        String lastName = resultSet.getString("lastName");
        String middleName = resultSet.getString("middleName");
        FullName fullName = new FullName(firstName, lastName, middleName);
        Position position = Position.valueOf(resultSet.getString("position"));
        LocalDate hired = resultSet.getDate("hiredate").toLocalDate();
        BigDecimal salary = resultSet.getBigDecimal("salary");
        BigInteger managerId = resultSet.getBigDecimal("manager") != null ?
                resultSet.getBigDecimal("manager").toBigInteger() : new BigInteger(String.valueOf(0));
        BigInteger departmentId = resultSet.getBigDecimal("department") != null ?
                resultSet.getBigDecimal("department").toBigInteger() : new BigInteger(String.valueOf(0));
        Employee manager = findManager(managerId);
        Department department = findDepartment(departmentId);
        return new Employee(id, fullName, position, hired, salary, manager, department);
    }

    private Department mapResultSetToDepartments(ResultSet resultSet) throws SQLException {
        BigInteger id = resultSet.getBigDecimal("id").toBigInteger();
        String depName = resultSet.getString("name");
        String location = resultSet.getString("location");
        return new Department(id, depName, location);
    }

    private Department findDepartment(BigInteger departmentId) {
        try (Connection connection = ConnectionSource.instance().createConnection()) {
            PreparedStatement statement = connection.prepareStatement(FIND_DEP);
            statement.setInt(1, departmentId.intValue());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToDepartments(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    private Employee findManager(BigInteger managerId) {
        try (Connection connection = ConnectionSource.instance().createConnection()) {
            PreparedStatement statement = connection.prepareStatement(FIND_MANAGER);
            statement.setInt(1, managerId.intValue());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return mapResultSetToEmployee(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }
}
