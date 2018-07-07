package Model;

/**
 * Created by 300269668 on 6/11/2018.
 */

public class SharedUser {
    private String id;
    private String name;
    private String permission;
    private String data_id;

    public String getData_id() {
        return data_id;
    }

    public SharedUser(String id, String name, String permission, String data_id) {
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.data_id = data_id;
    }


    public String getPermission() {
        return permission;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
