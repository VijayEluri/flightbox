package org.lttng.flightbox.io;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.lttng.flightbox.model.SystemModel;
import org.lttng.flightbox.model.Task;

public class TraceEventHandlerModelMeta extends TraceEventHandlerBase {

	private SystemModel model;
	private long nbNotMetaEvent;

	public TraceEventHandlerModelMeta() {
		super();
		hooks.add(new TraceHook("task_state", "process_state"));
		hooks.add(new TraceHook("fd_state", "file_descriptor"));
		hooks.add(new TraceHook("metadata", "core_marker_format"));
		hooks.add(new TraceHook("metadata", "core_marker_id"));
		hooks.add(new TraceHook("vm_state", "vm_map"));
		hooks.add(new TraceHook("irq_state", "idt_table"));
		hooks.add(new TraceHook("irq_state", "interrupt"));
		hooks.add(new TraceHook("softirq_state", "softirq_vec"));
		hooks.add(new TraceHook("syscall_state", "sys_call_table"));
		hooks.add(new TraceHook("module_state", "list_module"));
		hooks.add(new TraceHook("netif_state", "network_ipv4_interface"));
		hooks.add(new TraceHook("swap_state", "statedump_swap_files"));
		hooks.add(new TraceHook("global_state", "statedump_end"));
	}


	@Override
	public void handleInit(TraceReader reader, JniTrace trace) {

	}

	@Override
	public void handleComplete(TraceReader reader) {

	}

	public void handle_fd_state_file_descriptor(TraceReader reader, JniEvent event) {
	}

	public void handle_metadata_core_marker_format(TraceReader reader, JniEvent event) {
	}

	public void handle_metadata_core_marker_id(TraceReader reader, JniEvent event) {
	}

	public void handle_vm_state_vm_map(TraceReader reader, JniEvent event) {
	}

	public void handle_irq_state_idt_table(TraceReader reader, JniEvent event) {
		Long id = (Long) event.parseFieldByName("irq");
		String symbol = (String) event.parseFieldByName("symbol");
		String[] sym = symbol.split("[+]");
		model.getInterruptTable().add(id.intValue(), sym[0]);
	}

	public void handle_irq_state_interrupt(TraceReader reader, JniEvent event) {
	}

	public void handle_softirq_state_softirq_vec(TraceReader reader, JniEvent event) {
		Long id = (Long) event.parseFieldByName("id");
		String symbol = (String) event.parseFieldByName("symbol");
		String[] sym = symbol.split("[+]");
		model.getSoftIRQTable().add(id.intValue(), sym[0]);
	}

	public void handle_syscall_state_sys_call_table(TraceReader reader, JniEvent event) {
		Long id = (Long) event.parseFieldByName("id");
		String symbol = (String) event.parseFieldByName("symbol");
		String[] sym = symbol.split("[+]");
		model.getSyscallTable().add(id.intValue(), sym[0]);
	}

	public void handle_module_state_list_module(TraceReader reader, JniEvent event) {
	}

	public void handle_netif_state_network_ipv4_interface(TraceReader reader, JniEvent event) {
	}

	public void handle_swap_state_statedump_swap_files(TraceReader reader, JniEvent event) {
	}

	public void handle_global_state_statedump_end(TraceReader reader, JniEvent event) {
		reader.cancel();
	}

	public void handle_task_state_process_state(TraceReader reader, JniEvent event) {
		if (model == null)
			return;
		Task task = new Task();
		long eventTs = event.getEventTime().getTime();
		Long pid = (Long) event.parseFieldByName("pid");
		task.setProcessId(pid.intValue());
		task.setCreateTime(eventTs);
		task.setCmd((String) event.parseFieldByName("name"));
		//task.setParentProcess();
		model.addTask(task);
	}

	public void setModel(SystemModel model) {
		this.model = model;
	}

	public SystemModel getModel() {
		return model;
	}

}