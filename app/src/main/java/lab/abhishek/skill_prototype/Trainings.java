package lab.abhishek.skill_prototype;

/**
 * Created by Abhishek on 15-Apr-17.
 */

public class Trainings {

    private String _id, user_id, user_name , training_name, phone, availability, location, latitude, longtitude, price, description, image_url;

    public Trainings(){

    }

    public Trainings(String _id, String user_id, String user_name, String training_name, String phone, String availability, String location, String latitude, String longtitude, String price, String description, String image_url) {
        this._id = _id;
        this.user_id = user_id;
        this.user_name = user_name;
        this.training_name = training_name;
        this.phone = phone;
        this.availability = availability;
        this.location = location;
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.price = price;
        this.description = description;
        this.image_url = image_url;
    }

    public String getImage_url(){
        return image_url;
    }

    public String get_id() {
        return _id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getTraining_name() {
        return training_name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvailability() {
        return availability;
    }

    public String getLocation() {
        return location;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }
}
