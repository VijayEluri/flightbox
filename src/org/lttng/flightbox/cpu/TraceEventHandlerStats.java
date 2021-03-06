package org.lttng.flightbox.cpu;

import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.io.EventData;
import org.lttng.flightbox.io.TraceEventHandlerBase;
import org.lttng.flightbox.io.TraceHook;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.statistics.ResourceUsage;

public class TraceEventHandlerStats extends TraceEventHandlerBase {
	
	int count; 
	JniTrace trace;

	HashMap<Long, EventData> cpuHistory;
	ResourceUsage<Long> cpuStats;
	private double start;
	private double end;
	
	public TraceEventHandlerStats() {
		super();
		hooks.add(new TraceHook("kernel", "sched_schedule"));
	}
	
	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {
		this.trace = trace;
		count = 0;
		cpuHistory = new HashMap<Long, EventData>();
		start = (double) trace.getStartTime().getTime();
		end = (double) trace.getEndTime().getTime();
		cpuStats = new ResourceUsage<Long>((long)start, (long)end, 400);
	}
	
	public void handle_kernel_sched_schedule(TraceReader reader, JniEvent event) {
		count++;
		Long cpu = event.getParentTracefile().getCpuNumber();
		long eventTs = event.getEventTime().getTime();
		Long prev_pid = (Long) event.parseFieldByName("prev_pid");
		Long next_pid = (Long) event.parseFieldByName("next_pid");
		
		double t = 0;
		if (cpuHistory.containsKey(cpu)) { // we have a previous event
			t = cpuHistory.get(cpu).getTime();
		} else { // first event for this CPU
			t = start; 
			cpuHistory.put(cpu, new EventData());
		}
		if (prev_pid > 0) {
			cpuStats.addInterval(t, eventTs, cpu, TaskState.USER);
		}
		// update history to keep track of previous event
		cpuHistory.get(cpu).update(event);
	}

	@Override
	public void handleComplete(TraceReader reader) {
		// finish intervals until trace end
		EventData event;
		for(Long cpu: cpuHistory.keySet()) {
			// last event for this CPU
			event = cpuHistory.get(cpu);
			long eventTs = event.getTime();
			Long next_pid = (Long) event.get("next_pid");
			if(next_pid > 0) {
				cpuStats.addInterval(eventTs, end, cpu, TaskState.USER);
			}
		}
	}
	
	public ResourceUsage<Long> getUsageStats() {
		return cpuStats;
	}
}
