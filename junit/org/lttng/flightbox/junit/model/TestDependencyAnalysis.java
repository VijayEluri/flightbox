package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.dep.BlockingModel;
import org.lttng.flightbox.dep.BlockingStats;
import org.lttng.flightbox.dep.BlockingStatsElement;
import org.lttng.flightbox.dep.BlockingTaskListener;
import org.lttng.flightbox.dep.BlockingItem;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.FileDescriptor;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class TestDependencyAnalysis {

	@Test
	public void testNanosleep() throws JniException {
		String tracePath = new File(Path.getTraceDir(), "sleep-1x-1sec").getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		BlockingModel bm = model.getBlockingModel();
		
		Task foundTask = model.getLatestTaskByCmdBasename("sleep");
		SortedSet<BlockingItem> taskItems = bm.getBlockingItemsForTask(foundTask);

		assertTrue(taskItems.size() >= 1);
		BlockingItem info = taskItems.last();
		double duration = info.getEndTime() - info.getStartTime();
		assertEquals(1000000000.0, duration, 10000000.0);
	}

	@Test
	public void testInception() throws JniException {
		String trace = "inception-3x-100ms";
		File file = new File(Path.getTraceDir(), trace);
		// make sure we have this trace
		assertTrue("Missing trace " + trace, file.isDirectory());

		String tracePath = file.getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		BlockingModel bm = model.getBlockingModel();
		
		// get the last spawned child
		Task foundTask = model.getLatestTaskByCmdBasename("inception");
		SortedSet<BlockingItem> taskItems = bm.getBlockingItemsForTask(foundTask);

		// 100ms + 200ms + 400ms = 700ms
		assertTrue(taskItems.size() >= 1);
		BlockingItem info = taskItems.last();
		double duration = info.getEndTime() - info.getStartTime();
		assertEquals(400000000.0, duration, 10000000.0);

		// verify recovered blocking information
		Task master = foundTask.getParentProcess().getParentProcess();
		SortedSet<BlockingItem> masterItems = bm.getBlockingItemsForTask(master);
		BlockingItem nanoSleep = null, waitPid = null;
		int SYS_NANOSLEEP = 162;
		int SYS_WAITPID = 7;
		for (BlockingItem item: masterItems) {
			assertNotNull(item.getWakeUp());
			if (item.getWaitingSyscall().getSyscallId() == SYS_NANOSLEEP) {
			    nanoSleep = item;
			} else if (item.getWaitingSyscall().getSyscallId() == SYS_WAITPID) {
			    waitPid = item;
			}
		}
		
		assertNotNull(nanoSleep);
		assertNotNull(waitPid);
		double p = 10000000;
		assertEquals(nanoSleep.getDuration(), 100000000, p);
		assertEquals(waitPid.getDuration(), 600000000, p);
	}

	@Test
	public void testRcpHog() throws JniException {
		String trace = "rpc-sleep-100ms";
		File file = new File(Path.getTraceDir(), trace);
		// make sure we have this trace
		assertTrue("Missing trace " + trace, file.isDirectory());

		String tracePath = file.getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		BlockingModel bm = model.getBlockingModel();
		Task foundTask = model.getLatestTaskByCmdBasename("clihog");
		SortedSet<BlockingItem> taskItems = bm.getBlockingItemsForTask(foundTask);
		assertTrue(taskItems.size() >= 1);
		
		BlockingItem read = taskItems.last();
		double p = 10000000;
		assertEquals(read.getDuration(), 100000000, p);
		
		TreeSet<BlockingItem> children = read.getChildren(model);
		assertTrue(children.size() >= 1);
		
		BlockingItem sleep = children.last();
		assertEquals(sleep.getDuration(), 100000000, p);
		
		BlockingStats stats = bm.getBlockingStatsForTask(foundTask);
		HashMap<FileDescriptor, BlockingStatsElement<FileDescriptor>> fdStats = stats.getFileDescriptorStats();
		System.out.println(fdStats);
	}

	@Test
	public void testFDWaitingStats() throws JniException {
		String trace = "dd-100M";
		File file = new File(Path.getTraceDir(), trace);
		// make sure we have this trace
		assertTrue("Missing trace " + trace, file.isDirectory());

		String tracePath = file.getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		listener.setModel(model);
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		BlockingModel bm = model.getBlockingModel();
		Task foundTask = model.getLatestTaskByCmdBasename("dd");
		SortedSet<BlockingItem> taskItems = bm.getBlockingItemsForTask(foundTask);
		BlockingStats stats = bm.getBlockingStatsForTask(foundTask);
		System.out.println(stats.getFileDescriptorStats());
	}
	
}
