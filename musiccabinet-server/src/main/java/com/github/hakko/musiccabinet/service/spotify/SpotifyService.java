package com.github.hakko.musiccabinet.service.spotify;

import jahspotify.AbstractConnectionListener;
import jahspotify.ConnectionListener;
import jahspotify.JahSpotify;
import jahspotify.JahSpotify.PlayerStatus;
import jahspotify.PlaybackListener;
import jahspotify.Search;
import jahspotify.SearchListener;
import jahspotify.SearchResult;
import jahspotify.media.Album;
import jahspotify.media.Artist;
import jahspotify.media.Image;
import jahspotify.media.Link;
import jahspotify.media.Playlist;
import jahspotify.media.Track;
import jahspotify.media.User;
import jahspotify.services.JahSpotifyService;
import jahspotify.services.MediaHelper;

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
		new SpotifyBlockingRequest<Boolean>() {
			public void run() {

				System.err.println("Attempting to initialize spotify.");
				try {
					JahSpotifyService.class.getMethod("initialize", File.class);

				} catch (NoSuchMethodException e) {
					System.err.println("Did not find spotify libraries.");
					initialized = false;
					return;
				}

				final File tempFolder = new File(
						settingsService.getSpotifyCache());
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
							getSpotify().addConnectionListener(
									SpotifyService.this);
						} catch (UnsatisfiedLinkError e) {
							System.err
									.println("Unable to find libjahspotify.so.");
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
							.getSpotifyUser(settingsService
									.getSpotifyUserName());
					System.err.println(user);
					if (user != null && user.getBlob() != null) {
						System.err
								.println("Logging in with existing settings.");

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
										settingsService.getSpotifyPassword(),
										null);
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
				this.finish(Boolean.TRUE);
			}
		}.start();
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

	private JahSpotify getSpotify() {
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

	public void login(final String username, final String password,
			final String blob) {

		new SpotifyBlockingRequest<Boolean>() {

			@Override
			public void run() {
				getSpotify().login(username, password, blob, true);
				this.finish(Boolean.TRUE);

			}

		}.start();
	}

	public boolean isLoggedIn() {
		SpotifyBlockingRequest<Boolean> sbr = new SpotifyBlockingRequest<Boolean>(
				Boolean.FALSE) {

			@Override
			public void run() {
				this.finish(getSpotify().isLoggedIn());
			}
		};
		return sbr.start();
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
	public void loggedIn(final boolean success) {
		new SpotifyBlockingRequest<Boolean>(Boolean.FALSE) {
			@Override
			public void run() {
				loggedIn = success;
				if (loggedIn) {
					spotifyUser = new SpotifyUser(getSpotify().getUser());
					spotifyDao.createOrUpdateSpotifyUser(spotifyUser);
				}
				synchronized (listeners) {
					for (ConnectionListener listener : listeners) {
						listener.loggedIn(success);
					}
				}
				this.finish(Boolean.TRUE);
			}
		}.start();

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
	public void blobUpdated(final String blob) {
		new SpotifyBlockingRequest() {
			@Override
			public void run() {
				LOG.debug("Spotify blob updated to " + blob);
				SpotifyUser spotifyUser = new SpotifyUser(getSpotify()
						.getUser());
				spotifyUser.setBlob(blob);
				spotifyDao.createOrUpdateSpotifyUser(spotifyUser);
				synchronized (listeners) {
					for (ConnectionListener listener : listeners) {
						listener.blobUpdated(blob);
					}
				}
			}
		}.start();

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

	public SearchResult search(final jahspotify.Query query) {
		SpotifyBlockingRequest<SearchResult> sbr = new SpotifyBlockingRequest<SearchResult>() {
			@Override
			public void run() {
				getSpotify().initiateSearch(new Search(query),
						new SearchListener() {
							@Override
							public void searchComplete(SearchResult searchResult) {
								finish(searchResult);
							}
						});
			}
		};
		return sbr.start();
	}

	public Artist readArtist(final Link link) {
		SpotifyBlockingRequest<Artist> sbr = new SpotifyBlockingRequest<Artist>() {
			@Override
			public void run() {
				Artist artist = getSpotify().readArtist(link, true);
				MediaHelper.waitFor(artist, 60);
				finish(artist);
			}
		};
		return sbr.start();
	}

	public Album readAlbum(final Link link) {
		SpotifyBlockingRequest<Album> sbr = new SpotifyBlockingRequest<Album>() {
			@Override
			public void run() {
				Album album = getSpotify().readAlbum(link, true);
				MediaHelper.waitFor(album, 60);
				finish(album);
			}
		};
		return sbr.start();
	}

	public Track readTrack(final Link link) {
		SpotifyBlockingRequest<Track> sbr = new SpotifyBlockingRequest<Track>() {
			@Override
			public void run() {
				Track track = getSpotify().readTrack(link);
				MediaHelper.waitFor(track, 60);
				finish(track);
			}
		};
		return sbr.start();
	}

	public Image readImage(final Link link) {
		SpotifyBlockingRequest<Image> sbr = new SpotifyBlockingRequest<Image>() {
			@Override
			public void run() {
				Image image = getSpotify().readImage(link);
				MediaHelper.waitFor(image, 120);
				finish(image);
			}
		};
		return sbr.start();
	}

	public Playlist readStarredPlaylist(final String username) {
		SpotifyBlockingRequest<Playlist> sbr = new SpotifyBlockingRequest<Playlist>() {
			@Override
			public void run() {

				Playlist starred = getSpotify().readStarredPlaylist(username,
						0, 0);
				MediaHelper.waitFor(starred, 60);
				finish(starred);
			}
		};
		return sbr.start();

	}

	public User getUser() {
		SpotifyBlockingRequest<User> sbr = new SpotifyBlockingRequest<User>() {
			@Override
			public void run() {
				User user = getSpotify().getUser();
				finish(user);
			}
		};
		return sbr.start();

	}

	public void resume() {
		new SpotifyBlockingRequest<Boolean>() {
			@Override
			public void run() {

				getSpotify().resume();
				finish(Boolean.TRUE);
			}
		}.start();
	}

	public void pause() {
		new SpotifyBlockingRequest<Boolean>() {
			@Override
			public void run() {

				getSpotify().pause();
				finish(Boolean.TRUE);
			}
		}.start();
	}

	public void stop() {
		new SpotifyBlockingRequest<Boolean>() {
			@Override
			public void run() {

				getSpotify().stop();
				finish(Boolean.TRUE);
			}
		}.start();
	}

	public void addPlaybackListener(final PlaybackListener playbackListener) {
		new SpotifyBlockingRequest<Boolean>() {
			@Override
			public void run() {

				getSpotify().addPlaybackListener(playbackListener);
				finish(Boolean.TRUE);
			}
		}.start();
	}

	public void play(final Link link) {
		new SpotifyBlockingRequest<Boolean>() {
			@Override
			public void run() {

				getSpotify().play(link);
				finish(Boolean.TRUE);
			}
		}.start();
	}

}
