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
	public Object receive(byte[] data) {
		Message<?> reply = null;
		Message<?> message = decodeMessage(data);
		// Handle message type
		// Return a reply message if type was getlog
		return reply;
	}

	@Override
	public byte[] send(Object data) {
		return encodeMessage((Message<?>) data);
	}
	
	/**
	 * Encodes the given message as a byte array as follows
	 * byte[0,?] - Message type
	 * byte[?,n] - Payload
	 * @param message
	 * @return
	 */
	private byte[] encodeMessage(Message<?> message) {
		byte[] encoded = null;
		// Encode the message as a byte array
		return encoded;
	}
	
	/**
	 * Decode the given byte array to the appropriate message object
	 * 
	 * @param encoded
	 * @return
	 */
	private Message<?> decodeMessage(byte[] encoded) {
		Message<?> message = null;
		// Decode byte array to message
		return message;
	}

}
