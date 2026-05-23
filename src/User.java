public abstract class User {
    protected int userid;
    protected String name;
    protected String email;
    protected String password;
    protected boolean loggedIn;

    public User(int userid, String name, String email, String password) {
        this.userid = userid;
        this.name = name;
        this.email = email;
        this.password = password;
        this.loggedIn = false;
    }

    public boolean login(String inputEmail, String inputPassword) {
        if (this.email.equals(inputEmail) && this.password.equals(inputPassword)) {
            this.loggedIn = true;
            return true;
        }
        return false;
    }

    public void logout() {
        this.loggedIn = false;
    }

    public void updateProfile(String newName, String newEmail) {
        this.name = newName;
        this.email = newEmail;
        System.out.println("👤 [UML Sync] Το προφίλ του χρήστη ενημερώθηκε: " + this.name);
    }

    public boolean changePassword(String oldPassword, String newPassword) {
        if (this.password.equals(oldPassword)) {
            this.password = newPassword;
            System.out.println("🔒 [UML Sync] Ο κωδικός πρόσβασης άλλαξε επιτυχώς.");
            return true;
        }
        System.out.println("❌ [UML Sync] Λάθος παλιός κωδικός πρόσβασης.");
        return false;
    }

    public int getUserid() { return userid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public boolean isLoggedIn() { return loggedIn; }
}