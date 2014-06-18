package com.github.hakko.musiccabinet.service.spotify;

import jahspotify.AbstractConnectionListener;
import jahspotify.ConnectionListener;
import jahspotify.JahSpotify;
import jahspotify.services.JahSpotifyService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

	public void init() {

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
				JahSpotifyService.initialize(tempFolder);
				getSpotify().addConnectionListener(SpotifyService.this);
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
			final SpotifyUser user = spotifyDao.getSpotifyUser(settingsService
					.getSpotifyUserName());
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
				if (!loggedIn) {
					System.err.println("Invalid username or password.");
				} else {
				  System.err.println("Logged in: " + loggedIn);
				}
				
			} else {
				System.err.println("No existing blob.");
			}
		}
		

		if(initialized) {
			System.err.println("Spotify service initialized.");
		} else {
			System.err.println("Error initializing spotify service.");
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

	public JahSpotify getSpotify() {
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
		for (ConnectionListener listener : listeners) {
			listener.initialized(initialized);
		}
	}

	public void login(String username, String password, String blob) {
		this.getSpotify().login(username, password, blob, true);
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
		loggedIn = success;
		if (loggedIn) {
			spotifyUser = new SpotifyUser(this.getSpotify()
					.getUser());
			spotifyDao.createOrUpdateSpotifyUser(spotifyUser);
		}
		synchronized (listeners) {
			for (ConnectionListener listener : listeners) {
				listener.loggedIn(success);
			}
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
		System.err.println("Spotify blob updated to " + blob);
		SpotifyUser spotifyUser = new SpotifyUser(this.getSpotify().getUser());
		spotifyUser.setBlob(blob);
		spotifyDao.createOrUpdateSpotifyUser(spotifyUser);
		synchronized (listeners) {
			for (ConnectionListener listener : listeners) {
				listener.blobUpdated(blob);
			}
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

}
