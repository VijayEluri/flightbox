package org.lttng.flightbox.model;

public class FileDescriptor extends SystemResource implements Comparable<FileDescriptor> {

	private int fd;
	private boolean isError;
	private Task owner;
	
	public void setFd(int fd) {
		this.fd = fd;
	}

	public int getFd() {
		return fd;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof FileDescriptor) {
			FileDescriptor p = (FileDescriptor) other;
			if (p.fd == this.fd && p.getStartTime() == this.getStartTime()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.fd + (int)this.getStartTime();
	}

	@Override
	public int compareTo(FileDescriptor o) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;
		if (o == this) return EQUAL;
		if (this.fd < o.fd) return BEFORE;
		if (this.fd > o.fd) return AFTER;
		if (this.getStartTime() < o.getStartTime()) return BEFORE;
		if (this.getStartTime() > o.getStartTime()) return AFTER;
		return EQUAL;
	}

	public void setError(boolean isError) {
		this.isError = isError;
	}

	public boolean isError() {
		return isError;
	}

	public boolean isOpen() {
		return this.getEndTime() < this.getStartTime();
	}

	public void setOwner(Task owner) {
		this.owner = owner;
	}
	
	public Task getOwner() {
		return owner;
	}
	
	@Override
	public String toString() {
		return "fd=" + fd;
	}
}
