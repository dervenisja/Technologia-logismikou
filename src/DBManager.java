import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private static final String URL = "jdbc:mysql://localhost:3306/PartyAppDB";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 1. Φόρτωση όλων των Τραγουδιών (Songs)
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

    // 2. Φόρτωση όλων των Hosts
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

    // 3. Φόρτωση όλων των Guests
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

    // 4. Φόρτωση όλων των Πάρτι (Parties) και σύνδεση με τους Hosts τους
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
                    if (h.getUserid() == hostId) { // χρήση του getUserid() από τη User κλάση σας
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

                // Αμφίδρομη σύνδεση: Προσθήκη του πάρτι στη λίστα του Host (όπως κάνει ο κώδικάς σας)
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

    // 5. Φόρτωση των Playlists και των Τραγουδιών τους (Σύνδεση Junction Πίνακα Playlist_Songs)
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

                        // Φέρνουμε τα songIds που ανήκουν σε αυτή την playlist από τον πίνακα Playlist_Songs
                        String junctionQuery = "SELECT songId FROM Playlist_Songs WHERE playlistId = " + playlistId;
                        try (Statement stmtJunction = conn.createStatement();
                             ResultSet rsJunction = stmtJunction.executeQuery(junctionQuery)) {
                            while (rsJunction.next()) {
                                int sId = rsJunction.getInt("songId");
                                for (Song song : availableSongs) {
                                    if (song.getSongId() == sId) {
                                        pl.getSongs().add(song); // Προσθήκη του πραγματικού αντικειμένου Song
                                        break;
                                    }
                                }
                            }
                        }
                        // Ενημέρωση του πάρτι με τη σωστή του λίστα
                        party.setPlaylist(pl);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(" Σφάλμα κατά τη φόρτωση των Playlists: " + e.getMessage());
        }
    }

    // 6. Αποθήκευση νέου πάρτι (για τις ενέργειες του Host)
    public static boolean saveParty(Party party) {
        // Αφαιρούμε το partyId από το INSERT για να αναλάβει το AUTO_INCREMENT της MySQL
        String query = "INSERT INTO Party (theme, location, startTime, endTime, status, qrEnabled, hostId) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, party.getTheme());
            pstmt.setString(2, party.getLocation());
            pstmt.setTimestamp(3, Timestamp.valueOf(java.time.LocalDateTime.now()));
            pstmt.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now().plusHours(5)));
            pstmt.setString(5, party.getStatus());
            pstmt.setBoolean(6, party.isQrEnabled());
            pstmt.setInt(7, party.getHost().getUserid());

            pstmt.executeUpdate();

            // ΦΕΡΝΟΥΜΕ ΤΟ ΣΩΣΤΟ ID: Παίρνουμε το ID που παρήγαγε αυτόματα η MySQL
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int assignedId = generatedKeys.getInt(1);
                    party.setPartyId(assignedId); // Ενημερώνουμε το αντικείμενο Java με το σωστό ID της βάσης!
                }
            }

            System.out.println("💾 [MySQL Persistence] Το πάρτι αποθηκεύτηκε επιτυχώς στη MySQL με το σωστό ID: " + party.getPartyId());
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Αποτυχία αποθήκευσης πάρτι στην SQL: " + e.getMessage());
            return false;
        }
    }

    // Αποθήκευση ενός SongRequest στη βάση δεδομένων real-time
    // Αποθήκευση ενός SongRequest στη βάση δεδομένων με AUTO_INCREMENT
    public static boolean saveSongRequest(SongRequest request) {

        String query = "INSERT INTO SongRequest (status, timestamp, queuePosition, guestId, songId, partyId) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, request.getStatus());
            pstmt.setTimestamp(2, Timestamp.valueOf(java.time.LocalDateTime.now()));
            pstmt.setInt(3, request.getQueuePosition());
            pstmt.setInt(4, request.getRequestedBy().getUserid()); // από τη γονική κλάση User
            pstmt.setInt(5, request.getRequestedSong().getSongId());
            pstmt.setInt(6, request.getRequestedParty().getPartyId());

            pstmt.executeUpdate();

            // 2. Παίρνουμε το σωστό, μοναδικό ID που έδωσε η MySQL αυτόματα
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int assignedId = generatedKeys.getInt(1);


                    request.setRequestId(assignedId);
                }
            }
            System.out.println(" Live Σύνδεση: Το αίτημα αποθηκεύτηκε στη MySQL με το σωστό ID: " + request.getRequestId());
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Αποτυχία αποθήκευσης SongRequest στην SQL: " + e.getMessage());
            return false;
        }
    }

    // Αποθήκευση μιας Πληρωμής (Payment) στη βάση δεδομένων real-time
    public static boolean savePayment(Payment payment) {
        String query = "INSERT INTO Payment (amount, paymentDate, paymentStatus, partyId, guestId) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDouble(1, payment.getAmount());
            pstmt.setTimestamp(2, Timestamp.valueOf(payment.getPaymentDate()));
            pstmt.setString(3, payment.getPaymentStatus());
            pstmt.setInt(4, payment.getParty().getPartyId());
            pstmt.setInt(5, payment.getGuest().getUserid());

            pstmt.executeUpdate();
            System.out.println("💾 [MySQL Persistence] Η πληρωμή καταγράφηκε επιτυχώς στη βάση δεδομένων!");
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Αποτυχία αποθήκευσης Payment στην SQL: " + e.getMessage());
            return false;
        }
    }

    // Αποθήκευση της παραχθείσας Playlist και των τραγουδιών της στη MySQL
    public static boolean saveGeneratedPlaylist(Party party) {
        String checkQuery = "SELECT playlistId FROM Playlist WHERE partyId = ?";
        String insertPlaylist = "INSERT INTO Playlist (generated, totalSongs, partyId) VALUES (true, ?, ?)";
        String updatePlaylist = "UPDATE Playlist SET generated = true, totalSongs = ? WHERE partyId = ?";
        String insertJunction = "INSERT INTO Playlist_Songs (playlistId, songId) VALUES (?, ?)";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false); // Έναρξη Transaction για ασφάλεια
            int playlistId = -1;

            // 1. Έλεγχος αν υπάρχει ήδη εγγραφή Playlist για αυτό το πάρτι
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkQuery)) {
                pstmtCheck.setInt(1, party.getPartyId());
                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        playlistId = rs.getInt("playlistId");
                    }
                }
            }

            // 2. Ενημέρωση ή Δημιουργία της Playlist
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

            // 3. Καθαρισμός παλιών τραγουδιών της λίστας (αν υπήρχαν)
            String deleteOld = "DELETE FROM Playlist_Songs WHERE playlistId = ?";
            try (PreparedStatement pstmtDelete = conn.prepareStatement(deleteOld)) {
                pstmtDelete.setInt(1, playlistId);
                pstmtDelete.executeUpdate();
            }

            // 4. Εισαγωγή των νέων επιλεγμένων τραγουδιών στον junction πίνακα
            try (PreparedStatement pstmtJunction = conn.prepareStatement(insertJunction)) {
                for (Song song : party.getPlaylist().getSongs()) {
                    pstmtJunction.setInt(1, playlistId);
                    pstmtJunction.setInt(2, song.getSongId());
                    pstmtJunction.addBatch();
                }
                pstmtJunction.executeBatch();
            }

            conn.commit(); // Οριστικοποίηση αλλαγών στη βάση
            System.out.println("💾 [MySQL Persistence] Η νέα Playlist αποθηκεύτηκε live στη βάση δεδομένων!");
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα κατά την αποθήκευση της Playlist στη MySQL: " + e.getMessage());
            return false;
        }
    }
    // Ενημέρωση της κατάστασης ενός SongRequest (π.χ. από pending σε approved)
    public static boolean updateSongRequestStatus(int requestId, String newStatus) {
        String query = "UPDATE SongRequest SET status = ? WHERE requestId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, requestId);
            pstmt.executeUpdate();
            System.out.println("💾 [MySQL] Η κατάσταση του αιτήματος #" + requestId + " άλλαξε σε: " + newStatus);
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα κατά την ενημέρωση του SongRequest στην SQL: " + e.getMessage());
            return false;
        }
    }

    // Φόρτωση όλων των SongRequests από τη MySQL και σύνδεση με τα αντίστοιχα Parties
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

                // 1. Εύρεση του σωστού Πάρτι
                Party targetParty = null;
                for (Party p : availableParties) {
                    if (p.getPartyId() == partyId) {
                        targetParty = p;
                        break;
                    }
                }

                // 2. Εύρεση του σωστού Guest
                PartyGuest targetGuest = null;
                for (PartyGuest g : availableGuests) {
                    if (g.getUserid() == guestId) {
                        targetGuest = g;
                        break;
                    }
                }

                // 3. Εύρεση του σωστού Τραγουδιού
                Song targetSong = null;
                for (Song s : availableSongs) {
                    if (s.getSongId() == songId) {
                        targetSong = s;
                        break;
                    }
                }

                // Αν βρέθηκαν όλες οι οντότητες, ανακατασκευάζουμε το Request στη μνήμη
                if (targetParty != null && targetGuest != null && targetSong != null) {

                    // ΔΙΟΡΘΩΣΗ: Κλήση του constructor με τα 4 ορίσματα που απαιτεί η Java σου
                    SongRequest request = new SongRequest(requestId, targetSong, targetGuest, targetParty);

                    // Ενημέρωση του status μέσω του Setter που φτιάξαμε
                    request.setStatus(status);

                    // Σύνδεση του request με το πάρτι ώστε να το βλέπει live ο Host
                    targetParty.getSongRequests().add(request);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Σφάλμα κατά τη φόρτωση των SongRequests από τη MySQL: " + e.getMessage());
        }
    }
}