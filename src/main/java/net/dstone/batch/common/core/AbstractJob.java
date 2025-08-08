package net.dstone.batch.common.core;

import java.util.LinkedList;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public abstract class AbstractJob extends BatchBaseObject{

	@Autowired
	protected JobBuilderFactory jobBuilderFactory;
	@Autowired
	protected StepBuilderFactory stepBuilderFactory;
	
	private String name;
	
	private LinkedList flowList = new LinkedList();
	
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
		Job job = null;
		try {
			String jobName = this.name;
			this.configJob();
			JobBuilder jobBuilder = jobBuilderFactory.get(jobName);
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
						Step step = stepBuilderFactory.get(taskletName).tasklet(tasklet).build();
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
						Step step = stepBuilderFactory.get(taskletName).tasklet(tasklet).build();
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
		}
		return job;
	}
	
	protected abstract void configJob() throws Exception;

}
