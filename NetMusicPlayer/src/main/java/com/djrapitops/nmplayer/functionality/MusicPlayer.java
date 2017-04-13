package com.djrapitops.nmplayer.functionality;

import com.djrapitops.nmplayer.fileutils.PlaylistFileManager;
import com.djrapitops.nmplayer.fileutils.TrackFileManager;
import com.djrapitops.nmplayer.messaging.MessageSender;
import com.djrapitops.nmplayer.messaging.Phrase;
import com.djrapitops.nmplayer.functionality.playlist.PlaylistManager;
import com.djrapitops.nmplayer.functionality.utilities.TextUtils;
import com.djrapitops.nmplayer.ui.TrackProgressBar;
import com.djrapitops.nmplayer.ui.Updateable;
import java.io.File;
import java.util.List;
import javafx.beans.Observable;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * This class contains all the logic used to change the playback (sound that is
 * coming out of the speakers). THIS CLASS SHOULD NOT BE INITIALIZED WITH CLASS
 * CONSTRUCTOR, MusicPlayer IS A SINGLETON CLASS, use MusicPlayer.getInstance()
 * instead.
 *
 * The class contains information about the current state of the player, as well
 * as a PlaylistManager
 *
 * @author Rsl1122
 * @see PlaylistManager
 */
public class MusicPlayer {

    private final PlaylistManager playlist;
    private final MessageSender msg;
    private MediaPlayer mp;
    private Updateable progressBar;
    private Updateable ui;

    private Track currentTrack;
    private int currentTrackIndex;
    private String selectedPlaylist;
    private boolean playing;
    private double volume;

    /**
     * Class constructor.
     *
     * Creates a new PlaylistManager and grabs the instance of MessageSender for
     * easier access to sending messages.
     *
     * @see MessageSender
     */
    public MusicPlayer() {
        playlist = new PlaylistManager();
        msg = MessageSender.getInstance();
        selectedPlaylist = "None";
        volume = 0.75;
    }

    /**
     * Method used to start the playback logic.
     *
     * Selects a playlist "all" that contains all the tracks in other playlists
     * & the tracks folder.
     *
     * Sets the initial playing state to false.
     *
     * @throws IllegalStateException If a javafx Application is has not been
     * started yet.
     */
    public void init() throws IllegalStateException {
        selectPlaylist("all");
        playing = false;
    }

    /**
     * This method is used to change the playlist. PlaylistFileManager is used
     * to load the playlist file, and TrackFileManager is used to translate the
     * file contents into Track objects.
     *
     * Then the playlist inside the PlaylistManager will be set as the new List
     * given by TrackFileManager.
     *
     * First track of the playlist will be selected for play.
     *
     * @param playlistName Name of the playlist
     * @throws IllegalStateException If a javafx Application is has not been
     * started yet.
     * @see PlaylistFileManager
     * @see TrackFileManager
     * @see PlaylistManager
     * @see selectTrack
     */
    public void selectPlaylist(String playlistName) throws IllegalStateException {
        selectedPlaylist = playlistName;
        playlist.setPlaylist(TrackFileManager.translateToTracks(PlaylistFileManager.load(selectedPlaylist)));
        currentTrackIndex = 0;
        msg.send(Phrase.SELECTED_PLAYLIST.parse(TextUtils.uppercaseFirst(playlistName)));
        if (playlist.isEmpty()) {
            msg.send(Phrase.PLAYLIST_EMPTY + "");
            return;
        }
        if (!playlist.hasTrack(currentTrack)) {
            selectTrack(playlist.selectTrack(currentTrackIndex));
        }
    }

    /**
     * Used to move to the next track in the playlist.
     *
     * If currentTrack is null (Not initialized) nothing is done. Otherwise the
     * playback is stopped, new Track selected, and then played.
     *
     * @throws IllegalStateException If a javafx Application is has not been
     * started yet.
     * @see selectTrack
     */
    public void nextTrack() throws IllegalStateException {
        if (currentTrack != null) {
            if (playlist.isEmpty()) {
                return;
            }
            mp.stop();
            playing = false;
            selectTrack(currentTrackIndex + 1);
            play();
        }
    }

    /**
     * Used to move to the previous track in the playlist.
     *
     * If currentTrack is null (Not initialized) nothing is done. Otherwise the
     * playback is stopped, new Track selected, and then played.
     *
     * @see selectTrack
     * @throws IllegalStateException If a javafx Application is has not been
     * started yet.
     */
    public void previousTrack() throws IllegalStateException {
        if (currentTrack != null) {
            if (playlist.isEmpty()) {
                return;
            }
            mp.stop();
            playing = false;
            selectTrack(currentTrackIndex - 1);
            play();
        }
    }

    /**
     * Begins/Resumes the playback.
     *
     * If MusicPlayer is null (not initialized) nothing is done. Playing Message
     * will be sent with MessageSender
     *
     * @see MessageSender
     */
    public void play() {
        if (mp != null) {
            playing = true;
            mp.play();
            msg.send(Phrase.NOW_PLAYING.parse(currentTrack.toString()));
        }
    }

    /**
     * Pauses the playback.
     *
     * If MusicPlayer is null (not initialized) nothing is done.
     *
     * Pause Message will be sent with MessageSender
     *
     * @see MessageSender
     */
    public void pause() {
        if (mp != null && playing) {
            playing = false;
            mp.pause();
            msg.send(Phrase.PAUSE + "");
        }
    }

    /**
     * Stops the playback.
     *
     * If MusicPlayer is null (not initialized) nothing is done. Stop Message
     * will be sent with MessageSender
     *
     * @see MessageSender
     */
    public void stop() {
        if (mp != null) {
            playing = false;
            mp.stop();
            msg.send(Phrase.STOP + "");
        }
    }

    /**
     * Used to change the MediaPlayer object to play the Track.
     *
     * Creates a new MediaPlayer object with the filepath inside the Track
     * object, and frees the resources associated with the old MediaPlayer
     * object, if one exists. If the file specified by Track object doesn't
     * exist a message will be sent with MessageSender. After successfully
     * creating the new MediaPlayer object, the currentTrack information will be
     * updated and a select message will be sent with MessageSender
     *
     *
     * @param track Track to be played.
     * @throws IllegalStateException If a javafx Application is has not been
     * started yet.
     * @see MessageSender
     */
    public void selectTrack(Track track) throws IllegalStateException {
        if (track != null && !track.equals(currentTrack)) {
            String mp3FilePath = track.getFilePath();
            File trackFile = new File(mp3FilePath);
            if (!trackFile.exists()) {
                msg.send(Phrase.NONEXISTING_FILE.parse(track.toString()));
                return;
            }
            Media play = new Media(trackFile.toURI().toString());
            if (mp != null) {
                mp.dispose();
            }
            mp = new MediaPlayer(play);
            mp.setVolume(volume);
            mp.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    nextTrack();
                    ui.update();
                }
            });
            mp.currentTimeProperty().addListener((Observable ov) -> {
                progressBar.update();
            });
            mp.setOnReady(() -> {
                progressBar.update();
            });
            currentTrackIndex = playlist.getIndexOf(track);
            currentTrack = track;
//            msg.send(Phrase.SELECTED.parse(currentTrack.toString()));
        }
    }

    public void selectTrack(int i) throws IllegalStateException {
        selectTrack(playlist.selectTrack(i));
    }

    /**
     * Adds a new Track object to the playlist in PlaylistManager.
     *
     * If the track given is null nothing is done. Otherwise the track is added
     * to playlist in PlaylistManager, add message is sent with MessageSender
     * and the new playlist is saved with PlaylistFileManager.
     *
     * @param track Track to add to the playlist.
     * @throws IllegalStateException If a javafx Application is has not been
     * started yet.
     * @see PlaylistManager
     * @see PlaylistFileManager
     * @see MessageSender
     */
    public void addTrackToPlaylist(Track track) throws IllegalStateException {
        if (track == null) {
            return;
        }
        playlist.addTrackToPlaylist(track);
        msg.send(Phrase.ADDED_TRACK.parse(track.getArtist() + " - " + track.getName()));
        PlaylistFileManager.save(playlist.getPlaylist(), selectedPlaylist, true);
    }

    public void removeTrackFromPlaylist(Track track) {
        boolean removingCurrentTrack = playlist.getIndexOf(track) == currentTrackIndex;
        if (removingCurrentTrack) {
            stop();
        }
        playlist.removeTrackFromPlaylist(track);
        msg.send(Phrase.REMOVED_TRACK.parse(track.toString()));
        PlaylistFileManager.save(playlist.getPlaylist(), selectedPlaylist, true);
        if (removingCurrentTrack) {
            selectTrack(currentTrackIndex);
        }
    }

    public double getCurrentTrackProgress() {
        if (mp == null) {
            return 0;
        }
        return mp.getCurrentTime().toSeconds() / mp.getTotalDuration().toSeconds();
    }

    public void setTrackPosition(double d) {
        if (mp == null) {
            return;
        }
        mp.seek(mp.getTotalDuration().multiply(d));
    }

    public void setVolume(double d) {
        volume = d;
        if (mp != null) {
            mp.setVolume(volume);
        }
    }

    public double getVolume() {
        return volume;
    }

    /**
     * Used to grab the MediaPlayer object that is currently handling the
     * playback.
     *
     * @return MediaPlayer that has the current track playback capability.
     */
    public MediaPlayer getMediaPlayer() {
        return mp;
    }

    /**
     * Tells whether or not the MusicPlayer has active playback going on.
     *
     * @return Is sound coming out of the speakers?
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Grab the currently used List of Tracks from PlaylistManager.
     *
     * @return list of tracks.
     * @see PlaylistManager
     */
    public List<Track> getPlaylist() {
        return playlist.getPlaylist();
    }

    public String getSelectedPlaylist() {
        return selectedPlaylist;
    }

    public Track getCurrentTrack() {
        return currentTrack;
    }

    public void setProgressBar(Updateable progressBar) {
        this.progressBar = progressBar;
    }

    public void setEndOfMediaUpdate(Updateable updateable) {
        this.ui = updateable;
    }

    /**
     * Used to get the only instance of the MusicPlayer so that all of it's
     * methods can be accessed easily.
     *
     * @return INSTANCE created in the static class MusicPlayerSingletonHolder
     */
    public static MusicPlayer getInstance() {
        return MusicPlayerSingletonHolder.INSTANCE;
    }

    private static class MusicPlayerSingletonHolder {

        private static final MusicPlayer INSTANCE = new MusicPlayer();
    }
}
