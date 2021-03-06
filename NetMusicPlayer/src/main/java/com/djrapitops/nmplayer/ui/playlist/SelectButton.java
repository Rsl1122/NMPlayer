package com.djrapitops.nmplayer.ui.playlist;

import com.djrapitops.nmplayer.functionality.MusicPlayer;
import com.djrapitops.nmplayer.functionality.PlaylistManager;
import com.djrapitops.nmplayer.functionality.Track;
import javafx.geometry.Pos;
import javafx.scene.control.Button;

/**
 * This button is used to change the track to the track represented by the
 * UITrack element.
 *
 * @author ristolah
 */
public class SelectButton extends Button {

    /**
     * Constructor for the button. Sets the click event response to play the
     * associated track from Playlist.
     *
     * Will check if the track is already playing, and does nothing if it is.
     *
     * @param uiTrack A UITrack Component to update when the button is pressed.
     * @see MusicPlayer
     * @see UITrack
     * @see PlaylistManager
     */
    public SelectButton(UITrack uiTrack) {
        final MusicPlayer mp = MusicPlayer.getInstance();
        Track track = uiTrack.getTrack();
        if (track.equals(mp.getCurrentTrack())) {
            setStyle("-fx-background-color: #8290ed; -fx-text-fill: White");
        } else {
            setStyle("-fx-background-color: White");
        }
        setAlignment(Pos.CENTER_LEFT);
        setText(uiTrack.getTrack().toString().replace("_", " "));
        setPrefWidth(10000);
        setOnAction(event -> {
            if (!mp.getCurrentTrack().equals(uiTrack.getTrack()) || !mp.isPlaying()) {
                mp.selectTrack(track);
                mp.play();
                uiTrack.update();
            }
        });
    }

}
