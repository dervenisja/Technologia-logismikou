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
        this.totalSongs = 0;
        this.songs = new ArrayList<>();
    }

    public void addSong(Song song) {
        // Έλεγχος αν το τραγούδι υπάρχει ήδη στη λίστα
        for (Song s : songs) {
            if (s.getSongId() == song.getSongId()) {
                System.out.println("⚠️ Το τραγούδι '" + song.getTitle() + "' υπάρχει ήδη στην playlist!");
                return; // Σταματάει εδώ, δεν το ξαναπροσθέτει
            }
        }

        songs.add(song);
        totalSongs = songs.size();
        System.out.println("✅ [UML Sync] Το τραγούδι προστέθηκε στην playlist: " + song.getTitle());
    }

    public void removeSong(int songId) {
        songs.removeIf(s -> s.getSongId() == songId);
        totalSongs = songs.size();
    }

    public void playNext() {
        if (!songs.isEmpty()) {
            songs.get(0).playSong();
        }
    }

    public void reorderSongs() {
        System.out.println("🔄 [UML Sync] Έγινε αναδιάταξη της λίστας.");
    }

    public int getPlaylistId() { return playlistId; }
    public boolean isGenerated() { return generated; }
    public void setGenerated(boolean generated) { this.generated = generated; }
    public int getTotalSongs() { return totalSongs; }
    public List<Song> getSongs() { return songs; }
}