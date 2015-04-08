package org.simplesockets.protocol;

public interface SimpleSocketsProtocol {
	/**
	 * Receive callback, called when data is received on a socket
	 * 
	 * @param data - Serialized object received
	 * @return reply object, or null if no reply necessary
	 */
	Object receive(Object data);
	
	/**
	 * Sending callback, called when data is to be sent on socket
	 * 
	 * @param data - Application data to be turned into serializable object for transmission
	 * @return Serializable object send on socket as reply
	 */
	Object send(Object data);
}
