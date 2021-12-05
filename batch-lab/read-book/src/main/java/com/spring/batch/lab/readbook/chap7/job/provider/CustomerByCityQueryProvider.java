package com.spring.batch.lab.readbook.chap7.job.provider;

import org.springframework.batch.item.database.orm.AbstractJpaQueryProvider;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class CustomerByCityQueryProvider extends AbstractJpaQueryProvider {

    private String cityName;

    @Override
    public Query createQuery() {
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createQuery(
                "SELECT c FROM CustomerJPA " +
                        "c WHERE c.city = :city");
        query.setParameter("city", this.cityName);
        return query;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.cityName, "City name is required");
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }
}
