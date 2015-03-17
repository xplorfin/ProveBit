package org.provebit.wallet;

import java.io.File;
import java.util.List;
import java.util.Map;

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
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.Threading;
import org.provebit.utils.ApplicationDirectory;
import org.provebit.wallet.deterministic.DeterministicExtension;

public class ProveBitWallet {
	
	public static final String WALLET_NAME = "wallet";

	// make the wallet work on main Bitcoin network
	private NetworkParameters params;
	private PeerGroup peerGroup;
	private Wallet wallet;
	private WalletInitializer walletGen;
	private WalletEventHandler weventh;
	private String name;
	
	public DeterministicExtension deterministic;

	private BlockChain chain;
	
	public ProveBitWallet() {
		initWallet(WALLET_NAME, ApplicationDirectory.INSTANCE.getRoot());
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
		walletGen = new WalletInitializer(params, directory,  walletName);
		
		// configure wallet service
		walletGen.setBlockingStartup(false);

		// and launch
		walletGen.startAsync();
		walletGen.awaitRunning();
		
		// post configuration
		name = walletGen.getName();
		peerGroup = walletGen.peerGroup();
		chain = walletGen.chain();
		peerGroup.setMaxConnections(12);
		wallet = walletGen.wallet();
		wallet.allowSpendingUnconfirmedTransactions();
		weventh = new WalletEventHandler();
		wallet.addEventListener(weventh);
		// ensure we have atleast one address
		//if (wallet.getKeychainSize() < 1)
		//	addAddress();
		Map<String,WalletExtension> extensions = wallet.getExtensions();
		deterministic = (DeterministicExtension) extensions.get(DeterministicExtension.getExtensionIDStatic());
		if (walletGen.newWallet)
			extensionsInit();
	}
	
	private void extensionsInit() {
		deterministic.newSeedInit(wallet);
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
	
	private class WalletEventHandler implements WalletEventListener {

		@Override
		public void onKeysAdded(List<ECKey> keys) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCoinsReceived(Wallet wallet, Transaction tx,
				Coin prevBalance, Coin newBalance) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCoinsSent(Wallet wallet, Transaction tx,
				Coin prevBalance, Coin newBalance) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReorganize(Wallet wallet) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onWalletChanged(Wallet wallet) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onScriptsAdded(Wallet wallet, List<Script> scripts) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
