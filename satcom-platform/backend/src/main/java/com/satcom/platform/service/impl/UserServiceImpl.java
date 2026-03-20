package com.satcom.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.satcom.platform.entity.User;
import com.satcom.platform.mapper.UserMapper;
import com.satcom.platform.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 * <p>提供用户相关的业务逻辑处理功能</p>
 *
 * @author satcom
 * @version 1.0.0
 * @since 2024-01-01
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
