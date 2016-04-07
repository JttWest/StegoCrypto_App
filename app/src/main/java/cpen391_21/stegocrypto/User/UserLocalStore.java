package cpen391_21.stegocrypto.User;

import android.content.Context;
import android.content.SharedPreferences;


public class UserLocalStore {

    public static final String USER_LOCAL_STORE_SP_NAME = "userDetails";

    private SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context) {
        userLocalDatabase = context.getSharedPreferences(USER_LOCAL_STORE_SP_NAME, Context.MODE_PRIVATE);
    }

    public void storeUserData(User user) {
        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.putString("userName", user.getUserName());
        userLocalDatabaseEditor.putString("password", user.getPassword());
        userLocalDatabaseEditor.commit();
    }

    public void setUserLoggedIn(boolean loggedIn) {
        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.putBoolean("loggedIn", loggedIn);
        userLocalDatabaseEditor.commit();
    }

    public void clearUserData() {
        SharedPreferences.Editor userLocalDatabaseEditor = userLocalDatabase.edit();
        userLocalDatabaseEditor.clear();
        userLocalDatabaseEditor.commit();
    }

    // returns current login user in local DB
    public User getLoggedInUser() {
        if (!userLocalDatabase.getBoolean("loggedIn", false)) {
            return null;
        }

        String userName = userLocalDatabase.getString("userName", "");
        String password = userLocalDatabase.getString("password", "");
        String instanceIDToken = userLocalDatabase.getString("instanceIDToken", "");

        User user = new User(userName, password, instanceIDToken);
        return user;
    }

}
