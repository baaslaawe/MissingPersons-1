package ke.co.debechlabs.missingpersons.models;

/**
 * Created by chriz on 4/16/2017.
 */

public class Person {
    private int id, found;
    private String personname, reported_date, image_url;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPersonname() {
        return personname;
    }

    public void setPersonname(String personname) {
        this.personname = personname;
    }

    public String getReported_date() {
        return reported_date;
    }

    public void setReported_date(String reported_date) {
        this.reported_date = reported_date;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getFound() {
        return found;
    }

    public void setFound(int found) {
        this.found = found;
    }
}
