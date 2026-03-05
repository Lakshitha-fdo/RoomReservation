package com.oceanview.dao;

public interface UserDao {
    boolean isValidUser(String username, String password);
}
