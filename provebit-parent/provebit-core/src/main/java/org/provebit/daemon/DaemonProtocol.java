package org.provebit.daemon;

import java.io.Serializable;

import org.simplesockets.protocol.SimpleSocketsProtocol;

public interface DaemonProtocol extends SimpleSocketsProtocol {
	/**
	 * DaemonMessageType : Object \ (optional) REPLY : Object
	 * 
	 * START : null \ no reply
	 * STOP : null \ no reply
	 * ADDFILES : List<String> \ no reply
	 * REMOVEFILES : List<String> \ no reply
	 * SETPERIOD : int \ no reply
	 * GETLOG : null \ REPLY : String
	 *
	 */
	public class DaemonMessage<T> implements Serializable {
		private static final long serialVersionUID = 2515667167455084448L;
		
		public DaemonMessageType type;
		public T data;
		public enum DaemonMessageType {START, STOP, ADDFILES, REMOVEFILES, SETPERIOD, GETLOG, REPLY};
		public DaemonMessage(DaemonMessageType type, T data) {
			this.type = type;
			this.data = data;
		}
	}
	
	public default Object receive(Object data) {
		DaemonMessage<String> reply = null;
		DaemonMessage<?> request = (DaemonMessage<?>) data;
		return handleMessage(request);
	}

	public default Object send(Object data) {
		return data;
	}
	
	abstract DaemonMessage<?> handleMessage(DaemonMessage<?> request);
}
