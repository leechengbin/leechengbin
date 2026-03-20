package com.company.project.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.project.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

/**
 * 用户数据访问接口
 * <p>继承 MyBatis-Plus 的 BaseMapper，自动获得 CRUD 能力</p>
 *
 * @author zhangsan
 * @version 1.0.0
 * @since 2024-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * <p>使用@Param 注解指定参数名称</p>
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM t_user WHERE username = #{username} AND deleted = 0")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    @Select("SELECT * FROM t_user WHERE email = #{email} AND deleted = 0")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM t_user WHERE phone = #{phone} AND deleted = 0")
    Optional<User> findByPhone(@Param("phone") String phone);
}
