import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private int playlistId;
    private boolean generated;
    private int totalSongs;
    private List<Song> songs;

    public Playlist(int playlistId, boolean generated) {
        this.playlistId = playlistId;
        this.generated = generated;
        this.songs = new ArrayList<>();
        this.totalSongs = 0;
    }

    public void addSong(Song song) {
        songs.add(song);
        totalSongs = songs.size();
        System.out.println("✅ Song added to playlist: " + song.getTitle());
    }

    public void removeSong(int songId) {
        songs.removeIf(song -> song.getSongId() == songId);
        totalSongs = songs.size();
        System.out.println("✅ Song removed from playlist");
    }

    public void playNext() {
        if (!songs.isEmpty()) {
            songs.get(0).playSong();
        }
    }

    public void reorderSongs() {
        System.out.println("🔄 Η σειρά των τραγουδιών στην playlist άλλαξε επιτυχώς.");
    }

    // Getters & Setters
    public int getPlaylistId() { return playlistId; }
    public boolean isGenerated() { return generated; }
    public void setGenerated(boolean generated) { this.generated = generated; }
    public int getTotalSongs() { return totalSongs; }
    public List<Song> getSongs() { return songs; }
}