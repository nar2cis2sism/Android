package engine.android.util.io;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 自定义数据传递对象
 * 
 * @author Daimon
 * @version N
 * @since 3/26/2012
 */
public class MyParcelable implements Parcelable {

    /**
     * 私有构造方法，从Parcel读出数据，根据(writeToParcel)写入时顺序排列
     */
    private MyParcelable(Parcel source) {
        read(source);
    }

    /**
     * 此处Parcelable.Creator的实例名必须为CREATOR且为public static，否则对象被传出时会抛出异常
     */
    public static final Parcelable.Creator<MyParcelable> CREATOR = new Creator<MyParcelable>() {

        @Override
        public MyParcelable createFromParcel(Parcel source) {
            return new MyParcelable(source);
        }

        @Override
        public MyParcelable[] newArray(int size) {
            return new MyParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        save(dest);
    }

    /**
     * 存储数据<br>
     * 传递对象需实现系列化（Parcelable或Serializable接口）
     */
    protected void save(Parcel p) {};

    /**
     * 读取数据<br>
     * 顺序与存储一致
     */
    protected void read(Parcel p) {};
}