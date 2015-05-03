package org.provebit.merkle;

/**
 * Enumeration used to define what hash type the merkle tree is using
 * @author noahmalmed
 *
 */
public enum HashType {
	SHA256, 				// Used for all merkle's outside of bitcoin
	DOUBLE_SHA256			// Used for merkle's inside of bitcoin
}
