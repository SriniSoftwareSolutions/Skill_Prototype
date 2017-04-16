package lab.abhishek.skill_prototype;

/**
 * Created by Abhishek on 15-Apr-17.
 */

public class Trainings {

    private String training_name, location, price, mobile, image_url;

    public Trainings() {
    }

    public Trainings(String training_name, String location, String price, String mobile, String image_url) {
        this.training_name = training_name;
        this.location = location;
        this.price = price;
        this.mobile = mobile;
        this.image_url = image_url;
    }

    public String getTraining_name() {
        return training_name;
    }

    public String getLocation() {
        return location;
    }

    public String getPrice() {
        return price;
    }

    public String getMobile() {
        return mobile;
    }

    public String getImage_url(){
        return image_url;
    }

}
