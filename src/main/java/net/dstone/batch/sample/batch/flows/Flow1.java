package net.dstone.batch.sample.batch.flows;

import org.springframework.stereotype.Component;

import net.dstone.batch.common.annotation.AutoRegFlow;
import net.dstone.batch.common.core.AbstractFlow;

@Component
@AutoRegFlow(parent = "Job1", name = "Flow1", order = 2)
public class Flow1 extends AbstractFlow {

}
