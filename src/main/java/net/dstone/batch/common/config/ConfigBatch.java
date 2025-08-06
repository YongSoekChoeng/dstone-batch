package net.dstone.batch.common.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

//import net.dstone.batch.common.annotation.AutoRegFlow;
//import net.dstone.batch.common.annotation.AutoRegJob;
//import net.dstone.batch.common.annotation.AutoRegStep;
import net.dstone.batch.common.core.BatchBaseObject;

@Configuration
@EnableBatchProcessing
public class ConfigBatch extends BatchBaseObject implements BatchConfigurer {

    private final PlatformTransactionManager txManagerCommon;
    private final DataSource dataSourceCommon;

    @Autowired
    public ConfigBatch(@Qualifier("txManagerCommon") PlatformTransactionManager txManagerCommon,
                       @Qualifier("dataSourceCommon") DataSource dataSourceCommon) {
        this.txManagerCommon = txManagerCommon;
        this.dataSourceCommon = dataSourceCommon;
    }

    @Override
    public JobRepository getJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(this.dataSourceCommon);
        factory.setTransactionManager(this.txManagerCommon);
        factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return this.txManagerCommon;
    }

    @Override
    public JobLauncher getJobLauncher() throws Exception {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(getJobRepository());
        launcher.afterPropertiesSet();
        return launcher;
    }

    @Override
    public JobExplorer getJobExplorer() throws Exception {
        JobExplorerFactoryBean factoryBean = new JobExplorerFactoryBean();
        factoryBean.setDataSource(this.dataSourceCommon);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
}
