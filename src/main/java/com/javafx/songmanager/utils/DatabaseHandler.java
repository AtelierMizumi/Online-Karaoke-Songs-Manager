package com.javafx.songmanager.utils;

import com.javafx.songmanager.models.UserInfo;
import com.javafx.songmanager.models.Song;

import javax.persistence.*;
import java.util.List;

public class DatabaseHandler {
    private static final EntityManagerFactory entityManagerFactory;

    static {
        entityManagerFactory = Persistence.createEntityManagerFactory("databasePU");
    }

    private final EntityManager entityManager;

    public DatabaseHandler() {
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    public void close() {
        entityManager.close();
    }

    public void saveUser(UserInfo user) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public UserInfo getUser(String username) {
        try {
            TypedQuery<UserInfo> query = entityManager.createQuery("SELECT u FROM UserInfo u WHERE u.username = :username", UserInfo.class);
            query.setParameter("username", username);
            List<UserInfo> users = query.getResultList();
            return users.isEmpty() ? null : users.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user", e);
        }
    }

    public void saveSong(Song song) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(song);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Failed to save song", e);
        }
    }
}