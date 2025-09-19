package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //用户id存如BaseContext
        BaseContext.setCurrentId(employee.getId());

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @Override
    public Result save(EmployeeDTO employeeDTO) {
        //数据拷贝
        Employee employee = BeanUtil.copyProperties(employeeDTO, Employee.class);
        //创建/修改时间/人
        LocalDateTime now = LocalDateTime.now();
        employee.setCreateTime(now);
        employee.setUpdateTime(now);

        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        //状态
        employee.setStatus(StatusConstant.ENABLE);
        //密码 加密
        employee.setPassword(DigestUtils.
                md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        employeeMapper.insert(employee);
        return Result.success();
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        //开启分页
        PageHelper.startPage(employeePageQueryDTO.getPage(),
                employeePageQueryDTO.getPageSize());
        //分页查询
        Page<Employee> page = employeeMapper.page(employeePageQueryDTO);
        //查询结果
        long total = page.getTotal();
        List<Employee> result = page.getResult();
        PageResult pageResult = new PageResult(total, result);
        return Result.success(pageResult);
    }

    /**
     * 员工启用或禁用
     * @param status
     * @return
     */
    @Override
    public Result startOrStop(Integer status, Long id) {
        //创建对象 更新状态，更新时间/人
        Employee employee = Employee.builder().status(status)
                .updateUser(BaseContext.getCurrentId())
                .updateTime(LocalDateTime.now())
                .id(id).build();

        //修改数据库
        employeeMapper.update(employee);
        return Result.success();
    }

}
