package net.dstone.batch.common.core;

import java.util.Collection;

import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecution;
import org.springframework.batch.core.job.flow.FlowExecutionException;
import org.springframework.batch.core.job.flow.FlowExecutor;
import org.springframework.batch.core.job.flow.State;

public abstract class AbstractFlow extends BatchBaseObject implements Flow {

	@Override
	public String getName() {
		return this.getName();
	}

	@Override
	public State getState(String stateName) {
		return this.getState(stateName);
	}

	@Override
	public FlowExecution start(FlowExecutor executor) throws FlowExecutionException{
		return this.start(executor);
	}

	@Override
	public FlowExecution resume(String stateName, FlowExecutor executor) throws FlowExecutionException{
		return this.resume(stateName, executor);
	}

	@Override
	public Collection<State> getStates() {
		return this.getStates();
	}

}
