public class Song {
    private int songId;
    private String title;
    private String artist;
    private String genre;
    private int duration;
    private double popularity;

    public Song(int songId, String title, String artist, String genre, int duration, double popularity) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.duration = duration;
        this.popularity = popularity;
    }

    public void playSong() {
        System.out.println("🎵 [UML Sync] Now playing: " + title + " by " + artist + " [" + duration + "s]");
    }

    public void updateMetadata(String newTitle, String newArtist, String newGenre) {
        this.title = newTitle;
        this.artist = newArtist;
        this.genre = newGenre;
    }

    public int getSongId() { return songId; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getGenre() { return genre; }
    public int getDuration() { return duration; }
    public double getPopularity() { return popularity; }
}