package classes;

public class Admin extends Author {

    public Admin() {
        super();
    }

    public Admin(String firstName, String lastName, String username, String password) {
        super(firstName, lastName, username, password, "Admin");
    }

    
    @Override
    public boolean hasAccessToCategory(String categoryName) {
        return true; 
    }
}