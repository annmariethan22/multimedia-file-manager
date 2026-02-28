package classes;

public class Author extends SimpleUser {

    public Author() {
        super();
    }

    public Author(String firstName, String lastName, String username, String password) {
        super(firstName, lastName, username, password, "Author");
    }

    protected Author(String firstName, String lastName, String username, String password, String role) {
        super(firstName, lastName, username, password, role);
    }
}
