package engine.android.util.manager;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 我的手机动作感应管理器
 * 
 * @author Daimon
 * @version N
 * @since 11/7/2012
 */
public class MySensorManager implements SensorEventListener {

    private final SensorManager sm;                         // 手机动作感应管理器

    private final Sensor aSensor;                           // 重力感应器
    private final Sensor mSensor;                           // 磁场感应器

    /** Daimon:CopyOnWriteArraySet **/
    private CopyOnWriteArraySet<MySensorListener> listener; // 手机动作感应监听器

    public MySensorManager(Context context) {
        sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // 获取相应的感应器
        aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        /**
         * TYPE注解
         * TYPE_ACCELEROMETER 加速度
         * TYPE_ALL 所有类型，NexusOne默认为 加速度
         * TYPE_GYROSCOPE 回转仪(这个不太懂)
         * TYPE_LIGHT 光线感应
         * TYPE_MAGNETIC_FIELD 磁场
         * TYPE_ORIENTATION 定向（指北针）和角度
         * TYPE_PRESSUR 压力计
         * TYPE_PROXIMITY 距离？不太懂
         * TYPE_TEMPERATURE 温度
         */
    }

    /**
     * 在onStart()方法中<br>
     * 注册感应监听器
     * 
     * @return 是否支持重力感应
     */
    public boolean register() {
        if (aSensor == null)
        {
            // 没有重力感应装置
            return false;
        }

        // 注册监听器，第三个参数是检测的灵敏度
        if (sm.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_UI))
        {
            sm.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
            return true;
        }

        return false;
    }

    /**
     * 在onStop()方法中<br>
     * 释放感应监听器
     */
    public void unregister() {
        sm.unregisterListener(this);
    }

    public void addSensorListener(MySensorListener l) {
        if (listener == null)
        {
            listener = new CopyOnWriteArraySet<MySensorListener>();
        }

        listener.add(l);
    }

    public void removeSensorListener(MySensorListener l) {
        if (listener != null)
        {
            listener.remove(l);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (listener != null && !listener.isEmpty())
        {
            for (MySensorListener l : listener)
            {
                l.notifyUpdate(event);
            }
        }
    }

    /**
     * 我的手机动作感应监听器
     */
    public interface MySensorListener {

        void notifyUpdate(SensorEvent event);
    }

    /**
     * 加速度感应
     */
    public static abstract class AccelerometerSensorListener implements MySensorListener {

        @Override
        public final void notifyUpdate(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                Point3D p3 = new Point3D(
                        event.values[SensorManager.DATA_X],
                        event.values[SensorManager.DATA_Y], 
                        event.values[SensorManager.DATA_Z]);
                notifyUpdate(p3);
            }
        }

        public abstract void notifyUpdate(Point3D update);
    }

    /**
     * 手机旋转感应
     */
    public static abstract class RotateSensorListener extends AccelerometerSensorListener {

        protected static int UPDATE_INTERVAL    = 100;        // 感应检测时间间隔（最小值：60）

        protected static final int ROTATE_NONE     = 0;       // 手机没有翻转
        protected static final int ROTATEX_LEFT    = 1;       // 手机向左翻转
        protected static final int ROTATEX_RIGHT   = 2;       // 手机向右翻转
        protected static final int ROTATEY_UP      = 3;       // 手机向上翻转
        protected static final int ROTATEY_DOWN    = 4;       // 手机向下翻转

        private Point3D lastUpdate;

        @Override
        public void notifyUpdate(Point3D update) {
            if (lastUpdate == null)
            {
                lastUpdate = update;
            }
            else if (update.timestamp - lastUpdate.timestamp >= UPDATE_INTERVAL)
            {
                /**
                 *  手机是纵向屏幕并水平放置:
                 *  此时x=0,y=0
                 *  z>0说明屏幕朝天,z<0说明屏幕朝地
                 *  x>0说明手机左翻,x<0说明手机右翻
                 *  y>0说明手机下翻,y<0说明手机上翻
                 */
                int rotateX = ROTATE_NONE;
                int rotateY = ROTATE_NONE;
                if (update.x > 0)
                {
                    rotateX = ROTATEX_LEFT;
                }
                else if (update.x < 0)
                {
                    rotateX = ROTATEX_RIGHT;
                }

                if (update.y > 0)
                {
                    rotateY = ROTATEY_DOWN;
                }
                else if (update.y < 0)
                {
                    rotateY = ROTATEY_UP;
                }

                notifyRotate(rotateX, rotateY, Point3D.getSpeed(lastUpdate, update));
                lastUpdate = update;
            }
        }

        /**
         * 手机翻转通知
         * 
         * @param rotateX X轴方向
         * @param rotateY Y轴方向
         * @param speed 翻转速率
         */
        public abstract void notifyRotate(int rotateX, int rotateY, float speed);
    }

    /**
     * 屏幕方向感应
     */
    public static abstract class OrientationSensorListener implements MySensorListener {

        private float[] magneticFieldValues = new float[3];
        private float[] accelerometerValues = new float[3];

        private float[] values = new float[3];
        private float[] R = new float[9];

        @Override
        public final void notifyUpdate(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magneticFieldValues = event.values;
                    break;
                case Sensor.TYPE_ACCELEROMETER:
                    accelerometerValues = event.values;
                    break;

                default:
                    return;
            }

            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
            SensorManager.getOrientation(R, values);

            notifyOrientation((float) Math.toDegrees(values[0]));
        }

        /**
         * 手机方向通知
         * 
         * @param degree 角度(relative to east of north)
         */
        public abstract void notifyOrientation(float degree);
    }

    /**
     * 3D坐标
     */
    public static final class Point3D {

        /***** 坐标轴 *****/
        public static final int AXIS_X = 0;
        public static final int AXIS_Y = 1;
        public static final int AXIS_Z = 2;

        float x, y, z;                              // 手机三维坐标（各方向的加速度）
        long timestamp;                             // 时间戳（单位：毫秒）

        public Point3D(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "x:" + x + ",y:" + y + ",z:" + z;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getZ() {
            return z;
        }

        public long getTimestamp() {
            return timestamp;
        }

        private float getAxis(int axis) {
            return new float[] { x, y, z }[axis];
        }

        /**
         * 获取手机转动速度
         * 
         * @param p1,p2 两次变动的坐标
         */
        public static float getSpeed(Point3D p1, Point3D p2) {
            return (Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y) + Math.abs(p1.z - p2.z))
                    * 1000 / Math.abs(p1.timestamp - p2.timestamp);
        }

        /**
         * 获取手机某一轴线上的转动速度
         * 
         * @param p1,p2 两次变动的坐标
         * @param axis {@link #AXIS_X},{@link #AXIS_Y},{@link #AXIS_Z}
         */
        public static float getSpeed(Point3D p1, Point3D p2, int axis) {
            return Math.abs(p1.getAxis(axis) - p2.getAxis(axis)) * 1000
                    / Math.abs(p1.timestamp - p2.timestamp);
        }
    }
}