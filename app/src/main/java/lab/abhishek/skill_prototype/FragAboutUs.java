package lab.abhishek.skill_prototype;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragAboutUs extends Fragment {

    private ImageButton ib_fb,ib_youtube,ib_twitter,ib_web, ib_rate_us;

    public FragAboutUs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        getActivity().setTitle("About Us");
        View view = inflater.inflate(R.layout.fragment_frag_about_us, container, false);

        ib_fb = (ImageButton) view.findViewById(R.id.abfb);
        ib_youtube = (ImageButton) view.findViewById(R.id.abyoutube);
        ib_twitter = (ImageButton) view.findViewById(R.id.abtwitter);
        ib_web = (ImageButton) view.findViewById(R.id.abweb);
        ib_rate_us = (ImageButton) view.findViewById(R.id.ab_rate_us);

        Picasso.with(getContext()).load(R.mipmap.abfacebook).fit().into(ib_fb);
        Picasso.with(getContext()).load(R.mipmap.abyoutube).fit().into(ib_youtube);
        Picasso.with(getContext()).load(R.mipmap.abtwitter).fit().into(ib_twitter);
        Picasso.with(getContext()).load(R.mipmap.abweb).fit().into(ib_web);
        Picasso.with(getContext()).load(R.mipmap.ablike).fit().into(ib_rate_us);


        ib_fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.facebook.com")));
            }
        });

        ib_youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.youtube.com")));
            }
        });

        ib_twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://twitter.com")));
            }
        });

        ib_web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://srini-web-developer.in")));
            }
        });

        ib_rate_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://play.google.com/store/apps");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getContext().getPackageName())));
                }
            }
        });

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = (MenuItem) menu.findItem(R.id.main_search);
        item.setVisible(false);
    }

}
