package ke.co.debechlabs.missingpersons.models;

/**
 * Created by chriz on 4/18/2017.
 */

public class Member {
    private int id;
    private String member_name;
    private String member_photo;
    private String member_relationship;


    public String getMember_name() {
        return member_name;
    }

    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }

    public String getMember_photo() {
        return member_photo;
    }

    public void setMember_photo(String member_photo) {
        this.member_photo = member_photo;
    }

    public String getMember_relationship() {
        return member_relationship;
    }

    public void setMember_relationship(String member_relationship) {
        this.member_relationship = member_relationship;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
