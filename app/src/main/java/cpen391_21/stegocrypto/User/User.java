package cpen391_21.stegocrypto.User;

public class User {
    private String userName, password, instanceIDToken;

    public User(String userName, String password, String instanceIDToken) {
        this.userName = userName;
        this.password = password;
        this.instanceIDToken = instanceIDToken;
    }

    public User(String userName, String password) {
        this(userName, password, "");
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getInstanceIDToken() {
        return instanceIDToken;
    }
}
