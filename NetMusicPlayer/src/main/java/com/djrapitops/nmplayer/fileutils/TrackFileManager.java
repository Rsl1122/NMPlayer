package com.djrapitops.nmplayer.fileutils;

import com.djrapitops.nmplayer.functionality.Track;
import com.djrapitops.nmplayer.functionality.utilities.TextUtils;
import com.djrapitops.nmplayer.messaging.MessageSender;
import com.djrapitops.nmplayer.messaging.Phrase;
import com.mpatric.mp3agic.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to read the properties of .mp3 files and translating them
 * to Track objects.
 *
 * <p>
 * All the methods are static.
 *
 * @author Rsl1122
 * @see Track
 */
public class TrackFileManager {

    /**
     * Get all of the supported file extensions in a String array.
     *
     * For example ".mp3"
     *
     * @return String array of supported file extensions.
     */
    private static String[] getSupportedFileExtensions() {
        return new String[]{".mp3", ".wav"};
    }

    /**
     * Check if a given file is a supported filetype for a Track.
     *
     * @param file File to check
     * @return Does the file end with a supported file extension?
     */
    public static boolean isSupportedFileType(File file) {
        String fName = file.getName();
        for (String ext : getSupportedFileExtensions()) {
            if (fName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the tracks folder if it doesn't exist and returns it.
     *
     * @return The tracks folder.
     */
    public static File getFolder() {
        File tracksFolder = new File("tracks");
        if (!tracksFolder.exists()) {
            tracksFolder.mkdir();
        }
        return tracksFolder;
    }

    /**
     * Used to turn a List containing absolute file paths to the .mp3 files into
     * a List containing Track objects.
     *
     * @param filepaths List containing absolute file paths.
     * @return a List containing Track objects that include the file
     * information.
     */
    public static List<Track> translateToTracks(List<String> filepaths) {
        List<Track> tracks = new ArrayList<>();
        for (String filepath : filepaths) {
            Track track = processFile(new File(filepath));
            if (track != null) {
                tracks.add(track);
            }
        }
        return tracks;
    }

    /**
     * Used to read the information of a single file and turning it into a Track
     * object.
     *
     * <p>
     * If the given file can not be read, doesn't exist or isn't an mp3-file, a
     * null value will be returned.
     *
     * <p>
     * A message will be sent with MessageSender if file type is wrong.
     *
     * @param file .mp3 file that will be read.
     * @return null or the Track object that contains information of the file.
     * @see MessageSender
     */
    public static Track processFile(File file) {
        if (file == null || !file.exists() || !file.canRead()) {
            return null;
        }
        if (!isSupportedFileType(file)) {
            MessageSender.getInstance().send(Phrase.WRONG_FILETYPE + "");
            return null;
        }
        return new Track(getTrackName(file), getArtist(file), file.getAbsolutePath());
    }

    /**
     * Used to get the Artist from the .mp3 file metadata.
     *
     * If the mp3 does not have a ID3v2 or ID3v1 tag, "Artist" will be returned.
     *
     * @param file .mp3 file to read the ID3 tag from.
     * @return Artist that is defined in the ID3 tag.
     */
    public static String getArtist(File file) {
        String artist = null;
        if (file.getName().endsWith(".mp3")) {
            try {
                Mp3File mp3 = new Mp3File(file);
                final ID3v2 id3v2Tag = mp3.getId3v2Tag();
                if (id3v2Tag != null) {
                    artist = id3v2Tag.getAlbumArtist();
                } else {
                    final ID3v1 id3v1Tag = mp3.getId3v1Tag();
                    if (id3v1Tag != null) {
                        artist = id3v1Tag.getArtist();
                    }
                }
            } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                ErrorManager.toLog(TrackFileManager.class, ex);
            }
        }
        if (artist == null) {
            artist = "";

        }
        if (artist.isEmpty()) {
            if (file.getName().contains(" - ")) {
                artist = file.getName().split(" - ")[0];
            } else {
                artist = "Artist";
            }

        }
        return artist;
    }

    /**
     * Used to get the Track name from the .mp3 file metadata.
     * <p>
     * If the mp3 does not have a ID3v2 or ID3v1 tag, "Track" will be returned.
     *
     * @param file .mp3 file to read the ID3 tag from.
     * @return Track name that is defined in the ID3 tag.
     */
    public static String getTrackName(File file) {
        String title = null;
        if (file.getName().endsWith(".mp3")) {
            try {
                Mp3File mp3 = new Mp3File(file);
                final ID3v2 id3v2Tag = mp3.getId3v2Tag();
                if (id3v2Tag != null) {
                    title = id3v2Tag.getTitle();
                } else {
                    final ID3v1 id3v1Tag = mp3.getId3v1Tag();
                    if (id3v1Tag != null) {
                        title = id3v1Tag.getTitle();
                    }
                }
            } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
                ErrorManager.toLog(TrackFileManager.class, ex);
            }
        }
        if (title == null) {
            title = "";
        }
        if (title.isEmpty()) {
            if (file.getName().contains(" - ")) {
                title = TextUtils.removeExtension(file.getName().split(" - ")[1]);
            } else {
                title = TextUtils.removeExtension(file.getName());
            }
        }
        return title;
    }
}
