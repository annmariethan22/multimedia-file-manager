package classes;

public class SimpleUser extends User {
    
    
    public SimpleUser() {
        super();
    }

    public SimpleUser(String firstName, String lastName, String username, String password) {
        super(firstName, lastName, username, password, "Simple User");
    }

    
    protected SimpleUser(String firstName, String lastName, String username, String password, String role) {
        super(firstName, lastName, username, password, role);
    }
}