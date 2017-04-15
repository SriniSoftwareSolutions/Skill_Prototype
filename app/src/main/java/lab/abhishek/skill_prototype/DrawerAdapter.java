package lab.abhishek.skill_prototype;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Abhishek on 15-Apr-17.
 */

public class DrawerAdapter extends BaseAdapter{

    private List<DrawerItem> mDrawerItems;
    private LayoutInflater mInflater;
    private Context context;

    public DrawerAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDrawerItems = getItemList();
        this.context = context;
    }

    private List<DrawerItem> getItemList() {

        List<DrawerItem> list = new ArrayList<>();
        list.add(new DrawerItem(0, "Create Training", R.mipmap.card));
        list.add(new DrawerItem(1, "About Us", R.mipmap.team));
        list.add(new DrawerItem(2, "Contact Us", R.mipmap.phonebook));
        list.add(new DrawerItem(3, "Sign Out", R.mipmap.exit));
        list.add(new DrawerItem(4, "Exit",R.mipmap.door));
        return list;
    }

    @Override
    public int getCount() {
        return mDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDrawerItems.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(
                    R.layout.navigationview_list_item, parent,
                    false);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView
                    .findViewById(R.id.icon_social_navigation_item);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DrawerItem item = mDrawerItems.get(position);

        Picasso.with(context).load(item.getIconRes()).fit().into(holder.icon);
        holder.title.setText(item.getText());

        return convertView;
    }

    private static class ViewHolder {
        public ImageView icon;
        public TextView title;
    }
}
