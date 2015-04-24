package com.github.hakko.musiccabinet.service.spotify;

import jahspotify.AbstractConnectionListener;
import jahspotify.ConnectionListener;
import jahspotify.JahSpotify;
import jahspotify.JahSpotify.PlayerStatus;
import jahspotify.services.JahSpotifyService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.github.hakko.musiccabinet.dao.SpotifyDao;
import com.github.hakko.musiccabinet.domain.model.library.SpotifyUser;
import com.github.hakko.musiccabinet.log.Logger;
import com.github.hakko.musiccabinet.util.BlockingRequest;

public class SpotifyService implements ConnectionListener {
	private static final Logger LOG = Logger.getLogger(SpotifyService.class);

	private static boolean initialized = false;

	private SpotifySettingsService settingsService;
	private SpotifyDao spotifyDao;

	private List<ConnectionListener> listeners = new ArrayList<ConnectionListener>();
	private boolean connected;
	private boolean loggedIn;
	private boolean loggingIn;
	private SpotifyUser spotifyUser;
	private ReentrantLock lock = new ReentrantLock(true);

	public boolean lock() {
		try {
			return lock.tryLock(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	public void unlock() {
		if (!lock.isHeldByCurrentThread() || !lock.isLocked()) {
			return;
		}
		lock.unlock();
	}

	public void init() {
		try {
			if (!lock()) {
				throw new RuntimeException(
						"Could not lock to initialize spotify");
			}

			System.err.println("Attempting to initialize spotify.");
			try {
				JahSpotifyService.class.getMethod("initialize", File.class);

			} catch (NoSuchMethodException e) {
				System.err.println("Did not find spotify libraries.");
				initialized = false;
				return;
			}

			final File tempFolder = new File(settingsService.getSpotifyCache());
			if (!tempFolder.exists()) {
				if (!tempFolder.mkdir()) {
					System.err
							.println("Could not find or create spotify cache directory: "
									+ tempFolder.getAbsolutePath());
					return;
				}
			}

			if (!tempFolder.isDirectory()) {
				System.err.println("Spotify cache must be a directory: "
						+ tempFolder.getAbsolutePath());
				return;
			}
			if (!tempFolder.canWrite()) {
				System.err.println("Spotify cache must be a writable: "
						+ tempFolder.getAbsolutePath());
				return;
			}

			final BlockingRequest<Boolean> blockingRequest = new BlockingRequest<Boolean>() {
				@Override
				public void run() {
					try {
						JahSpotifyService.initialize(tempFolder);
						getSpotify().addConnectionListener(SpotifyService.this);
					} catch (UnsatisfiedLinkError e) {
						System.err.println("Unable to find libjahspotify.so.");
						e.printStackTrace();
						finish(Boolean.FALSE);
					}
				}
			};
			AbstractConnectionListener initListener = new AbstractConnectionListener() {
				@Override
				public void initialized(boolean initialized) {
					blockingRequest.finish(initialized);
				}
			};
			registerListener(initListener);
			SpotifyService.initialized = blockingRequest.start();
			removeListener(initListener);
			System.err.println("Callback hit");

			if (settingsService.getSpotifyUserName() != null) {
				System.err.println(settingsService.getSpotifyUserName());
				final SpotifyUser user = spotifyDao
						.getSpotifyUser(settingsService.getSpotifyUserName());
				System.err.println(user);
				if (user != null && user.getBlob() != null) {
					System.err.println("Logging in with existing settings.");

					final BlockingRequest<Boolean> loginRequest = new BlockingRequest<Boolean>() {
						@Override
						public void run() {
							login(user.getUserName(), null, user.getBlob());
						}
					};
					AbstractConnectionListener loginListener = new AbstractConnectionListener() {
						@Override
						public void loggedIn(boolean success) {
							loginRequest.finish(success);
						}
					};

					registerListener(loginListener);
					Boolean loggedIn = loginRequest.start();
					removeListener(loginListener);
					if (loggedIn == null || !loggedIn) {
						System.err.println("Invalid username or password.");
					} else {
						System.err.println("Logged in: " + loggedIn);
					}
				} else if (user != null
						&& settingsService.getSpotifyPassword() != null) {
					System.err
							.println("Logging in with username/password settings.");

					final BlockingRequest<Boolean> loginRequest = new BlockingRequest<Boolean>() {
						@Override
						public void run() {
							login(user.getUserName(),
									settingsService.getSpotifyPassword(), null);
						}
					};
					AbstractConnectionListener loginListener = new AbstractConnectionListener() {
						@Override
						public void loggedIn(boolean success) {
							loginRequest.finish(success);
						}
					};

					registerListener(loginListener);
					Boolean loggedIn = loginRequest.start();
					removeListener(loginListener);
					if (loggedIn == null || !loggedIn) {
						System.err.println("Invalid username or password.");
					} else {
						System.err.println("Logged in: " + loggedIn);
					}

				} else {
					System.err.println("No existing blob.");
				}
			}

			if (initialized) {
				System.err.println("Spotify service initialized.");
			} else {
				System.err.println("Error initializing spotify service.");
			}
		} finally {
			unlock();
		}

	}

	public void setSettingsService(SpotifySettingsService settingsService) {
		this.settingsService = settingsService;
	}

	public void setSpotifyDao(SpotifyDao spotifyDao) {
		this.spotifyDao = spotifyDao;
	}

	public boolean isSpotifyAvailable() {
		if (!initialized) {
			return false;
		}
		return JahSpotifyService.getInstance() != null;
	}

	public PlayerStatus getStatus() {
		return JahSpotifyService.getInstance().createJahSpotify().getStatus();
	}

	public JahSpotify getSpotify() {
		if (!lock.isHeldByCurrentThread()) {
			throw new RuntimeException("getSpotify called without lock");
		}
		return JahSpotifyService.getInstance().createJahSpotify();
	}

	public void registerListener(ConnectionListener connectionListener) {
		synchronized (listeners) {
			if (listeners.contains(connectionListener)) {
				return;
			}
			listeners.add(connectionListener);
		}
	}

	public void removeListener(ConnectionListener connectionListener) {
		synchronized (listeners) {
			listeners.remove(connectionListener);
		}
	}

	@Override
	public void initialized(boolean initialized) {
		synchronized (listeners) {
			for (ConnectionListener listener : listeners) {
				listener.initialized(initialized);
			}
		}
	}

	public void login(String username, String password, String blob) {
		try {
			if (!lock()) {
				return;
			}
			this.getSpotify().login(username, password, blob, true);

		} finally {
			unlock();
		}
	}

	public boolean isLoggedIn() {
		try {
			if (!lock()) {
				return false;
			}
			return getSpotify().isLoggedIn();
		} finally {
			unlock();
		}
	}

	@Override
	public void connected() {
		connected = true;
		synchronized (listeners) {
			for (ConnectionListener listener : listeners) {
				listener.connected();
			}
		}
	}

	@Override
	public void disconnected() {
		connected = false;
		synchronized (listeners) {
			for (ConnectionListener listener : listeners) {
				listener.disconnected();
			}
		}
	}

	@Override
	public void loggedIn(boolean success) {
		try {
			if (!lock()) {
				return;
			}

			loggedIn = success;
			if (loggedIn) {
				spotifyUser = new SpotifyUser(this.getSpotify().getUser());
				spotifyDao.createOrUpdateSpotifyUser(spotifyUser);
			}
			synchronized (listeners) {
				for (ConnectionListener listener : listeners) {
					listener.loggedIn(success);
				}
			}
		} finally {
			unlock();
		}

	}

	@Override
	public void playlistsLoaded(boolean contents) {
		synchronized (listeners) {
			for (ConnectionListener listener : listeners) {
				listener.playlistsLoaded(contents);
			}
		}
	}

	@Override
	public void loggedOut() {
		loggedIn = false;
		synchronized (listeners) {
			for (ConnectionListener listener : listeners) {
				listener.loggedOut();
			}
		}
	}

	@Override
	public void blobUpdated(String blob) {
		try {
			if (lock()) {
				System.err.println("Spotify blob updated to " + blob);
				SpotifyUser spotifyUser = new SpotifyUser(this.getSpotify()
						.getUser());
				spotifyUser.setBlob(blob);
				spotifyDao.createOrUpdateSpotifyUser(spotifyUser);
				synchronized (listeners) {
					for (ConnectionListener listener : listeners) {
						listener.blobUpdated(blob);
					}
				}
			}
		} finally {
			unlock();
		}

	}

	@Override
	public void playTokenLost() {
		synchronized (listeners) {
			for (ConnectionListener listener : listeners) {
				listener.playTokenLost();
			}
		}
	}

	public SpotifyUser getSpotifyUser() {
		return spotifyUser;
	}

	public SpotifySettingsService getSpotifySettingsService() {
		return settingsService;
	}

	public String cleanAlbumName(String name) {
		if (name == null) {
			return "";
		}
		name = name.replaceAll("\\(Deluxe Version\\)", "");
		name = name.replaceAll("\\[Remastered\\]", "");
		return name.trim().toLowerCase();
	}

}
