public class RequestController {
    public SongRequest processSongRequest(PartyGuest guest, Song song, Party party) {
        System.out.println("⚙️ [RequestController] Validating request limits...");
        int currentRequests = 0;
        for (SongRequest req : guest.getSongRequests()) {
            if (req.getRequestedParty().getPartyId() == party.getPartyId()) {
                currentRequests++;
            }
        }
        if (currentRequests >= guest.getRequestLimit()) {
            System.out.println("❌ [RequestController] Limit exceeded! Allowed: " + guest.getRequestLimit());
            return null;
        }
        SongRequest newRequest = guest.submitSongRequest(song, party);
        PersistenceController.saveSongRequest(newRequest);
        return newRequest;
    }

    public void processApproval(SongRequest request, Party party) {
        System.out.println("⚙️ [RequestController] Processing approval for: " + request.getRequestedSong().getTitle());
        request.approveRequest();
        party.getPlaylist().addSong(request.getRequestedSong());
        PersistenceController.updateSongRequestStatus(request.getRequestId(), "approved");
        PersistenceController.saveGeneratedPlaylist(party);
    }
}