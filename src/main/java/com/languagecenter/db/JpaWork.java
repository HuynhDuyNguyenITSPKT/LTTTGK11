package com.languagecenter.db;

import jakarta.persistence.EntityManager;
@FunctionalInterface
public interface JpaWork<T> {
    T execute(EntityManager em) throws Exception;
}
