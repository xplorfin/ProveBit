package org.provebit.wallet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.BlockChain;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.DownloadListener;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.PeerEventListener;
import org.bitcoinj.core.Wallet;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStoreException;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.store.WalletProtobufSerializer;
import org.provebit.wallet.deterministic.DeterministicExtension;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;

public class WalletInitializer extends WalletAppKit {
	public boolean newWallet = false;
	private String name;

	public WalletInitializer(NetworkParameters params, File directory,
			String filePrefix) {
		super(params, directory, filePrefix);
	}

	@Override
	protected void startUp() throws Exception {
		// check directory, should be ~/.provebit?
		if (!directory.exists()) {
			if (!directory.mkdir()) {
				throw new IOException("Could not create named directory");
			}
		}
		FileInputStream walletStream = null;
		try {
			File chainFile = new File(directory, filePrefix + ".spvchain");
			boolean chainFileExists = chainFile.exists();
			vWalletFile = new File(directory, filePrefix + ".wallet");
			name = vWalletFile.getCanonicalPath();
			boolean shouldReplayWallet = vWalletFile.exists()
					&& !chainFileExists;

			vStore = new SPVBlockStore(params, chainFile);
			if (!chainFileExists && checkpoints != null) {
				// get earliest key time to create BlockChain Object
				long time = Long.MAX_VALUE;
				if (vWalletFile.exists()) {
					Wallet wallet = new Wallet(params);
					FileInputStream stream = new FileInputStream(vWalletFile);
					// TODO check is this the right way to do
					wallet = new WalletProtobufSerializer().readWallet(stream);
					time = wallet.getEarliestKeyCreationTime();
				}
				CheckpointManager.checkpoint(params, checkpoints, vStore, time);
			}
			vChain = new BlockChain(params, vStore);
			vPeerGroup = createPeerGroup();
			if (this.userAgent != null)
				vPeerGroup.setUserAgent(userAgent, version);
			if (vWalletFile.exists()) {
				walletStream = new FileInputStream(vWalletFile);
				vWallet = new Wallet(params);
				addWalletExtensions(); // All extensions must be present before
										// we deserialize
				// TODO check readWallet
				vWallet = new WalletProtobufSerializer().readWallet(walletStream);
				if (shouldReplayWallet)
					vWallet.clearTransactions(0);
			} else {
				vWallet = new Wallet(params);
				// vWallet.addKey(new ECKey()); Skip adding first wallet key
				// because deterministic key is needed
				addWalletExtensions();
				newWallet = true;
			}
			if (useAutoSave)
				vWallet.autosaveToFile(vWalletFile, 1, TimeUnit.SECONDS, null);
			// Set up peer addresses or discovery first, so if wallet extensions
			// try to broadcast a transaction
			// before we're actually connected the broadcast waits for an
			// appropriate number of connections.
			if (peerAddresses != null) {
				for (PeerAddress addr : peerAddresses)
					vPeerGroup.addAddress(addr);
				peerAddresses = null;
			} else {
				vPeerGroup.addPeerDiscovery(new DnsDiscovery(params));
			}
			vChain.addWallet(vWallet);
			vPeerGroup.addWallet(vWallet);
			onSetupCompleted();

			if (blockingStartup) {
				vPeerGroup.startAsync();
				vPeerGroup.awaitRunning();
				// Make sure we shut down cleanly.
				installShutdownHook();

				// TODO: Be able to use the provided download listener when
				// doing a blocking startup.
				final DownloadListener listener = new DownloadListener();
				vPeerGroup.startBlockChainDownload(listener);
				listener.await();
			} else {
				vPeerGroup.startAsync();
				vPeerGroup.addListener(new Service.Listener() {
					@Override
					public void running() {
						final PeerEventListener l = downloadListener == null ? new DownloadListener()
								: downloadListener;
						vPeerGroup.startBlockChainDownload(l);
					}

					@Override
					public void failed(State from, Throwable failure) {
						throw new RuntimeException(failure);
					}
				}, MoreExecutors.sameThreadExecutor());
			}
		} catch (BlockStoreException e) {
			throw new IOException(e);
		} finally {
			if (walletStream != null)
				walletStream.close();
		}
	}

	protected void addWalletExtensions() throws Exception {
		vWallet.addExtension(new DeterministicExtension());
	}

	private void installShutdownHook() {
		if (autoStop)
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						WalletInitializer.this.stopAsync();
						WalletInitializer.this.awaitTerminated();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
	}

	public String getName() {
		return name;
	}

}
