package org.lttng.flightbox.junit.model;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.junit.Test;
import org.lttng.flightbox.io.ModelBuilder;
import org.lttng.flightbox.io.TraceEventHandlerModel;
import org.lttng.flightbox.io.TraceEventHandlerModelMeta;
import org.lttng.flightbox.io.TraceReader;
import org.lttng.flightbox.junit.Path;
import org.lttng.flightbox.model.FileDescriptor;
import org.lttng.flightbox.model.SocketInet;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class TestModelSocket {

	//@Test
	public void testRetreiveSocket() throws JniException {
		String tracePath = new File(Path.getTraceDir(), "rpc-sleep-100ms").getPath();
		SystemModel model = new SystemModel();

		ModelBuilder.buildFromTrace(tracePath, model);

		// the last netcat is the client
		TreeSet<Task> tasksServer = model.getTaskByCmd("srvhog", true);
		assertEquals(1, tasksServer.size());

		TreeSet<Task> tasksClient = model.getTaskByCmd("clihog", true);
		assertEquals(1, tasksClient.size());

		Task server = tasksServer.first();
		Task client = tasksClient.first();

		SocketInet clientSocket = findSocket(client);
		SocketInet serverSocket = findSocket(server);

		assertNotNull(clientSocket);
		assertNotNull(serverSocket);

		assertEquals(9876, clientSocket.getDstPort());
		assertEquals(9876, serverSocket.getSrcPort());
		
		assertTrue(clientSocket.isComplementary(serverSocket));

		assertFalse(clientSocket.isOpen());
		assertFalse(serverSocket.isOpen());

		assertTrue(clientSocket.isClient());
		assertFalse(serverSocket.isClient());
	}
	
	//@Test
	public void testRetreiveSocketMultiThreadServer() throws JniException {
		String tracePath = new File("tests/trace-wk-rpc/").getPath();
		SystemModel model = new SystemModel();

		ModelBuilder.buildFromTrace(tracePath, model);
		
		// one main server thread, two clients and two worker threads
		
		Set<Task> tasks = model.getTaskByCmd("/wk", true);
		assertEquals(5, tasks.size());

		Task[] t = new Task[tasks.size()]; 
		tasks.toArray(t);
		
		Task mainServer = t[0];
		Task client1 = t[1];
		Task thread1 = t[2];
		Task client2 = t[3];
		Task thread2 = t[4];
		
		Set<Task> conn1 = model.findConnectedTask(client1);
		/* FIXME: Because thread1 is the last user of the server socket
		 * this task is the owner of the socket hence mainServer
		 * is not returned in the set. In some situation, we may want
		 * all linked tasks. */
		//assertTrue(conn1.contains(mainServer));
		assertTrue(conn1.contains(thread1));
		
		Set<Task> conn2 = model.findConnectedTask(client2);
		//assertTrue(conn2.contains(mainServer));
		assertTrue(conn2.contains(thread2));
	}
	
	/** 
	 * returns the first defined socket of the task
	 * @param task
	 * @return
	 */
	public SocketInet findSocket(Task task) {
		HashMap<Integer, TreeSet<FileDescriptor>> fds = task.getFileDescriptors();
		SocketInet sock = null;
		for (Integer i : fds.keySet()) {
			FileDescriptor last = fds.get(i).last();
			if (last instanceof SocketInet) {
				SocketInet s = (SocketInet) last;
				if (s.isSet()) {
					sock = s;
					break;
				}
			}
		}
		return sock;
	}

}
