package ke.co.debechlabs.missingpersons.models;

/**
 * Created by chriz on 5/24/2017.
 */

public class Contact {
    private int id;
    private String name, contact;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
