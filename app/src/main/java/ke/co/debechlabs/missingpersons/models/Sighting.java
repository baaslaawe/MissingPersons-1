package ke.co.debechlabs.missingpersons.models;

/**
 * Created by chriz on 5/24/2017.
 */

public class Sighting {
    private int id, person_id;
    private String location, coordinates, dateseen, dateadded, validated;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPerson_id() {
        return person_id;
    }

    public void setPerson_id(int person_id) {
        this.person_id = person_id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getDateseen() {
        return dateseen;
    }

    public void setDateseen(String dateseen) {
        this.dateseen = dateseen;
    }

    public String getDateadded() {
        return dateadded;
    }

    public void setDateadded(String dateadded) {
        this.dateadded = dateadded;
    }

    public String getValidated() {
        return validated;
    }

    public void setValidated(String validated) {
        this.validated = validated;
    }
}
