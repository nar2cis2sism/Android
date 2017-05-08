package engine.android.aidl;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 进程间传输指令
 * 
 * @author Daimon
 * @version N
 * @since 10/17/2014
 */
public final class Action implements Parcelable {

    /**
     * 实现此接口的子类必须保留一个空的构造器
     */
    public interface ActionParam {

        void saveToBundle(Bundle bundle);

        void readFromBundle(Bundle bundle);
    }
    
    public final String action;
    public final Bundle data;
    public boolean syncable;
    
    public Action(String action) {
        this(action, null);
    }
    
    public Action(String action, ActionParam param) {
        this.action = action;
        if (param != null)
        {
            param.saveToBundle(data = new Bundle());
        }
        else
        {
            data = null;
        }
    }
	
	/**
	 * 私有构造方法，从Parcel读出数据，根据(writeToParcel)写入时顺序排列
	 */
	private Action(Parcel source) {
        action = source.readString();
        data = source.readBundle();
        syncable = source.readInt() == 1;
	}
	
	/**
	 * 此处Parcelable.Creator的实例名必须为CREATOR且为public static，否则对象被传出时会抛出异常
	 */
	public static final Parcelable.Creator<Action> CREATOR = new Creator<Action>() {

		@Override
		public Action createFromParcel(Parcel source) {
			return new Action(source);
		}

		@Override
		public Action[] newArray(int size) {
			return new Action[size];
		}};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	    dest.writeString(action);
	    dest.writeBundle(data);
	    dest.writeInt(syncable ? 1 : 0);
	}

	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder(action);
	    if (syncable) sb.append(",syncable=").append(syncable);
	    if (data != null)
	    {
	        sb.append(",data=").append(data);
	    }
	    
	    return sb.toString();
	}
}