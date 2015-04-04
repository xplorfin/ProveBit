package org.provebit.systems.bitcoin.ui.gui;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import javax.swing.SwingUtilities;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.core.Wallet.SendResult;
import org.bitcoinj.core.WalletEventListener;
import org.bitcoinj.core.WalletExtension;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.bitcoinj.utils.Threading;
import org.provebit.systems.bitcoin.BitcoinDirectory;

public class WalletModel extends Observable {

	public static final String WALLET_NAME = "bitcoin";

	// make the wallet work on main Bitcoin network
	private NetworkParameters params;
	private PeerGroup peerGroup;
	private Wallet wallet;
	private WalletAppKit walletGen;
	private WalletEventHandler weventh;
	
	private BlockChain chain;

	private Map<String,WalletExtension> extensions;

	private Date last;
	
	
	public WalletModel() {
		initWallet(WALLET_NAME, BitcoinDirectory.INSTANCE.getRoot());
	}

	private void initWallet(String walletName, File directory) {
		assert(walletName != null);
		if (walletName.toLowerCase().startsWith("testnet")) {
			params = TestNet3Params.get();
			System.out.println(walletName + " is a testnet wallet");
		}
		else {
			params = MainNetParams.get();
		}
		walletGen = new WalletAppKit(params, directory,  walletName);
		
		// configure wallet service
		walletGen.setBlockingStartup(false);

		// and launch
		walletGen.startAsync();
		walletGen.awaitRunning();
		
		// post configuration
		peerGroup = walletGen.peerGroup();
		chain = walletGen.chain();
		peerGroup.setMaxConnections(12);
		wallet = walletGen.wallet();
		wallet.allowSpendingUnconfirmedTransactions();
		weventh = new WalletEventHandler();
		wallet.addEventListener(weventh);
		extensions = wallet.getExtensions();
	}
	
	
	/**
	 * Sends bitcoin
	 * @param btc - amount of bitcoin to send
	 * @param destAddress - what address to send to
	 * @throws AddressFormatException
	 * @throws InsufficientMoneyException
	 */
	public void simpleSendCoins(Coin btc, String destAddress) 
			throws AddressFormatException, InsufficientMoneyException {
		Address destination = new Address(params, destAddress);
		
		// TODO do something about failed sends
		SendResult res = wallet.sendCoins(peerGroup, destination, btc);
		res.broadcastComplete.addListener(new Runnable() {
			@Override
			public void run() {
				// TODO inform UI that transaction is sent
			}
			
		}, Threading.USER_THREAD);
	}
	
	public void proofTX(byte[] hash) {
		if (hash.length != 32) {
			throw new RuntimeException("not a hash");
		}
		
		byte[] embedData = new byte[40];
		byte[] id = null;
		try {
			id = "ProveBit".getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// should not happen
			e.printStackTrace();
		}
		assert(id.length == 8);
		
		// TODO
		
		Transaction dataTx = new Transaction(params);
		dataTx.addOutput(Coin.ZERO, new ScriptBuilder().op(ScriptOpCodes.OP_RETURN).data(hash).build());
	}
	
	private Coin lastBalance = null;
	
	public Coin getBalance() {
		Coin balance = wallet.getBalance();
		return balance;
	}
	
	private Address lastAddress = null;
	
	public Address getReceivingAddress() {
		return wallet.currentReceiveAddress();
	}
	
	private class WalletEventHandler implements WalletEventListener {

		@Override
		public void onKeysAdded(List<ECKey> keys) {
			notifyChange();
		}

		@Override
		public void onCoinsReceived(Wallet wallet, Transaction tx,
				Coin prevBalance, Coin newBalance) {
			// caught by onWalletChanged
		}

		@Override
		public void onCoinsSent(Wallet wallet, Transaction tx,
				Coin prevBalance, Coin newBalance) {
			// caught by onWalletChanged

		}

		@Override
		public void onReorganize(Wallet wallet) {
			// caught by onWalletChanged

		}

		@Override
		public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
			// caught by onWalletChanged

		}

		@Override
		public void onWalletChanged(Wallet wallet) {
			notifyChange();
		}

		@Override
		public void onScriptsAdded(Wallet wallet, List<Script> scripts) {
			notifyChange();
		}
		
	}
	
	private void notifyChange() {
		// for performance reasons we only set changed when a value has actually changed
		boolean diff = false;
		
		Address newAddress = getReceivingAddress();
		if (!newAddress.equals(lastAddress)) {
			diff = true;
		}
		lastAddress = newAddress;
		
		Coin newBalance = getBalance();
		if (!newBalance.equals(lastBalance)) {
			diff = true;
		}
		lastBalance = newBalance;
		
		if (!diff) {
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setChanged();
				notifyObservers();
			}
		});

	}
}
