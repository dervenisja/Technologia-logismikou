-- 1. Καθαρισμός παλιών δεδομένων με ασφαλή τρόπο (DELETE αντί για TRUNCATE)
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM Playlist_Songs;
DELETE FROM Playlist;
DELETE FROM Party;
DELETE FROM Subscription;
DELETE FROM PartyGuest;
DELETE FROM PartyHost;
DELETE FROM Song;
DELETE FROM User;

-- 2. Μηδενισμός των μετρητών ID (Auto Increment) ώστε να ξεκινήσουν από το 1
ALTER TABLE User AUTO_INCREMENT = 1;
ALTER TABLE Party AUTO_INCREMENT = 1;
ALTER TABLE Playlist AUTO_INCREMENT = 1;
ALTER TABLE Song AUTO_INCREMENT = 1;
SET FOREIGN_KEY_CHECKS = 1;

-- 3. Εισαγωγή Χρηστών (Hosts και Guests)
INSERT INTO User (userId, name, email, password) VALUES 
(1, 'Nick Dimitriou', 'nick@partyapp.com', 'pass123'),
(2, 'Anna Papadopoulou', 'anna@partyapp.com', 'pass456'),
(3, 'Maria Papadopoulou', 'maria@email.com', 'pass789'),
(4, 'Giorgos Georgiou', 'giorgos@email.com', 'pass101'),
(5, 'Katerina Lanara', 'katerina@email.com', 'pass202'),
(6, 'Dimitris Alexiou', 'dimitris@email.com', 'pass303');

-- 4. Σύνδεση με PartyHosts
INSERT INTO PartyHost (userId, hostRating, hostedPartiesCount, subscriptionLevel) VALUES 
(1, 4.8, 1, 'Premium'),
(2, 4.5, 1, 'Basic');

-- 5. Σύνδεση με Subscriptions
INSERT INTO Subscription (subscriptionId, planType, status, monthlyCost, hostId) VALUES 
(1, 'Premium', 'active', 15.00, 1),
(2, 'Basic', 'active', 0.00, 2);

-- 6. Σύνδεση με PartyGuests
INSERT INTO PartyGuest (userId, nickname, requestLimit) VALUES 
(3, 'Mariella', 5),
(4, 'Geo', 3),
(5, 'Kat', 4),
(6, 'Jim_Tech', 3);

-- 7. Εμπλουτισμένη Βιβλιοθήκη Τραγουδιών (Καλύπτει όλα τα genres του κώδικά σου!)
INSERT INTO Song (songId, title, artist, genre, duration, popularity) VALUES 
(1, 'Blinding Lights', 'The Weeknd', 'Pop', 200, 92.5),
(2, 'Levitating', 'Dua Lipa', 'Pop', 210, 89.0),
(3, 'Stay', 'Kid Laroi', 'Hip Hop', 190, 88.0),
(4, 'Strobe', 'Deadmau5', 'EDM / House', 380, 85.0),
(5, 'One More Time', 'Daft Punk', 'EDM / House', 245, 91.0),
(6, 'Coffee Breath', 'Sofia Mills', 'Lo-fi / Lounge', 185, 78.5),
(7, 'Midnight City', 'M83', 'Electronic', 243, 86.0),
(8, 'Animals', 'Martin Garrix', 'EDM / House', 304, 89.5);

-- 8. Εισαγωγή Πάρτι
INSERT INTO Party (partyId, theme, location, startTime, endTime, status, qrEnabled, hostId) VALUES 
(1, 'Summer Vibes', 'Athens Beach', NOW(), DATE_ADD(NOW(), INTERVAL 5 HOUR), 'active', TRUE, 1),
(2, 'Winter Party', 'Thessaloniki Center', DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 52 HOUR), 'scheduled', FALSE, 2);

-- 9. Αρχική Playlist για το Party 1
INSERT INTO Playlist (playlistId, generated, totalSongs, partyId) VALUES 
(1, TRUE, 3, 1);

-- Προσθήκη αρχικών τραγουδιών στην Playlist 1
INSERT INTO Playlist_Songs (playlistId, songId) VALUES 
(1, 1), 
(1, 2),
(1, 3);