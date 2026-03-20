package com.company.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.company.project.entity.User;

/**
 * 用户服务接口
 * <p>继承 MyBatis-Plus 的 IService，获得通用 CRUD 方法</p>
 *
 * @author zhangsan
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface UserService extends IService<User> {

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    User getUserById(Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 用户 ID
     */
    Long createUser(User user);

    /**
     * 更新用户
     *
     * @param user 用户信息
     */
    void updateUser(User user);

    /**
     * 删除用户
     *
     * @param id 用户 ID
     */
    void deleteUser(Long id);
}
