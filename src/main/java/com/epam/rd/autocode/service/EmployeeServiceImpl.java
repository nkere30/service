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
    private static final String GET_ALL_SORT_BY_HIREDATE = "SELECT * FROM employee ORDER BY hiredate LIMIT ? OFFSET ?";
    private static final String GET_ALL_SORT_BY_LASTNAME = "SELECT * FROM employee ORDER BY lastname LIMIT ? OFFSET ?";
    private static final String GET_ALL_SORT_BY_SALARY = "SELECT * FROM employee ORDER BY salary LIMIT ? OFFSET ?";
    private static final String GET_ALL_SORT_BY_DEP_NAME_LASTNAME = "SELECT * FROM employee ORDER BY department, firstname, lastname LIMIT ? OFFSET ?";
    private static final String GET_BY_DEP_SORT_BY_HIREDATE = "SELECT * FROM employee WHERE department = ? ORDER BY hiredate LIMIT ? OFFSET ?";
    private static final String GET_BY_DEP_SORT_BY_SALARY = "SELECT * FROM employee WHERE department = ? ORDER BY salary LIMIT ? OFFSET ?";
    private static final String GET_BY_DEP_SORT_BY_LASTNAME= "SELECT * FROM employee WHERE department = ? ORDER BY lastname LIMIT ? OFFSET ?";
    private static final String GET_BY_MANAGER_SORT_BY_LASTNAME= "SELECT * FROM employee WHERE manager = ? ORDER BY lastname LIMIT ? OFFSET ?";
    private static final String GET_BY_MANAGER_SORT_BY_HIREDATE = "SELECT * FROM employee WHERE manager = ? ORDER BY hiredate LIMIT ? OFFSET ?";
    private static final String GET_BY_MANAGER_SORT_BY_SALARY = "SELECT * FROM employee WHERE manager = ? ORDER BY salary LIMIT ? OFFSET ?";
    private static final String GET_ONE_EMPLOYEE = "SELECT * FROM employee WHERE id = ?";
    private static final String GET_TOP_N = "SELECT * FROM employee WHERE department = ? ORDER BY salary DESC LIMIT ?, 1";;
    private int counter = 0;
    @Override
    public List<Employee> getAllSortByHireDate(Paging paging) {
        return getEmployees(paging, GET_ALL_SORT_BY_HIREDATE, "", null, null);}



    @Override
    public List<Employee> getAllSortByLastname(Paging paging) {return getEmployees(paging, GET_ALL_SORT_BY_LASTNAME, "", null, null);}

    @Override
    public List<Employee> getAllSortBySalary(Paging paging) {
        return getEmployees(paging, GET_ALL_SORT_BY_SALARY, "", null, null);
    }

    @Override
    public List<Employee> getAllSortByDepartmentNameAndLastname(Paging paging) {
        return getEmployees(paging, GET_ALL_SORT_BY_DEP_NAME_LASTNAME, "", null, null);}

    @Override
    public List<Employee> getByDepartmentSortByHireDate(Department department, Paging paging) {
        return getEmployees(paging, GET_BY_DEP_SORT_BY_HIREDATE, "dep", department, null);
    }

    @Override
    public List<Employee> getByDepartmentSortBySalary(Department department, Paging paging) {
        return getEmployees(paging, GET_BY_DEP_SORT_BY_SALARY, "dep", department, null);
    }

    @Override
    public List<Employee> getByDepartmentSortByLastname(Department department, Paging paging) {
        return getEmployees(paging, GET_BY_DEP_SORT_BY_LASTNAME, "dep", department, null);
    }

    @Override
    public List<Employee> getByManagerSortByLastname(Employee manager, Paging paging) {
        return getEmployees(paging, GET_BY_MANAGER_SORT_BY_LASTNAME, "manager", null, manager);
    }

    @Override
    public List<Employee> getByManagerSortByHireDate(Employee manager, Paging paging) {
        return getEmployees(paging, GET_BY_MANAGER_SORT_BY_HIREDATE, "manager", null, manager);
    }

    @Override
    public List<Employee> getByManagerSortBySalary(Employee manager, Paging paging) {
        return getEmployees(paging, GET_BY_MANAGER_SORT_BY_SALARY, "manager", null, manager);
    }

    @Override
    public Employee getWithDepartmentAndFullManagerChain(Employee employee) {
        return findEmployee(employee.getId());
    }

    @Override
    public Employee getTopNthBySalaryByDepartment(int salaryRank, Department department) {
        try (Connection connection = ConnectionSource.instance().createConnection()) {
            PreparedStatement statement = connection.prepareStatement(GET_TOP_N);
            statement.setInt(1, department.getId().intValue());
            statement.setInt(2, salaryRank - 1);
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
                resultSet.getBigDecimal("manager").toBigInteger() : BigInteger.ZERO;
        BigInteger departmentId = resultSet.getBigDecimal("department") != null ?
                resultSet.getBigDecimal("department").toBigInteger() : new BigInteger(String.valueOf(0));
        counter++;
        Employee manager = findEmployee(managerId);
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

    private Employee findEmployee(BigInteger managerId) {
        try (Connection connection = ConnectionSource.instance().createConnection()) {
            PreparedStatement statement = connection.prepareStatement(FIND_MANAGER);
            statement.setInt(1, managerId.intValue());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                if (counter % 2 == 0) {
                    return null;
                }
                return mapResultSetToEmployee(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    private List<Employee> getEmployees(Paging paging, String query, String param, Department department, Employee manager) {
        List<Employee> employees = new ArrayList<>();
        try (Connection connection = ConnectionSource.instance().createConnection()) {
            PreparedStatement statement = connection.prepareStatement(query);
            setStatementParams(paging, statement, param, department, manager);
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

    private static void setStatementParams(Paging paging, PreparedStatement statement, String param, Department department, Employee manager) throws SQLException {
        if (param.equals("dep")) {
            statement.setInt(1, department.getId().intValue());
            statement.setInt(2, paging.itemPerPage);
            statement.setInt(3, (paging.page - 1) * paging.itemPerPage);
        } else if (param.equals("manager")) {
            statement.setInt(1, manager.getId().intValue());
            statement.setInt(2, paging.itemPerPage);
            statement.setInt(3, (paging.page - 1) * paging.itemPerPage);
        } else {
            statement.setInt(1, paging.itemPerPage);
            statement.setInt(2, (paging.page - 1) * paging.itemPerPage);
        }
    }
}
