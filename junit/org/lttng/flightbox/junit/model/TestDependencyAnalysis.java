package org.lttng.flightbox.junit.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.SortedSet;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.dep.BlockingTaskListener;
import org.lttng.flightbox.dep.BlockingTree;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class TestDependencyAnalysis {

	@Test
	public void testNanosleep() throws JniException {
		String tracePath = new File(Path.getTraceDir(), "sleep-1x-1sec").getPath();
		SystemModel model = new SystemModel();
		BlockingTaskListener listener = new BlockingTaskListener();
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		Task foundTask = model.getLatestTaskByCmdBasename("sleep");
		SortedSet<BlockingTree> taskItems = listener.getBlockingItemsForTask(foundTask);

		assertEquals(1, taskItems.size());
		BlockingTree info = taskItems.first();
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
		model.addTaskListener(listener);

		ModelBuilder.buildFromTrace(tracePath, model);

		// get the last spawned child
		Task foundTask = model.getLatestTaskByCmdBasename("inception");
		SortedSet<BlockingTree> taskItems = listener.getBlockingItemsForTask(foundTask);

		// 100ms + 200ms + 400ms = 700ms
		assertEquals(1, taskItems.size());
		BlockingTree info = taskItems.first();
		double duration = info.getEndTime() - info.getStartTime();
		assertEquals(400000000.0, duration, 10000000.0);

		// verify recovered blocking information
		Task master = foundTask.getParentProcess().getParentProcess();
		SortedSet<BlockingTree> masterItems = listener.getBlockingItemsForTask(master);
		assertEquals(2, masterItems.size());
		for (BlockingTree item: masterItems) {
			assertNotNull(item.getWakeUp());
		}
	}

}
