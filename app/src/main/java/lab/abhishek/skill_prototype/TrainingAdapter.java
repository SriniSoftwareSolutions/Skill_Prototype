package lab.abhishek.skill_prototype;

import android.content.Context;
import android.support.v4.util.CircularArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Abhishek on 16-Apr-17.
 */

public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.MyViewHolder> {

    List<Trainings> trainingList;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        TextView name, location, mobile, price;

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (CircleImageView) itemView.findViewById(R.id.cardImage);
            name = (TextView) itemView.findViewById(R.id.card_name);
            location = (TextView) itemView.findViewById(R.id.card_location);
            mobile = (TextView) itemView.findViewById(R.id.card_mobile);
            price = (TextView) itemView.findViewById(R.id.card_price);
        }
    }


    public TrainingAdapter(List<Trainings> trainingList, Context context){
        this.trainingList = trainingList;
        this.context = context;
    }

    @Override
    public TrainingAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.training_card, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TrainingAdapter.MyViewHolder holder, int position) {

        final Trainings trainings = trainingList.get(position);
        holder.name.setText(trainings.getTraining_name());
        holder.price.setText(trainings.getPrice());
        holder.location.setText(trainings.getLocation());
        holder.mobile.setText(trainings.getMobile());

        try {
            Picasso.with(context).load(trainings.getImage_url()).placeholder(R.mipmap.userdp).networkPolicy(NetworkPolicy.OFFLINE).into(holder.image, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(trainings.getImage_url()).placeholder(R.mipmap.userdp).into(holder.image);
                }
            });
        } catch (Exception e){
            Picasso.with(context).load(R.mipmap.userdp).into(holder.image);
        }

    }

    @Override
    public int getItemCount() {
        return trainingList.size();
    }
}
