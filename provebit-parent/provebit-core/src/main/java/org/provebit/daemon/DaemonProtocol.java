package org.provebit.daemon;

import org.simplesockets.protocol.SimpleSocketsProtocol;

public class DaemonProtocol implements SimpleSocketsProtocol {
	/**
	 * MessageType : Object \ (optional) REPLY : Object
	 * 
	 * START : null \ no reply
	 * STOP : null \ no reply
	 * ADDFILES : List<String> \ no reply
	 * REMOVEFILES : List<String> \ no reply
	 * SETPERIOD : int \ no reply
	 * GETLOG : null \ REPLY : String
	 * @author dan
	 *
	 */
	public enum MessageType {START, STOP, ADDFILES, REMOVEFILES, SETPERIOD, GETLOG, REPLY};
	public class Message<T> {
		public MessageType type;
		public T data;
		
		public Message(MessageType type, T data) {
			this.type = type;
			this.data = data;
		}
	}
	
	@Override
	public Object receive(Object data) {
		Message<String> reply = null;
		Message<?> request = (Message) data;
		// Handle message type
		// Return a reply message if type was getlog
		return reply;
	}

	@Override
	public Object send(Object data) {
		return data;
	}
}
