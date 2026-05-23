-- Δημιουργία της Βάσης Δεδομένων
CREATE DATABASE IF NOT EXISTS PartyAppDB;
USE PartyAppDB;

-- Απενεργοποίηση ελέγχων για ασφαλές "γκρέμισμα" των παλιών πινάκων
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS Subscription;
DROP TABLE IF EXISTS QRCode;
DROP TABLE IF EXISTS LightingSystem;
DROP TABLE IF EXISTS AudioSettings;
DROP TABLE IF EXISTS Environment;
DROP TABLE IF EXISTS Payment;
DROP TABLE IF EXISTS SongRequest;
DROP TABLE IF EXISTS Playlist_Songs;
DROP TABLE IF EXISTS Song;
DROP TABLE IF EXISTS Playlist;
DROP TABLE IF EXISTS Party;
DROP TABLE IF EXISTS PartyGuest;
DROP TABLE IF EXISTS PartyHost;
DROP TABLE IF EXISTS `User`;

-- Επανενεργοποίηση ελέγχων
SET FOREIGN_KEY_CHECKS = 1;


-- 1. Κεντρικός πίνακας User (Γονική κλάση)
CREATE TABLE `User` (
    `userId` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) UNIQUE NOT NULL,
    `password` VARCHAR(255) NOT NULL
);

-- 2. Πίνακας PartyHost (Κληρονομεί από User)
CREATE TABLE PartyHost (
    userId INT PRIMARY KEY,
    hostRating DECIMAL(3,2) DEFAULT 0.0,
    hostedPartiesCount INT DEFAULT 0,
    subscriptionLevel VARCHAR(50),
    totalRevenue DECIMAL(10,2) DEFAULT 0.0,
    FOREIGN KEY (userId) REFERENCES `User`(userId) ON DELETE CASCADE
);

-- 3. Πίνακας PartyGuest (Κληρονομεί από User)
CREATE TABLE PartyGuest (
    userId INT PRIMARY KEY,
    nickname VARCHAR(100),
    requestLimit INT DEFAULT 3,
    FOREIGN KEY (userId) REFERENCES `User`(userId) ON DELETE CASCADE
);

-- 4. Πίνακας Party
CREATE TABLE Party (
    partyId INT AUTO_INCREMENT PRIMARY KEY,
    theme VARCHAR(100),
    location VARCHAR(255),
    startTime DATETIME,
    endTime DATETIME,
    status VARCHAR(50) DEFAULT 'scheduled',
    qrEnabled BOOLEAN DEFAULT TRUE,
    hostId INT,
    FOREIGN KEY (hostId) REFERENCES PartyHost(userId) ON DELETE SET NULL
);

-- 5. Πίνακας Playlist
CREATE TABLE Playlist (
    playlistId INT AUTO_INCREMENT PRIMARY KEY,
    generated BOOLEAN DEFAULT FALSE,
    totalSongs INT DEFAULT 0,
    partyId INT UNIQUE,
    FOREIGN KEY (partyId) REFERENCES Party(partyId) ON DELETE CASCADE
);

-- 6. Πίνακας Song
CREATE TABLE Song (
    songId INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    artist VARCHAR(100) NOT NULL,
    genre VARCHAR(50),
    duration INT,
    popularity DECIMAL(5,2)
);

-- 7. Πίνακας Συσχέτισης Playlist_Songs (Πολλά-προς-Πολλά)
CREATE TABLE Playlist_Songs (
    playlistId INT,
    songId INT,
    PRIMARY KEY (playlistId, songId),
    FOREIGN KEY (playlistId) REFERENCES Playlist(playlistId) ON DELETE CASCADE,
    FOREIGN KEY (songId) REFERENCES Song(songId) ON DELETE CASCADE
);

-- 8. Πίνακας SongRequest
CREATE TABLE SongRequest (
    requestId INT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(50) DEFAULT 'pending',
    timestamp DATETIME,
    queuePosition INT,
    guestId INT,
    songId INT,
    partyId INT,
    FOREIGN KEY (guestId) REFERENCES PartyGuest(userId) ON DELETE CASCADE,
    FOREIGN KEY (songId) REFERENCES Song(songId) ON DELETE CASCADE,
    FOREIGN KEY (partyId) REFERENCES Party(partyId) ON DELETE CASCADE
);

-- 9. Πίνακας Payment
CREATE TABLE Payment (
    paymentId INT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(10,2),
    paymentDate DATETIME,
    paymentStatus VARCHAR(50) DEFAULT 'pending',
    partyId INT,
    guestId INT,
    FOREIGN KEY (partyId) REFERENCES Party(partyId) ON DELETE CASCADE,
    FOREIGN KEY (guestId) REFERENCES PartyGuest(userId) ON DELETE CASCADE
);

-- 10. Πίνακες Συστημάτων Χώρου
CREATE TABLE Environment (
    environmentId INT AUTO_INCREMENT PRIMARY KEY,
    crowdDensity INT DEFAULT 0,
    roomSize INT,
    detectedMood VARCHAR(50) DEFAULT 'calm',
    partyId INT UNIQUE,
    FOREIGN KEY (partyId) REFERENCES Party(partyId) ON DELETE CASCADE
);

CREATE TABLE AudioSettings (
    audioId INT AUTO_INCREMENT PRIMARY KEY,
    partyId INT UNIQUE,
    volume INT DEFAULT 50,
    bass INT DEFAULT 50,
    treble INT DEFAULT 50,
    FOREIGN KEY (partyId) REFERENCES Party(partyId) ON DELETE CASCADE
);

CREATE TABLE LightingSystem (
    lightingId INT AUTO_INCREMENT PRIMARY KEY,
    lightingMode VARCHAR(50),
    brightness INT DEFAULT 100,
    colorScheme VARCHAR(50),
    partyId INT UNIQUE,
    FOREIGN KEY (partyId) REFERENCES Party(partyId) ON DELETE CASCADE
);

CREATE TABLE QRCode (
    qrCodeId INT AUTO_INCREMENT PRIMARY KEY,
    qrValue VARCHAR(255) NOT NULL,
    expirationDate DATETIME,
    isValid BOOLEAN DEFAULT TRUE,
    partyId INT,
    FOREIGN KEY (partyId) REFERENCES Party(partyId) ON DELETE CASCADE
);

-- Πίνακας Subscription
CREATE TABLE Subscription (
    subscriptionId INT AUTO_INCREMENT PRIMARY KEY,
    planType VARCHAR(50),
    status VARCHAR(50) DEFAULT 'active',
    monthlyCost DECIMAL(10,2),
    hostId INT,
    FOREIGN KEY (hostId) REFERENCES PartyHost(userId) ON DELETE CASCADE
);