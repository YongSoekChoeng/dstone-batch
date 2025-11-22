package net.dstone.batch.common.core;

import java.util.LinkedList;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public abstract class BaseJobConfig extends BaseBatchObject{

	@Autowired
	protected JobRepository jobRepository;

	@Autowired
	protected PlatformTransactionManager txManagerCommon;

    @Autowired 
    @Qualifier("sqlSessionFactorySample")
    protected SqlSessionFactory sqlSessionFactorySample; 

    @Autowired 
    @Qualifier("sqlSessionSample")
    protected SqlSessionTemplate sqlSessionSample; 

    @Autowired 
    @Qualifier("sqlBatchSessionSample")
    protected SqlSessionTemplate sqlBatchSessionSample; 
    
    @Autowired 
    @Qualifier("jobRegisterListener")
    protected JobExecutionListener jobRegisterListener;
    
	private String name;
	
	private LinkedList<Object> flowList = new LinkedList<Object>();
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	private boolean checkItemType(Object item) {
		boolean isValid = false;
		if(item != null) {
			if(item instanceof Flow) {
				isValid = true;
			}else if(item instanceof Step) {
				isValid = true;
			}else if(item instanceof Tasklet) {
				isValid = true;
			}
		}
		return isValid;
	}
	
	protected void addFlow(Flow flow) throws Exception {
		if(flow == null) {
			throw new Exception("null 을 세팅할 수 없습니다.");
		}else if(!this.checkItemType(flow)) {
			throw new Exception("지원하지않은 타입입니다.");
		}
		this.flowList.add(flow);
	}

	protected void addStep(Step step) throws Exception {
		if(step == null) {
			throw new Exception("null 을 세팅할 수 없습니다.");
		}else if(!this.checkItemType(step)) {
			throw new Exception("지원하지않은 타입입니다.");
		}
		this.flowList.add(step);
	}

	protected void addTasklet(Tasklet tasklet) throws Exception {
		if(tasklet == null) {
			throw new Exception("null 을 세팅할 수 없습니다.");
		}else if(!this.checkItemType(tasklet)) {
			throw new Exception("지원하지않은 타입입니다.");
		}
		this.flowList.add(tasklet);
	}

	public Job buildAutoRegJob() throws Exception {
		this.info(this.getClass().getName() + ".buildAutoRegJob() has been called !!!");
		StringBuffer regLog = new StringBuffer();
		Job job = null;
		String jobName = this.getName();
		try {
			
			regLog.setLength(0);
			regLog.append("\n").append("\n");
			regLog.append( "|------------------------------------ Job[" + jobName + "] configuration Start ------------------------------------|");
			
			this.debug(regLog.toString());
			this.configJob();
			JobBuilder jobBuilder = new JobBuilder(jobName, jobRepository);
			jobBuilder.listener(jobRegisterListener);
			FlowBuilder<Flow> jobFlowBuilder = new FlowBuilder<Flow>(jobName+"-Flow");
			for(int i=0; i<flowList.size(); i++) {
				Object flowItem = flowList.get(i);
				if( i == 0 ) {
					if(flowItem instanceof Flow) {
						String flowName = jobName + "-" + "Flow" + "-" + i;
						FlowBuilder<Flow> subFlowBuilder = new FlowBuilder<Flow>(flowName).start((Flow) flowItem);
						jobFlowBuilder.start(subFlowBuilder.build());
					}else if(flowItem instanceof Step) {
						String stepName = jobName + "-" + "Step" + "-" + i;
						FlowBuilder<Flow> subFlowBuilder = new FlowBuilder<Flow>(stepName).start((Step) flowItem);
						jobFlowBuilder.start(subFlowBuilder.build());
					}else if(flowItem instanceof Tasklet) {
						String taskletName = jobName + "-" + "Tasklet" + "-" + i;
						Tasklet tasklet = (Tasklet)flowItem;
						Step step = new StepBuilder(taskletName, jobRepository).tasklet(tasklet, txManagerCommon).build();
						FlowBuilder<Flow> subFlowBuilder = new FlowBuilder<Flow>(taskletName).start(step);
						jobFlowBuilder.start(subFlowBuilder.build());
					}else {
						throw new Exception("지원하지않은 타입입니다.");
					}
				}else {
					if(flowItem instanceof Flow) {
						String flowName = jobName + "-" + "Flow" + "-" + i;
						FlowBuilder<Flow> subFlowBuilder = new FlowBuilder<Flow>(flowName).start((Flow) flowItem);
						jobFlowBuilder.next(subFlowBuilder.build());
					}else if(flowItem instanceof Step) {
						String stepName = jobName + "-" + "Step" + "-" + i;
						FlowBuilder<Flow> subFlowBuilder = new FlowBuilder<Flow>(stepName).start((Step) flowItem);
						jobFlowBuilder.next(subFlowBuilder.build());
					}else if(flowItem instanceof Tasklet) {
						String taskletName = jobName + "-" + "Tasklet" + "-" + i;
						Tasklet tasklet = (Tasklet)flowItem;
						Step step = new StepBuilder(taskletName, jobRepository).tasklet(tasklet, txManagerCommon).build();
						FlowBuilder<Flow> subFlowBuilder = new FlowBuilder<Flow>(taskletName).start(step);
						jobFlowBuilder.next(subFlowBuilder.build());
					}else {
						throw new Exception("지원하지않은 타입입니다.");
					}
				}
			}
			job = jobBuilder.start(jobFlowBuilder.build()).end().build();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {

			regLog.setLength(0);
			regLog.append("\n");
			regLog.append( "|------------------------------------ Job[" + jobName + "] configuration End ------------------------------------|");
			regLog.append("\n");
			this.debug(regLog.toString());
		}
		return job;
	}
	
	protected abstract void configJob() throws Exception;

}
