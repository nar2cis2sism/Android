package engine.android.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 进程间传输事件
 * 
 * @author Daimon
 * @since 10/17/2014
 */
public final class Event extends engine.android.core.extra.EventBus.Event implements Parcelable {
    
    public Event(String action, int status, Object param) {
        super(action, status, param);
    }
	
	/**
	 * 私有构造方法，从Parcel读出数据，根据(writeToParcel)写入时顺序排列
	 */
	private Event(Parcel source) {
        super(source.readString(), source.readInt(), source.readValue(null));
	}
	
	/**
	 * 此处Parcelable.Creator的实例名必须为CREATOR且为public static，否则对象被传出时会抛出异常
	 */
	public static final Parcelable.Creator<Event> CREATOR = new Creator<Event>() {

		@Override
		public Event createFromParcel(Parcel source) {
			return new Event(source);
		}

		@Override
		public Event[] newArray(int size) {
			return new Event[size];
		}};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	    dest.writeString(action);
	    dest.writeInt(status);
	    dest.writeValue(param);
	}
}