package com.expenseflow.config;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class HibernateFilterAspect {

    @Autowired
    private EntityManager entityManager;

    @Before("execution(* org.springframework.data.repository.Repository+.*(..))")
    public void enableDeletedFilter() {
        try {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("deletedFilter");
        } catch (Exception e) {
            // Log warning or ignore if session is not active/available
        }
    }
}
