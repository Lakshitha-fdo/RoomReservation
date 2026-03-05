package com.oceanview.service;

import com.oceanview.dao.UserDao;
import com.oceanview.model.ServiceResult;
import com.oceanview.util.ValidationUtil;

public class AuthService {
    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public ServiceResult<Void> login(String username, String password) {
        if (ValidationUtil.isBlank(username) || ValidationUtil.isBlank(password)) {
            return ServiceResult.fail("Username and password are required.");
        }
        boolean valid = userDao.isValidUser(username.trim(), password.trim());
        return valid ? ServiceResult.ok("Login successful.", null) : ServiceResult.fail("Invalid username or password.");
    }
}
