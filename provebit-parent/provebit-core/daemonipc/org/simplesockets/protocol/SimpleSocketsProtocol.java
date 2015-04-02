package org.simplesockets.protocol;

public interface SimpleSocketsProtocol {
	/**
	 * Receive callback, called when data is received on a socket
	 * 
	 * @param data - byte array representing data received
	 * @return reply object, or null if no reply necessary
	 */
	Object receive(byte[] data);
	
	/**
	 * Sending callback, called when data is to be sent on socket
	 * 
	 * @param data - Application data to be turned into a byte array to be sent on socket
	 * @return byte[] representation of Object to send on socket
	 */
	byte[] send(Object data);
}
