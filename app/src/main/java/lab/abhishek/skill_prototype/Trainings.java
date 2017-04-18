package lab.abhishek.skill_prototype;

/**
 * Created by Abhishek on 15-Apr-17.
 */

public class Trainings {

    private String id,training_name, location, price, mobile, image_url, trainer_id;
    private int dist;

    public Trainings() {
    }

    public Trainings(String id, String training_name, String location, String price, String mobile, String image_url, String trainer_id) {
        this.id = id;
        this.training_name = training_name;
        this.location = location;
        this.price = price;
        this.mobile = mobile;
        this.image_url = image_url;
        this.trainer_id = trainer_id;
    }

    public Trainings(String id, String training_name, String location, String price, String mobile, String image_url, String trainer_id, int dist) {
        this.id = id;
        this.training_name = training_name;
        this.location = location;
        this.price = price;
        this.mobile = mobile;
        this.image_url = image_url;
        this.trainer_id = trainer_id;
        this.dist = dist;
    }

    public int getDist(){
        return dist;
    }

    public String getId(){
        return id;
    }

    public String getTrainer_id(){
        return trainer_id;
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
