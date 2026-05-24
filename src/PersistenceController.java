import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenceController {
    private static final String URL = "jdbc:mysql://localhost:3306/PartyAppDB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static List<Song> loadSongs() {
        List<Song> songsList = new ArrayList<>();
        String query = "SELECT * FROM Song";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                songsList.add(new Song(
                        rs.getInt("songId"),
                        rs.getString("title"),
                        rs.getString("artist"),
                        rs.getString("genre"),
                        rs.getInt("duration"),
                        rs.getDouble("popularity")
                ));
            }
        } catch (SQLException e) {
            System.out.println(" Σφάλμα κατά τη φόρτωση των Songs: " + e.getMessage());
        }
        return songsList;
    }

    public static List<PartyHost> loadHosts() {
        List<PartyHost> hostsList = new ArrayList<>();
        String query = "SELECT u.userId, u.name, u.email, u.password, h.hostRating, h.subscriptionLevel " +
                "FROM User u JOIN PartyHost h ON u.userId = h.userId";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                PartyHost host = new PartyHost(
                        rs.getInt("userId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getDouble("hostRating"),
                        rs.getString("subscriptionLevel")
                );
                host.login(host.getEmail(), rs.getString("password"));
                hostsList.add(host);
            }
        } catch (SQLException e) {
            System.out.println(" Σφάλμα κατά τη φόρτωση των Hosts: " + e.getMessage());
        }
        return hostsList;
    }

    public static List<PartyGuest> loadGuests() {
        List<PartyGuest> guestsList = new ArrayList<>();
        String query = "SELECT u.userId, u.name, u.email, u.password, g.nickname, g.requestLimit " +
                "FROM User u JOIN PartyGuest g ON u.userId = g.userId";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                PartyGuest guest = new PartyGuest(
                        rs.getInt("userId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("nickname"),
                        rs.getInt("requestLimit")
                );
                guest.login(guest.getEmail(), rs.getString("password"));
                guestsList.add(guest);
            }
        } catch (SQLException e) {
            System.out.println(" Σφάλμα κατά τη φόρτωση των Guests: " + e.getMessage());
        }
        return guestsList;
    }

    public static List<Party> loadParties(List<PartyHost> availableHosts) {
        List<Party> partiesList = new ArrayList<>();
        String query = "SELECT * FROM Party";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int hostId = rs.getInt("hostId");
                PartyHost matchingHost = null;
                for (PartyHost h : availableHosts) {
                    if (h.getUserid() == hostId) {
                        matchingHost = h;
                        break;
                    }
                }
                Party party = new Party(
                        rs.getInt("partyId"),
                        rs.getString("theme"),
                        rs.getString("location"),
                        rs.getTimestamp("startTime").toLocalDateTime(),
                        rs.getTimestamp("endTime").toLocalDateTime(),
                        rs.getBoolean("qrEnabled"),
                        matchingHost
                );
                party.setStatus(rs.getString("status"));
                if (matchingHost != null) {
                    matchingHost.getParties().add(party);
                }
                partiesList.add(party);
            }
        } catch (SQLException e) {
            System.out.println(" Σφάλμα κατά τη φόρτωση των Parties: " + e.getMessage());
        }
        return partiesList;
    }

    public static void loadPlaylistsForParties(List<Party> availableParties, List<Song> availableSongs) {
        String query = "SELECT * FROM Playlist";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int partyId = rs.getInt("partyId");
                int playlistId = rs.getInt("playlistId");
                boolean generated = rs.getBoolean("generated");
                for (Party party : availableParties) {
                    if (party.getPartyId() == partyId) {
                        Playlist pl = new Playlist(playlistId, generated);
                        String junctionQuery = "SELECT songId FROM Playlist_Songs WHERE playlistId = " + playlistId;
                        try (Statement stmtJunction = conn.createStatement();
                             ResultSet rsJunction = stmtJunction.executeQuery(junctionQuery)) {
                            while (rsJunction.next()) {
                                int sId = rsJunction.getInt("songId");
                                for (Song song : availableSongs) {
                                    if (song.getSongId() == sId) {
                                        pl.getSongs().add(song);
                                        break;
                                    }
                                }
                            }
                        }
                        party.setPlaylist(pl);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(" Σφάλμα κατά τη φόρτωση των Playlists: " + e.getMessage());
        }
    }

    public static boolean saveParty(Party party) {
        String query = "INSERT INTO Party (theme, location, startTime, endTime, status, qrEnabled, hostId) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, party.getTheme());
            pstmt.setString(2, party.getLocation());
            pstmt.setTimestamp(3, Timestamp.valueOf(party.getStartTime()));
            pstmt.setTimestamp(4, Timestamp.valueOf(party.getEndTime()));
            pstmt.setString(5, party.getStatus());
            pstmt.setBoolean(6, party.isQrEnabled());
            pstmt.setInt(7, party.getHost().getUserid());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    party.setPartyId(generatedKeys.getInt(1));
                }
            }
            System.out.println("💾 [MySQL] Το πάρτι αποθηκεύτηκε επιτυχώς με ID: " + party.getPartyId());
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Αποτυχία αποθήκευσης πάρτι: " + e.getMessage());
            return false;
        }
    }

    public static boolean saveSongRequest(SongRequest request) {
        String query = "INSERT INTO SongRequest (status, timestamp, queuePosition, guestId, songId, partyId) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, request.getStatus());
            pstmt.setTimestamp(2, Timestamp.valueOf(request.getTimestamp()));
            pstmt.setInt(3, request.getQueuePosition());
            pstmt.setInt(4, request.getRequestedBy().getUserid());
            pstmt.setInt(5, request.getRequestedSong().getSongId());
            pstmt.setInt(6, request.getRequestedParty().getPartyId());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    request.setRequestId(generatedKeys.getInt(1));
                }
            }
            System.out.println(" Live Σύνδεση: Το αίτημα αποθηκεύτηκε με ID: " + request.getRequestId());
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Αποτυχία αποθήκευσης SongRequest: " + e.getMessage());
            return false;
        }
    }

    public static boolean savePayment(Payment payment) {
        String query = "INSERT INTO Payment (amount, paymentDate, paymentStatus, partyId, guestId) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, payment.getAmount());
            pstmt.setTimestamp(2, Timestamp.valueOf(payment.getPaymentDate()));
            pstmt.setString(3, payment.getPaymentStatus());
            pstmt.setInt(4, payment.getParty().getPartyId());
            pstmt.setInt(5, payment.getGuest().getUserid());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Αποτυχία αποθήκευσης Payment: " + e.getMessage());
            return false;
        }
    }

    public static boolean saveGeneratedPlaylist(Party party) {
        String checkQuery = "SELECT playlistId FROM Playlist WHERE partyId = ?";
        String insertPlaylist = "INSERT INTO Playlist (generated, totalSongs, partyId) VALUES (true, ?, ?)";
        String updatePlaylist = "UPDATE Playlist SET generated = true, totalSongs = ? WHERE partyId = ?";

        // 👇 ΕΔΩ ΕΓΙΝΕ Η ΑΛΛΑΓΗ: Μπήκε η λέξη IGNORE
        String insertJunction = "INSERT IGNORE INTO Playlist_Songs (playlistId, songId) VALUES (?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            int playlistId = -1;
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkQuery)) {
                pstmtCheck.setInt(1, party.getPartyId());
                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        playlistId = rs.getInt("playlistId");
                    }
                }
            }
            int totalSongsGenerated = party.getPlaylist().getSongs().size();
            if (playlistId == -1) {
                try (PreparedStatement pstmtInsert = conn.prepareStatement(insertPlaylist, Statement.RETURN_GENERATED_KEYS)) {
                    pstmtInsert.setInt(1, totalSongsGenerated);
                    pstmtInsert.setInt(2, party.getPartyId());
                    pstmtInsert.executeUpdate();
                    try (ResultSet generatedKeys = pstmtInsert.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            playlistId = generatedKeys.getInt(1);
                        }
                    }
                }
            } else {
                try (PreparedStatement pstmtUpdate = conn.prepareStatement(updatePlaylist)) {
                    pstmtUpdate.setInt(1, totalSongsGenerated);
                    pstmtUpdate.setInt(2, party.getPartyId());
                    pstmtUpdate.executeUpdate();
                }
            }
            String deleteOld = "DELETE FROM Playlist_Songs WHERE playlistId = ?";
            try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteOld)) {
                pstmtDelete.setInt(1, playlistId);
                pstmtDelete.executeUpdate();
            }
            try (PreparedStatement pstmtJunction = conn.prepareStatement(insertJunction)) {
                for (Song song : party.getPlaylist().getSongs()) {
                    pstmtJunction.setInt(1, playlistId);
                    pstmtJunction.setInt(2, song.getSongId());
                    pstmtJunction.addBatch();
                }
                pstmtJunction.executeBatch();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα κατά την αποθήκευση της Playlist: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateSongRequestStatus(int requestId, String newStatus) {
        String query = "UPDATE SongRequest SET status = ? WHERE requestId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, requestId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα στο updateSongRequestStatus: " + e.getMessage());
            return false;
        }
    }

    public static boolean updatePartyStatus(int partyId, String newStatus) {
        String query = "UPDATE Party SET status = ? WHERE partyId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, partyId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα στο updatePartyStatus: " + e.getMessage());
            return false;
        }
    }
    public static boolean updatePartyDuration(int partyId, java.time.LocalDateTime newEndTime) {
        String query = "UPDATE Party SET endTime = ? WHERE partyId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(newEndTime));
            pstmt.setInt(2, partyId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα SQL στο updatePartyDuration: " + e.getMessage());
            return false;
        }
    }

    public static boolean saveGuestJoin(PartyGuest guest, Party party) {
        System.out.println("💾 [PersistenceController] Καταγραφή συμμετοχής του Guest " + guest.getNickname() + " στο Party #" + party.getPartyId());
        return true;
    }

    // Ενημέρωση των ρυθμίσεων ήχου στον ξεχωριστό πίνακα AudioSettings
    public static boolean updatePartyAudio(int partyId, int volume, int bass, int treble) {

        String query = "INSERT INTO AudioSettings (partyId, volume, bass, treble) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE volume = ?, bass = ?, treble = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Τιμές για το INSERT
            pstmt.setInt(1, partyId);
            pstmt.setInt(2, volume);
            pstmt.setInt(3, bass);
            pstmt.setInt(4, treble);

            // Τιμές για το UPDATE
            pstmt.setInt(5, volume);
            pstmt.setInt(6, bass);
            pstmt.setInt(7, treble);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα SQL στο updatePartyAudio: " + e.getMessage());
            return false;
        }
    }

    // Μέθοδος για ενημέρωση της συνδρομής του Host στη MySQL
    public static boolean updateHostSubscription(int hostId, String newPlan) {

        String query = "UPDATE PartyHost SET subscriptionLevel = ? WHERE userid = ?";

        try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/PartyAppDB", "root", "");
             java.sql.PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newPlan);
            pstmt.setInt(2, hostId); // Προσοχή: βεβαιώσου ότι το Entity σου έχει getHostId()

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0; // Επιστρέφει true αν η ενημέρωση πέτυχε

        } catch (java.sql.SQLException e) {
            System.out.println("❌ Σφάλμα SQL στο updateHostSubscription: " + e.getMessage());
            return false;
        }
    }


    public static void loadAudioSettingsForParties(List<Party> parties) {
        String query = "SELECT * FROM AudioSettings";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int partyId = rs.getInt("partyId");
                int volume = rs.getInt("volume");
                int bass = rs.getInt("bass");
                int treble = rs.getInt("treble");

                // Ψάχνουμε να βρούμε σε ποιο πάρτι ανήκουν αυτές οι ρυθμίσεις
                for (Party party : parties) {
                    if (party.getPartyId() == partyId) {
                        party.updateAudioSettings(volume, bass, treble); // Τα βάζουμε στο Entity
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα κατά τη φόρτωση των AudioSettings: " + e.getMessage());
        }
    }

    public static void loadSongRequestsForParties(List<Party> availableParties, List<PartyGuest> availableGuests, List<Song> availableSongs) {
        String query = "SELECT * FROM SongRequest";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int requestId = rs.getInt("requestId");
                String status = rs.getString("status");
                int guestId = rs.getInt("guestId");
                int songId = rs.getInt("songId");
                int partyId = rs.getInt("partyId");
                Party targetParty = null;
                for (Party p : availableParties) {
                    if (p.getPartyId() == partyId) {
                        targetParty = p;
                        break;
                    }
                }
                PartyGuest targetGuest = null;
                for (PartyGuest g : availableGuests) {
                    if (g.getUserid() == guestId) {
                        targetGuest = g;
                        break;
                    }
                }
                Song targetSong = null;
                for (Song s : availableSongs) {
                    if (s.getSongId() == songId) {
                        targetSong = s;
                        break;
                    }
                }
                if (targetParty != null && targetGuest != null && targetSong != null) {
                    SongRequest request = new SongRequest(requestId, targetSong, targetGuest, targetParty);
                    request.setStatus(status);
                    targetParty.getSongRequests().add(request);
                    targetGuest.getSongRequests().add(request);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα κατά τη φόρτωση των SongRequests: " + e.getMessage());
        }
    }
}