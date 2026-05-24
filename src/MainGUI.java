import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainGUI {
    private static List<PartyHost> hosts;
    private static List<PartyGuest> guests;
    private static List<Party> parties;
    private static List<Song> songsLibrary;

    private static PartyController partyController = new PartyController();
    private static AccessController accessController = new AccessController();
    private static RequestController requestController = new RequestController();
    private static PaymentController paymentController = new PaymentController();

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initializeData();
        SwingUtilities.invokeLater(() -> createMainMenu());
    }

    private static void initializeData() {
        System.out.println("Φόρτωση δεδομένων από MySQL...");
        songsLibrary = PersistenceController.loadSongs();
        hosts = PersistenceController.loadHosts();
        guests = PersistenceController.loadGuests();
        parties = PersistenceController.loadParties(hosts);
        PersistenceController.loadPlaylistsForParties(parties, songsLibrary);
        PersistenceController.loadAudioSettingsForParties(parties);
        System.out.println("Τα δεδομένα φορτώθηκαν επιτυχώς!");
    }

    // ==========================================
    // 1. ΤΟ ΚΕΝΤΡΙΚΟ ΠΑΡΑΘΥΡΟ
    // ==========================================
    private static void createMainMenu() {
        JFrame frame = new JFrame("Party App System - Κεντρικό Μενού");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridLayout(4, 1, 10, 10));

        JLabel titleLabel = new JLabel("Καλωσήρθατε στο Party App", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        frame.add(titleLabel);

        JButton hostButton = new JButton("Μενού Διοργανωτή (Host)");
        JButton guestButton = new JButton("Μενού Καλεσμένου (Guest)");
        JButton exitButton = new JButton("Έξοδος");

        frame.add(hostButton);
        frame.add(guestButton);
        frame.add(exitButton);

        exitButton.addActionListener(e -> System.exit(0));
        hostButton.addActionListener(e -> selectHostAndOpenMenu(frame));
        guestButton.addActionListener(e -> selectGuestAndOpenMenu(frame));

        frame.setVisible(true);
    }

    // ==========================================
    // 2. ΕΠΙΛΟΓΗ HOST & ΑΝΟΙΓΜΑ ΜΕΝΟΥ
    // ==========================================
    private static void selectHostAndOpenMenu(JFrame parentFrame) {
        if (hosts == null || hosts.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "Δεν βρέθηκαν Hosts στη βάση!", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Φτιάχνουμε έναν πίνακα με τα ονόματα των Host για το Dropdown
        String[] hostNames = new String[hosts.size()];
        for (int i = 0; i < hosts.size(); i++) {
            hostNames[i] = hosts.get(i).getName();
        }

        // Εμφανίζουμε το Dropdown
        String choice = (String) JOptionPane.showInputDialog(parentFrame, "Επίλεξε το προφίλ σου:", "Είσοδος Host",
                JOptionPane.QUESTION_MESSAGE, null, hostNames, hostNames[0]);

        // Αν επέλεξε κάποιον και πάτησε ΟΚ
        if (choice != null) {
            PartyHost selectedHost = null;
            for (PartyHost h : hosts) {
                if (h.getName().equals(choice)) {
                    selectedHost = h;
                    break;
                }
            }
            openHostMenu(selectedHost);
        }
    }

    // ==========================================
    // 3. ΤΟ ΠΑΡΑΘΥΡΟ ΤΟΥ ΣΥΓΚΕΚΡΙΜΕΝΟΥ HOST
    // ==========================================
    private static void openHostMenu(PartyHost host) {
        JFrame hostFrame = new JFrame("Party App - Πίνακας Ελέγχου Διοργανωτή");
        hostFrame.setSize(650, 450); // Πιο πλατύ παράθυρο για το πλέγμα
        hostFrame.setLocationRelativeTo(null);
        hostFrame.setLayout(new BorderLayout()); // Διαχωρισμός σε Πάνω (Header) και Κέντρο (Grid)

        // --- ΠΑΝΩ ΜΕΡΟΣ: Επικεφαλίδα ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(41, 128, 185)); // Ωραίο Μπλε χρώμα
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10)); // Περιθώρια

        JLabel title = new JLabel(" HOST DASHBOARD: " + host.getName());
        title.setForeground(Color.WHITE); // Λευκά γράμματα
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(title);
        hostFrame.add(headerPanel, BorderLayout.NORTH); // Τοποθέτηση στην κορυφή

        // --- ΚΕΝΤΡΙΚΟ ΜΕΡΟΣ: Πλέγμα Κουμπιών (Tiles) ---
        // 4 γραμμές, 2 στήλες, με κενό 15 pixels ανάμεσα στα κουμπιά!
        JPanel gridPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Εσωτερικό περιθώριο του πίνακα

        // Χρησιμοποιούμε τη νέα βοηθητική μέθοδο για να φτιάξουμε τα κουμπιά
        JButton btn1 = createStyledButton(" 1. Δημιουργία Νέου Πάρτι");
        JButton btn2 = createStyledButton(" 2. Διαχείριση Playlist");
        JButton btn3 = createStyledButton(" 3. Έγκριση Αιτημάτων");
        JButton btn4 = createStyledButton(" 4. Closing Mode");
        JButton btn5 = createStyledButton("️ 5. Παράταση Διάρκειας");
        JButton btn6 = createStyledButton(" 6. Συνδρομή Host");
        JButton btn7 = createStyledButton(" 7. Smart Lighting");
        JButton btn8 = createStyledButton(" 8. Ρυθμίσεις Ήχου");

        // Προσθήκη των κουμπιών στο πλέγμα
        gridPanel.add(btn1); gridPanel.add(btn2);
        gridPanel.add(btn3); gridPanel.add(btn4);
        gridPanel.add(btn5); gridPanel.add(btn6);
        gridPanel.add(btn7); gridPanel.add(btn8);

        hostFrame.add(gridPanel, BorderLayout.CENTER); // Τοποθέτηση στο κέντρο

        // --- ΣΥΝΔΕΣΗ ΤΩΝ ΚΟΥΜΠΙΩΝ (Λειτουργικότητα) ---
        btn1.addActionListener(e -> openCreatePartyForm(host));
        btn2.addActionListener(e -> openPlaylistManager(host));
        btn3.addActionListener(e -> openApproveRequestsForm(host));
        btn4.addActionListener(e -> openClosingModeForm(host));
        btn5.addActionListener(e -> openExtendDurationForm(host));
        btn6.addActionListener(e -> openSubscriptionForm(host));
        btn7.addActionListener(e -> openSmartLightingForm(host));
        btn8.addActionListener(e -> openAudioSettingsForm(host));


        hostFrame.setVisible(true);
    }

    // ==========================================
    // 4. Η ΦΟΡΜΑ ΓΙΑ ΤΙΣ ΡΥΘΜΙΣΕΙΣ ΗΧΟΥ (UCS: Audio Settings)
    // ==========================================
    private static void openAudioSettingsForm(PartyHost host) {
        if (host.getParties().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Δεν έχεις δημιουργήσει κανένα πάρτι ακόμα!", "Προσοχή", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Για απλότητα, επιλέγουμε το πρώτο πάρτι του Host (αν θες βάζουμε και εδώ dropdown!)
        Party party = host.getParties().get(0);

        // Φτιάχνουμε το panel με τα πεδία κειμένου
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField volField = new JTextField(String.valueOf(party.getVolume()));
        JTextField bassField = new JTextField(String.valueOf(party.getBass()));
        JTextField trebleField = new JTextField(String.valueOf(party.getTreble()));

        panel.add(new JLabel("Ένταση (Volume):")); panel.add(volField);
        panel.add(new JLabel("Μπάσα (Bass):"));   panel.add(bassField);
        panel.add(new JLabel("Πρίμα (Treble):")); panel.add(trebleField);

        // Εμφανίζουμε τη φόρμα
        int result = JOptionPane.showConfirmDialog(null, panel, "🎛 Audio Mixer: " + party.getTheme(), JOptionPane.OK_CANCEL_OPTION);

        // Αν πατήσει "ΟΚ"
        if (result == JOptionPane.OK_OPTION) {
            try {
                int vol = Integer.parseInt(volField.getText());
                int bass = Integer.parseInt(bassField.getText());
                int treble = Integer.parseInt(trebleField.getText());

                // 👇 ΒΗΜΑ Γ: ΚΑΛΟΥΜΕ ΤΟΝ CONTROLLER (Η Λογική παραμένει 100% ίδια!)
                partyController.processAudioSettings(party, vol, bass, treble);

                JOptionPane.showMessageDialog(null, "✅ Οι ρυθμίσεις αποθηκεύτηκαν επιτυχώς και στη Βάση!", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "❌ Παρακαλώ βάλε μόνο αριθμούς!", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // 5. ΦΟΡΜΑ ΔΗΜΙΟΥΡΓΙΑΣ ΝΕΟΥ ΠΑΡΤΙ (UCS1)
    // ==========================================
    private static void openCreatePartyForm(PartyHost host) {
        // ΑΛΛΑΓΗ 1: Κάναμε τις γραμμές 3 (αφού βγάλαμε το checkbox)
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JTextField themeField = new JTextField();
        JTextField locationField = new JTextField();
        JTextField durationField = new JTextField("4"); // Προεπιλογή 4 ώρες

        panel.add(new JLabel("Θέμα Πάρτι :"));
        panel.add(themeField);

        panel.add(new JLabel("Τοποθεσία :"));
        panel.add(locationField);

        panel.add(new JLabel("Διάρκεια (ώρες):"));
        panel.add(durationField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Δημιουργία & Άμεση Έναρξη Πάρτι", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String theme = themeField.getText().trim();
                String location = locationField.getText().trim();
                int durationHours = Integer.parseInt(durationField.getText().trim());

                if (theme.isEmpty() || location.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Το θέμα και η τοποθεσία δεν μπορούν να είναι κενά!", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                java.time.LocalDateTime startTime = java.time.LocalDateTime.now();
                java.time.LocalDateTime endTime = startTime.plusHours(durationHours);

                Party newParty = partyController.processCreateParty(host, theme, location, startTime, endTime, true);
                parties.add(newParty);


                newParty.startParty();
                // Ενημερώνουμε και τη βάση ότι το πάρτι είναι πλέον ενεργό ("active")
                PersistenceController.updatePartyStatus(newParty.getPartyId(), "active");

                JOptionPane.showMessageDialog(null, "✅ Το πάρτι '" + theme + "' δημιουργήθηκε και είναι πλέον ΕΝΕΡΓΟ!", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "❌ Η διάρκεια πρέπει να είναι αριθμός (π.χ. 4)!", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // 6. ΦΟΡΜΑ ΔΙΑΧΕΙΡΙΣΗΣ PLAYLIST (UCS2)
    // ==========================================
    private static void openPlaylistManager(PartyHost host) {
        // 1. Έλεγχος αν ο Host έχει δημιουργήσει πάρτι
        if (host.getParties().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Δεν έχεις δημιουργήσει κανένα πάρτι ακόμα.", "Προσοχή", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 👇 ΝΕΟ ΒΗΜΑ: Φτιάχνουμε λίστα με τα πάρτι του Host για να ΔΙΑΛΕΞΕΙΣ
        String[] partyOptions = new String[host.getParties().size()];
        for (int i = 0; i < host.getParties().size(); i++) {
            partyOptions[i] = "Party #" + host.getParties().get(i).getPartyId() + " - " + host.getParties().get(i).getTheme();
        }

        // Εμφάνιση Dropdown για την επιλογή πάρτι
        String selectedPartyText = (String) JOptionPane.showInputDialog(null,
                "Επίλεξε σε ποιο πάρτι θέλεις να διαχειριστείς την Playlist:",
                "Επιλογή Πάρτι",
                JOptionPane.QUESTION_MESSAGE,
                null,
                partyOptions,
                partyOptions[0]);

        // Αν ο χρήστης πατήσει Άκυρο (Cancel), σταματάμε τη μέθοδο
        if (selectedPartyText == null) return;

        // Αντιστοίχιση του κειμένου που επιλέχθηκε με το πραγματικό αντικείμενο Party
        Party selectedParty = null;
        for (Party p : host.getParties()) {
            if (selectedPartyText.contains("Party #" + p.getPartyId())) {
                selectedParty = p;
                break;
            }
        }

        if (selectedParty == null) return;

        // 2. Εμφάνιση των 3 επιλογών με όμορφα κουμπιά (Αφού πλέον ξέρουμε ποιο πάρτι πειράζουμε!)
        String[] options = {"🔀 Αυτόματη Δημιουργία (Shuffle)", "➕ Προσθήκη Τραγουδιού", "➖ Αφαίρεση Τραγουδιού"};
        int subChoice = JOptionPane.showOptionDialog(null,
                "Τι θέλεις να κάνεις με την Playlist του '" + selectedParty.getTheme() + "';",
                "Διαχείριση Playlist - " + selectedParty.getTheme(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        // --- ΥΠΟ-ΕΠΙΛΟΓΗ Α: Αυτόματη Δημιουργία ---
        if (subChoice == 0) {
            selectedParty.generatePlaylist(songsLibrary);
            PersistenceController.saveGeneratedPlaylist(selectedParty);
            JOptionPane.showMessageDialog(null, "✅ Η αυτόματη Playlist για το πάρτι '" + selectedParty.getTheme() + "' δημιουργήθηκε επιτυχώς!", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
        }

        // --- ΥΠΟ-ΕΠΙΛΟΓΗ Β: Προσθήκη Τραγουδιού ---
        else if (subChoice == 1) {
            if (songsLibrary == null || songsLibrary.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Η βιβλιοθήκη τραγουδιών είναι άδεια!", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] sNames = new String[songsLibrary.size()];
            for (int i = 0; i < songsLibrary.size(); i++) {
                sNames[i] = songsLibrary.get(i).getTitle() + " - " + songsLibrary.get(i).getArtist();
            }

            String sChoice = (String) JOptionPane.showInputDialog(null, "Επίλεξε τραγούδι για προσθήκη στο '" + selectedParty.getTheme() + "':", "Προσθήκη Τραγουδιού",
                    JOptionPane.QUESTION_MESSAGE, null, sNames, sNames[0]);

            if (sChoice != null) {
                for (Song s : songsLibrary) {
                    if ((s.getTitle() + " - " + s.getArtist()).equals(sChoice)) {
                        selectedParty.getPlaylist().addSong(s);
                        PersistenceController.saveGeneratedPlaylist(selectedParty);
                        JOptionPane.showMessageDialog(null, "✅ Το τραγούδι προστέθηκε στην Playlist του '" + selectedParty.getTheme() + "'!", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            }
        }

        // --- ΥΠΟ-ΕΠΙΛΟΓΗ Γ: Αφαίρεση Τραγουδιού ---
        else if (subChoice == 2) {
            java.util.List<Song> currentSongs = selectedParty.getPlaylist().getSongs();
            if (currentSongs.isEmpty()) {
                JOptionPane.showMessageDialog(null, "❌ Η playlist του '" + selectedParty.getTheme() + "' είναι ήδη άδεια.", "Προσοχή", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] currentNames = new String[currentSongs.size()];
            for (int i = 0; i < currentSongs.size(); i++) {
                currentNames[i] = currentSongs.get(i).getTitle();
            }

            String remChoice = (String) JOptionPane.showInputDialog(null, "Επίλεξε τραγούδι για αφαίρεση από το '" + selectedParty.getTheme() + "':", "Αφαίρεση Τραγουδιού",
                    JOptionPane.QUESTION_MESSAGE, null, currentNames, currentNames[0]);

            if (remChoice != null) {
                for (Song s : currentSongs) {
                    if (s.getTitle().equals(remChoice)) {
                        selectedParty.getPlaylist().removeSong(s.getSongId());
                        PersistenceController.saveGeneratedPlaylist(selectedParty);
                        JOptionPane.showMessageDialog(null, "✅ Το τραγούδι αφαιρέθηκε από το '" + selectedParty.getTheme() + "'!", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            }
        }
    }
    // ==========================================
    // 7. ΦΟΡΜΑ ΠΡΟΒΟΛΗΣ & ΕΓΚΡΙΣΗΣ ΑΙΤΗΜΑΤΩΝ (UCS4)
    // ==========================================
    private static void openApproveRequestsForm(PartyHost host) {
        // 1. Έλεγχος αν ο Host έχει δημιουργήσει πάρτι
        if (host.getParties().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Δεν έχεις δημιουργήσει κανένα πάρτι ακόμα.", "Προσοχή", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Επιλογή Πάρτι (Όπως κάναμε και στην Playlist)
        String[] partyOptions = new String[host.getParties().size()];
        for (int i = 0; i < host.getParties().size(); i++) {
            partyOptions[i] = "Party #" + host.getParties().get(i).getPartyId() + " - " + host.getParties().get(i).getTheme();
        }

        String selectedPartyText = (String) JOptionPane.showInputDialog(null,
                "Επίλεξε πάρτι για προβολή αιτημάτων:",
                "Επιλογή Πάρτι",
                JOptionPane.QUESTION_MESSAGE, null, partyOptions, partyOptions[0]);

        if (selectedPartyText == null) return;

        Party selectedParty = null;
        for (Party p : host.getParties()) {
            if (selectedPartyText.contains("Party #" + p.getPartyId())) {
                selectedParty = p;
                break;
            }
        }
        if (selectedParty == null) return;

        // 3. Βρίσκουμε ΜΟΝΟ τα αιτήματα που είναι 'pending'
        java.util.List<SongRequest> pendingRequests = new java.util.ArrayList<>();
        for (SongRequest r : selectedParty.getSongRequests()) {
            if (r.getStatus().equalsIgnoreCase("pending")) {
                pendingRequests.add(r);
            }
        }

        // Αν δεν υπάρχουν εκκρεμή αιτήματα
        if (pendingRequests.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Δεν υπάρχουν εκκρεμή αιτήματα (Pending) για το πάρτι '" + selectedParty.getTheme() + "'.", "Ενημέρωση", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 4. Φτιάχνουμε λίστα με τα αιτήματα για να διαλέξει ο Host ποιο θα εγκρίνει
        String[] requestOptions = new String[pendingRequests.size()];
        for (int i = 0; i < pendingRequests.size(); i++) {
            requestOptions[i] = "Από: " + pendingRequests.get(i).getRequestedBy().getNickname() + " 🎵 Τραγούδι: " + pendingRequests.get(i).getRequestedSong().getTitle();
        }

        String selectedReqText = (String) JOptionPane.showInputDialog(null,
                "Επίλεξε ποιο αίτημα θέλεις να ΕΓΚΡΙΝΕΙΣ:",
                "Εκκρεμή Αιτήματα - " + selectedParty.getTheme(),
                JOptionPane.QUESTION_MESSAGE, null, requestOptions, requestOptions[0]);

        if (selectedReqText != null) {
            // Βρίσκουμε το αντικείμενο SongRequest που αντιστοιχεί στην επιλογή
            SongRequest selectedRequest = null;
            for (SongRequest r : pendingRequests) {
                if (selectedReqText.contains(r.getRequestedSong().getTitle()) && selectedReqText.contains(r.getRequestedBy().getNickname())) {
                    selectedRequest = r;
                    break;
                }
            }

            if (selectedRequest != null) {
                // 👇 ΚΑΛΟΥΜΕ ΤΟΝ CONTROLLER! (Αλλάζει το status και ενημερώνει τη MySQL)
                requestController.processApproval(selectedRequest, selectedParty);

                JOptionPane.showMessageDialog(null, "✅ Το αίτημα εγκρίθηκε και το τραγούδι προστέθηκε στην Playlist!", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    // ==========================================
    // 8. ΕΝΕΡΓΟΠΟΙΗΣΗ CLOSING MODE (UCS5)
    // ==========================================
    private static void openClosingModeForm(PartyHost host) {
        // 1. Έλεγχος αν ο Host έχει δημιουργήσει πάρτι
        if (host.getParties().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Δεν έχεις δημιουργήσει κανένα πάρτι ακόμα.", "Προσοχή", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Φτιάχνουμε λίστα με τα πάρτι για να επιλέξει
        String[] partyOptions = new String[host.getParties().size()];
        for (int i = 0; i < host.getParties().size(); i++) {
            partyOptions[i] = "Party #" + host.getParties().get(i).getPartyId() + " - " + host.getParties().get(i).getTheme();
        }

        String selectedPartyText = (String) JOptionPane.showInputDialog(null,
                "Επίλεξε ποιο πάρτι θέλεις να τερματίσεις (Closing Mode):",
                "Επιλογή Πάρτι",
                JOptionPane.QUESTION_MESSAGE, null, partyOptions, partyOptions[0]);

        if (selectedPartyText == null) return; // Πάτησε άκυρο

        Party selectedParty = null;
        for (Party p : host.getParties()) {
            if (selectedPartyText.contains("Party #" + p.getPartyId())) {
                selectedParty = p;
                break;
            }
        }
        if (selectedParty == null) return;

        // 3. ΠΑΡΑΘΥΡΟ ΕΠΙΒΕΒΑΙΩΣΗΣ (Safety Check)
        int confirm = JOptionPane.showConfirmDialog(null,
                "Είσαι σίγουρος ότι θέλεις να ενεργοποιήσεις το Closing Mode για το πάρτι '" + selectedParty.getTheme() + "';\nΑυτή η ενέργεια θα τερματίσει το πάρτι οριστικά.",
                "⚠️ Επιβεβαίωση Τερματισμού",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        // Αν πατήσει "Ναι" (Yes)
        if (confirm == JOptionPane.YES_OPTION) {
            // Καλούμε τον Controller
            partyController.processClosingMode(selectedParty, host);

            JOptionPane.showMessageDialog(null, " Το Closing Mode ενεργοποιήθηκε!\nΤο πάρτι '" + selectedParty.getTheme() + "' τερματίστηκε επιτυχώς.", "Ενημέρωση", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    // ==========================================
    // 9. ΦΟΡΜΑ ΠΑΡΑΤΑΣΗΣ ΔΙΑΡΚΕΙΑΣ ΠΑΡΤΙ (UCS6)
    // ==========================================
    private static void openExtendDurationForm(PartyHost host) {
        // 1. Έλεγχος αν ο Host έχει δημιουργήσει πάρτι
        if (host.getParties().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Δεν έχεις δημιουργήσει κανένα πάρτι ακόμα.", "Προσοχή", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Επιλογή Πάρτι από Dropdown list
        String[] partyOptions = new String[host.getParties().size()];
        for (int i = 0; i < host.getParties().size(); i++) {
            partyOptions[i] = "Party #" + host.getParties().get(i).getPartyId() + " - " + host.getParties().get(i).getTheme();
        }

        String selectedPartyText = (String) JOptionPane.showInputDialog(null,
                "Επίλεξε πάρτι για παράταση διάρκειας:",
                "Επιλογή Πάρτι",
                JOptionPane.QUESTION_MESSAGE, null, partyOptions, partyOptions[0]);

        if (selectedPartyText == null) return; // Πάτησε άκυρο

        Party selectedParty = null;
        for (Party p : host.getParties()) {
            if (selectedPartyText.contains("Party #" + p.getPartyId())) {
                selectedParty = p;
                break;
            }
        }
        if (selectedParty == null) return;

        // 3. Παράθυρο εισαγωγής των έξτρα λεπτών
        String inputMinutes = JOptionPane.showInputDialog(null,
                "Τρέχουσα ώρα λήξης: " + selectedParty.getEndTime() + "\n\nΠόσα έξτρα λεπτά θέλεις να προσθέσεις;",
                "⏱️ Παράταση Διάρκειας",
                JOptionPane.QUESTION_MESSAGE);

        // Αν ο Host έδωσε τιμή και πάτησε OK
        if (inputMinutes != null && !inputMinutes.trim().isEmpty()) {
            try {
                int extraMinutes = Integer.parseInt(inputMinutes.trim());

                if (extraMinutes <= 0) {
                    JOptionPane.showMessageDialog(null, "❌ Παρακαλώ εισάγετε έναν θετικό αριθμό λεπτών!", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                    return;
                }


                partyController.processExtendDuration(selectedParty, extraMinutes);

                // Εμφάνιση Live Update με τη νέα ώρα λήξης
                JOptionPane.showMessageDialog(null,
                        "✅ Η παράταση δόθηκε επιτυχώς!\n⏰ [Live Update] Η νέα ώρα λήξης είναι: " + selectedParty.getEndTime(),
                        "Επιτυχία",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "❌ Παρακαλώ πληκτρολογήστε μόνο αριθμούς (π.χ. 30 ή 60)!", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // ==========================================
    // 10. ΔΙΑΧΕΙΡΙΣΗ ΣΥΝΔΡΟΜΗΣ (UCS7 / UCS11)
    // ==========================================
    private static void openSubscriptionForm(PartyHost host) {
        String currentPlan = host.getSubscriptionLevel();
        if (currentPlan == null || currentPlan.trim().isEmpty()) {
            currentPlan = "Basic";
        }

        String[] availablePlans = {"Basic", "Pro", "Premium"};

        String newPlan = (String) JOptionPane.showInputDialog(null,
                "Τρέχον Πλάνο: " + currentPlan + "\n\nΕπίλεξε το νέο σου πλάνο συνδρομής:",
                " Διαχείριση Συνδρομής",
                JOptionPane.QUESTION_MESSAGE,
                null,
                availablePlans,
                currentPlan);

        if (newPlan != null) {
            if (newPlan.equals(currentPlan)) {
                JOptionPane.showMessageDialog(null, "Έχεις ήδη επιλέξει το πλάνο " + currentPlan + "!", "Ενημέρωση", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            //  Υπολογισμός Τιμής
            double price = 0.0;
            if (newPlan.equals("Pro")) price = 15.0;
            else if (newPlan.equals("Premium")) price = 30.0;
            else price = 0.0; // Το Basic είναι δωρεάν

            //  Παράθυρο Επιβεβαίωσης & Πληρωμής
            String message = "Επιλέξατε το πλάνο: " + newPlan + "\nΚόστος Αναβάθμισης: €" + price + "\n\nΘέλετε να προχωρήσετε στην πληρωμή και να αναβαθμίσετε το λογαριασμό σας;";
            int confirm = JOptionPane.showConfirmDialog(null, message, "💳 Επιβεβαίωση Πληρωμής", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            // Αν πατήσει "Ναι"
            if (confirm == JOptionPane.YES_OPTION) {
                // Καλούμε τον Controller
                partyController.processSubscription(host, newPlan);

                JOptionPane.showMessageDialog(null, "✅ Η πληρωμή ολοκληρώθηκε επιτυχώς!\nΝέο Πλάνο: " + newPlan, "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "❌ Η αναβάθμιση ακυρώθηκε.", "Ακύρωση", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    // ==========================================
    // 11. ΣΥΓΧΡΟΝΙΣΜΟΣ ΦΩΤΙΣΜΟΥ (Smart Lighting)
    // ==========================================
    private static void openSmartLightingForm(PartyHost host) {
        if (host.getParties().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Δεν έχεις δημιουργήσει κανένα πάρτι ακόμα.", "Προσοχή", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] partyOptions = new String[host.getParties().size()];
        for (int i = 0; i < host.getParties().size(); i++) {
            partyOptions[i] = "Party #" + host.getParties().get(i).getPartyId() + " - " + host.getParties().get(i).getTheme();
        }

        String selectedPartyText = (String) JOptionPane.showInputDialog(null,
                "Επίλεξε πάρτι για συγχρονισμό των φώτων:",
                "💡 Smart Lighting",
                JOptionPane.QUESTION_MESSAGE, null, partyOptions, partyOptions[0]);

        if (selectedPartyText == null) return;

        Party selectedParty = null;
        for (Party p : host.getParties()) {
            if (selectedPartyText.contains("Party #" + p.getPartyId())) {
                selectedParty = p;
                break;
            }
        }
        if (selectedParty == null) return;


        final Party finalParty = selectedParty;

        // ΔΗΜΙΟΥΡΓΙΑ ΤΟΥ ΠΑΡΑΘΥΡΟΥ ΓΙΑ ΤΟ ΕΦΕ
        JDialog effectDialog = new JDialog((Frame)null, "Σύνδεση με Φώτα (DMX)...", true);
        effectDialog.setSize(400, 250);
        effectDialog.setLocationRelativeTo(null);
        effectDialog.setLayout(new BorderLayout());

        JLabel statusLabel = new JLabel("Ανάλυση ρυθμού & Αποστολή σημάτων...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        effectDialog.add(statusLabel, BorderLayout.NORTH);

        JPanel lightPanel = new JPanel();
        lightPanel.setBackground(Color.BLACK);
        effectDialog.add(lightPanel, BorderLayout.CENTER);

        Color[] discoColors = {Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.YELLOW, Color.CYAN, Color.ORANGE};

        // Ο TIMER ΠΟΥ ΑΛΛΑΖΕΙ ΤΑ ΧΡΩΜΑΤΑ
        javax.swing.Timer timer = new javax.swing.Timer(250, new java.awt.event.ActionListener() {
            int counter = 0;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                lightPanel.setBackground(discoColors[counter % discoColors.length]);
                counter++;

                if (counter >= 12) {
                    ((javax.swing.Timer)e.getSource()).stop();
                    effectDialog.dispose();


                    JOptionPane.showMessageDialog(null,
                            "✅ Ο συγχρονισμός DMX ολοκληρώθηκε!\nΤα φώτα πλέον ακολουθούν το ρυθμό του πάρτι: '" + finalParty.getTheme() + "'.",
                            "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        timer.start();
        effectDialog.setVisible(true);
    }

    //=======================================================================================================================================================
    // 12. ΕΠΙΛΟΓΗ GUEST ΚΑΙ ΑΝΟΙΓΜΑ ΜΕΝΟΥ
    // ======================================================================================================================================================

    private static void selectGuestAndOpenMenu(JFrame parentFrame) {
        if (guests == null || guests.isEmpty()) {
            JOptionPane.showMessageDialog(parentFrame, "Δεν βρέθηκαν Guests στη βάση!", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Φτιάχνουμε τον πίνακα με τα ονόματα των Guests
        String[] guestNames = new String[guests.size()];
        for (int i = 0; i < guests.size(); i++) {
            guestNames[i] = guests.get(i).getName() + " (" + guests.get(i).getNickname() + ")";
        }

        // Εμφανίζουμε το Dropdown
        String choice = (String) JOptionPane.showInputDialog(parentFrame, "Επίλεξε το προφίλ σου:", "Είσοδος Guest",
                JOptionPane.QUESTION_MESSAGE, null, guestNames, guestNames[0]);

        if (choice != null) {
            PartyGuest selectedGuest = null;
            for (PartyGuest g : guests) {
                if (choice.contains(g.getNickname())) {
                    selectedGuest = g;
                    break;
                }
            }
            if (selectedGuest != null) {
                openGuestMenu(selectedGuest);
            }
        }
    }

    // ==========================================
    // 13. ΤΟ ΝΕΟ ΠΑΡΑΘΥΡΟ DASHBOARD ΤΟΥ GUEST
    // ==========================================
    private static void openGuestMenu(PartyGuest guest) {
        JFrame guestFrame = new JFrame("Party App - Πίνακας Ελέγχου Καλεσμένου");
        guestFrame.setSize(500, 350);
        guestFrame.setLocationRelativeTo(null);
        guestFrame.setLayout(new BorderLayout());

        // --- ΠΑΝΩ ΜΕΡΟΣ: Επικεφαλίδα (Πράσινο χρώμα) ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(39, 174, 96)); // Όμορφο Πράσινο
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JLabel title = new JLabel(" GUEST DASHBOARD: " + guest.getNickname());
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(title);
        guestFrame.add(headerPanel, BorderLayout.NORTH);

        // --- ΚΕΝΤΡΙΚΟ ΜΕΡΟΣ: 3 Κουμπιά ---
        JPanel gridPanel = new JPanel(new GridLayout(3, 1, 15, 15));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnJoin = createStyledButton(" 1. Είσοδος σε Πάρτι & Πληρωμή (Join)");
        JButton btnRequest = createStyledButton(" 2. Σκανάρισμα QR & Αίτημα Τραγουδιού");
        JButton btnView = createStyledButton(" 3. Προβολή των Αιτημάτων μου");

        gridPanel.add(btnJoin);
        gridPanel.add(btnRequest);
        gridPanel.add(btnView);

        guestFrame.add(gridPanel, BorderLayout.CENTER);

        btnJoin.addActionListener(e -> openJoinPartyForm(guest));
        btnRequest.addActionListener(e -> openQRRequestForm(guest));
        btnView.addActionListener(e -> openViewRequestsForm(guest));

        guestFrame.setVisible(true);
    }

    // ==========================================
    // 14. ΕΙΣΟΔΟΣ & ΠΛΗΡΩΜΗ (UCS12) - ΜΟΝΟ ΣΕ ACTIVE ΠΑΡΤΙ
    // ==========================================
    private static void openJoinPartyForm(PartyGuest guest) {

        java.util.List<Party> activeParties = new java.util.ArrayList<>();
        for (Party p : parties) {
            if ("active".equalsIgnoreCase(p.getStatus())) {
                activeParties.add(p);
            }
        }

        // Αν η λίστα με τα ενεργά είναι άδεια
        if (activeParties.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Δεν υπάρχουν ενεργά πάρτι αυτή τη στιγμή για να συμμετάσχεις. Δοκίμασε ξανά αργότερα!", "Ενημέρωση", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Φτιάχνουμε λίστα για το Dropdown ΜΟΝΟ με τα ενεργά πάρτι
        String[] partyOptions = new String[activeParties.size()];
        for (int i = 0; i < activeParties.size(); i++) {
            partyOptions[i] = "Party #" + activeParties.get(i).getPartyId() + " - " + activeParties.get(i).getTheme();
        }

        String choice = (String) JOptionPane.showInputDialog(null,
                "Επίλεξε σε ποιο ΕΝΕΡΓΟ πάρτι θέλεις να μπεις:",
                " Είσοδος σε Πάρτι",
                JOptionPane.QUESTION_MESSAGE, null, partyOptions, partyOptions[0]);

        if (choice != null) {
            Party targetParty = null;
            // Ψάχνουμε ΜΕΣΑ στα ενεργά πάρτι
            for (Party p : activeParties) {
                if (choice.contains("Party #" + p.getPartyId())) {
                    targetParty = p;
                    break;
                }
            }

            if (targetParty != null) {
                // Παράθυρο Επιβεβαίωσης Πληρωμής
                int confirm = JOptionPane.showConfirmDialog(null,
                        "Απαιτείται πληρωμή εισόδου €15 για το πάρτι: " + targetParty.getTheme() + "\n\nΘέλετε να προχωρήσετε;",
                        "💳 Πληρωμή Εισόδου",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    if (paymentController.processPayment(guest, targetParty, 15.0)) {
                        accessController.processJoinParty(guest, targetParty);
                        JOptionPane.showMessageDialog(null, "✅ Η πληρωμή ολοκληρώθηκε! Έγινες μέλος στο πάρτι.", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "❌ Σφάλμα κατά την πληρωμή.", "Αποτυχία", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    // ==========================================
    // 15. ΣΚΑΝΑΡΙΣΜΑ QR & ΑΙΤΗΜΑ ΤΡΑΓΟΥΔΙΟΥ (UCS8 & UCS9)
    // ==========================================
    private static void openQRRequestForm(PartyGuest guest) {
        if (guest.getJoinedParties().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Πρέπει πρώτα να πληρώσεις είσοδο (Επιλογή 1) σε κάποιο πάρτι!", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Για απλότητα, παίρνουμε το 1ο πάρτι που έχει κάνει join
        Party party = guest.getJoinedParties().get(0);

        // ΒΗΜΑ 1: Προσομοίωση σκαναρίσματος QR Code
        String inputId = JOptionPane.showInputDialog(null,
                "📷 ΣΚΑΝΑΡΙΣΜΑ QR CODE\n\nΒρίσκεσαι στο πάρτι: " + party.getTheme() + "\nΠληκτρολόγησε το ID του πάρτι που βλέπεις στην οθόνη του μαγαζιού:",
                "Επιβεβαίωση Παρουσίας", JOptionPane.QUESTION_MESSAGE);

        if (inputId != null && !inputId.trim().isEmpty()) {
            try {
                int qrPartyId = Integer.parseInt(inputId.trim());
                QRCode qr = new QRCode(1, "QR_VALID_" + qrPartyId, 24, qrPartyId);

                // ΕΛΕΓΧΟΣ QR ΜΕΣΩ CONTROLLER
                if (accessController.processQRCode(guest, qr, party)) {
                    JOptionPane.showMessageDialog(null, "✅ Επιβεβαίωση φυσικής παρουσίας! Το QR Code είναι έγκυρο.", "QR Success", JOptionPane.INFORMATION_MESSAGE);

                    // ΒΗΜΑ 2: Υποβολή Αιτήματος Τραγουδιού
                    if (songsLibrary.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Δεν υπάρχουν διαθέσιμα τραγούδια.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String[] songOptions = new String[songsLibrary.size()];
                    for (int i = 0; i < songsLibrary.size(); i++) {
                        songOptions[i] = songsLibrary.get(i).getTitle() + " - " + songsLibrary.get(i).getArtist();
                    }

                    String songChoice = (String) JOptionPane.showInputDialog(null,
                            "Επίλεξε ποιο τραγούδι θέλεις να ζητήσεις από τον DJ:",
                            "🎵 Αίτημα Τραγουδιού",
                            JOptionPane.QUESTION_MESSAGE, null, songOptions, songOptions[0]);

                    if (songChoice != null) {
                        Song selectedSong = null;
                        for (Song s : songsLibrary) {
                            if (songChoice.equals(s.getTitle() + " - " + s.getArtist())) {
                                selectedSong = s;
                                break;
                            }
                        }

                        if (selectedSong != null) {
                            // ΑΠΟΣΤΟΛΗ ΑΙΤΗΜΑΤΟΣ ΜΕΣΩ CONTROLLER
                            requestController.processSongRequest(guest, selectedSong, party);
                            JOptionPane.showMessageDialog(null, "✅ Το αίτημα στάλθηκε στον Host! Αναμονή έγκρισης...", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "❌ Άκυρο QR Code! Φαίνεται να μην βρίσκεσαι στον χώρο του πάρτι.", "Αποτυχία", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Παρακαλώ εισάγετε σωστό αριθμό ID.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // 16. ΠΡΟΒΟΛΗ ΑΙΤΗΜΑΤΩΝ
    // ==========================================
    private static void openViewRequestsForm(PartyGuest guest) {
        java.util.List<SongRequest> reqs = guest.getSongRequests();

        if (reqs.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Δεν έχεις κάνει κανένα αίτημα τραγουδιού ακόμα.", "Πληροφορία", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("Τα Αιτήματά σου:\n\n");
        for (SongRequest r : reqs) {
            String emoji = r.getStatus().equalsIgnoreCase("pending") ? "⏳ " : (r.getStatus().equalsIgnoreCase("approved") ? "✅ " : "❌ ");
            sb.append(emoji).append(r.getRequestedSong().getTitle())
                    .append(" [ Κατάσταση: ").append(r.getStatus().toUpperCase()).append(" ]\n");
        }

        JOptionPane.showMessageDialog(null, sb.toString(), "Ιστορικό Αιτημάτων", JOptionPane.INFORMATION_MESSAGE);
    }

    // ==========================================
    // ΒΟΗΘΗΤΙΚΗ ΜΕΘΟΔΟΣ ΓΙΑ ΟΜΟΡΦΑ ΚΟΥΜΠΙΑ
    // ==========================================
    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBackground(new Color(236, 240, 241));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}