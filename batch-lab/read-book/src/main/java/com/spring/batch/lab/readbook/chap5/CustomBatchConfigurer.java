package com.spring.batch.lab.readbook.chap5;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@RequiredArgsConstructor
public class CustomBatchConfigurer extends DefaultBatchConfigurer {

    @Qualifier("repositoryDataSource")
    private final DataSource dataSource;

    @Qualifier("batchTransactionManager")
    private final PlatformTransactionManager transactionManager;

    @Override
    protected JobRepository createJobRepository() throws Exception {
        JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
        factoryBean.setDatabaseType(DatabaseType.MYSQL.getProductName());
        factoryBean.setTablePrefix("FOO_");
        factoryBean.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ");
        factoryBean.setDataSource(this.dataSource);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }


    /**
     * JobRepository 읽기 전용
     */
    @Override
    protected JobExplorer createJobExplorer() throws Exception {
        JobExplorerFactoryBean factoryBean = new JobExplorerFactoryBean();
        factoryBean.setDataSource(this.dataSource);
        factoryBean.setTablePrefix("FOO_");
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
}
