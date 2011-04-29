package org.lttng.flightbox.dep;

import java.util.TreeSet;

import org.lttng.flightbox.model.Task;
import org.lttng.flightbox.model.Task.TaskState;
import org.lttng.flightbox.model.state.StateInfo;
import org.lttng.flightbox.model.state.SyscallInfo;

public class BlockingItem implements Comparable<BlockingItem> {

	private Task task;
	private StateInfo wakeUp;
	private Task wakeUpTask;
	private SyscallInfo waitingSyscall;
	private long startTime;
	private long endTime;
	
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public StateInfo getWakeUp() {
		return wakeUp;
	}
	public void setWakeUp(StateInfo wakeUp) {
		this.wakeUp = wakeUp;
	}
	public SyscallInfo getWaitingSyscall() {
		return waitingSyscall;
	}
	public void setWaitingSyscall(SyscallInfo waitingSyscall) {
		this.waitingSyscall = waitingSyscall;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public TreeSet<BlockingItem> getChildren(BlockingModel blockingModel) {
		TreeSet<BlockingItem> result = new TreeSet<BlockingItem>();
		if (wakeUp == null || blockingModel == null)
			return result;
		
		if (wakeUp.getTaskState() == TaskState.EXIT) {
			populateSubBlocking(blockingModel, result, wakeUp.getTask());
		} else if (wakeUp.getTaskState() == TaskState.SOFTIRQ) {
			// some fun goes here
		}
		return result;
	}

	public void populateSubBlocking(BlockingModel blockingModel, TreeSet<BlockingItem> subBlock, Task subTask) {
		subBlock.addAll(blockingModel.getBlockingItemsForTask(subTask));
	}
	
	@Override
	public int compareTo(BlockingItem o) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		if (o == this) return EQUAL;
		if (this.startTime < o.startTime) return BEFORE;
		if (this.startTime > o.startTime) return AFTER;
		return EQUAL;
	}
	public void setWakeUpTask(Task wakeUpTask) {
		this.wakeUpTask = wakeUpTask;
	}
	public Task getWakeUpTask() {
		return wakeUpTask;
	}
}
