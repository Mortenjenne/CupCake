package app.services;

import app.persistence.UserMapper;

public class UserServiceImpl implements UserService
{
    private UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper)
    {
        this.userMapper = userMapper;
    }
}
