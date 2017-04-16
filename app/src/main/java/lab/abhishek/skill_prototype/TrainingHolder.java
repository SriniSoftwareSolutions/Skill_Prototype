package lab.abhishek.skill_prototype;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by Abhishek on 16-Apr-17.
 */

public class TrainingHolder extends RecyclerView.ViewHolder {

    private View mView;
    private ImageView iv_photo;
    private TextView tv_name, tv_training, tv_location, tv_price;
    public CardView cardView;

    public TrainingHolder(View itemView) {
        super(itemView);
        mView = itemView;
        cardView = (CardView) mView.findViewById(R.id.cardView);
    }

    public void setvalues(String fname,String lname, String location, String price, String image_url, Context context){

        iv_photo = (ImageView) mView.findViewById(R.id.cardImage);
        tv_name = (TextView) mView.findViewById(R.id.card_name);
        tv_training = (TextView) mView.findViewById(R.id.card_mobile);
        tv_location = (TextView) mView.findViewById(R.id.card_location);
        tv_price = (TextView) mView.findViewById(R.id.card_price);

        try {
            Picasso.with(context).load(image_url).placeholder(R.mipmap.userdp).into(iv_photo);
        } catch (Exception e){
            Picasso.with(context).load(R.mipmap.userdp).into(iv_photo);
        }
        tv_name.setText(fname);
        tv_training.setText(lname);
        tv_location.setText(location);
        tv_price.setText(price);

    }
}
