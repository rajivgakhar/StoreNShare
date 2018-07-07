package Model;

/**
 * Created by 300269668 on 7/3/2018.
 */

public class UserLog {
    private String user_id;
    private String message;
    private String date;
    public UserLog(String id, String user_id, String message, String date) {
        this.id = id;
        this.user_id = user_id;
        this.message = message;
        this.date = date;
    }

    private String id;

    public String getId() {
        return id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }




}
