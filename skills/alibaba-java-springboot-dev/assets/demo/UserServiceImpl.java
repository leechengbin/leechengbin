package com.company.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.project.entity.User;
import com.company.project.exception.BusinessException;
import com.company.project.repository.UserMapper;
import com.company.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 * <p>继承 MyBatis-Plus 的 ServiceImpl，自动获得 CRUD 实现</p>
 *
 * @author zhangsan
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getUserById(Long id) {
        log.info("查询用户信息，id={}", id);
        // 使用 MyBatis-Plus 的 getById 方法
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在，id=" + id);
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        log.info("根据用户名查询用户，username={}", username);
        // 使用 LambdaQueryWrapper 构建类型安全的查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("用户不存在，username=" + username);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(User user) {
        log.info("创建用户，username={}", user.getUsername());
        
        // 检查用户名是否已存在
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 使用 MyBatis-Plus 的 save 方法
        boolean success = this.save(user);
        if (!success) {
            throw new BusinessException("创建用户失败");
        }
        
        log.info("用户创建成功，id={}", user.getId());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(User user) {
        log.info("更新用户，id={}", user.getId());
        
        // 检查用户是否存在
        User existUser = this.getById(user.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在，id=" + user.getId());
        }
        
        // 使用 MyBatis-Plus 的 updateById 方法
        boolean success = this.updateById(user);
        if (!success) {
            throw new BusinessException("更新用户失败");
        }
        
        log.info("用户更新成功，id={}", user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        log.info("删除用户，id={}", id);
        
        // 检查用户是否存在
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在，id=" + id);
        }
        
        // 使用 MyBatis-Plus 的 removeById 方法（逻辑删除）
        boolean success = this.removeById(id);
        if (!success) {
            throw new BusinessException("删除用户失败");
        }
        
        log.info("用户删除成功，id={}", id);
    }
}
