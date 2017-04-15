package lab.abhishek.skill_prototype;

/**
 * Created by Abhishek on 15-Apr-17.
 */

public class DrawerItem {

    private long mId;
    private String mText;
    private int mIconRes;

    public DrawerItem() {
    }

    public DrawerItem(long id, String text, int iconRes) {
        mId = id;
        mText = text;
        mIconRes = iconRes;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public int getIconRes() {
        return mIconRes;
    }

    public void setIconRes(int iconRes) {
        mIconRes = iconRes;
    }

    @Override
    public String toString() {
        return mText;
    }

}
