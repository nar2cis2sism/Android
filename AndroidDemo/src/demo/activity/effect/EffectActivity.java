package demo.activity.effect;

import engine.android.util.anim.TVAnimation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import demo.android.R;
import demo.lockscreen.LockActivity;

import java.util.LinkedList;
import java.util.List;

public class EffectActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final ListView lv = (ListView) findViewById(R.id.list);
        List<String> list = new LinkedList<String>();
        list.add("抽屉");
        list.add("画廊");
        list.add("3D旋转");
        list.add("拍照");
        list.add("360度平滑摇杆（触屏方向导航）");
        list.add("水波纹效果");
        list.add("界面滑动");
        list.add("图片缩放");
        list.add("ScrollView反弹效果");
        list.add("悬浮窗口");
        list.add("歌词同步");
        list.add("翻页效果");
        list.add("转盘");
        list.add("滑动锁屏");
        list.add("瀑布流图片");
        list.add("密码锁");
        list.add("放大镜");
        list.add("火焰效果");
        list.add("徽章效果");
        list.add("九宫格锁屏界面");
        list.add("电视关闭");
        list.add("图片处理");
        list.add("Activity切换动画效果");
        list.add("毛玻璃");
        lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list));
        lv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
                case 0:
                    // 抽屉
                    startActivity(new Intent(EffectActivity.this, PanelActivity.class));
                    break;
                case 1:
                    // 画廊
                    startActivity(new Intent(EffectActivity.this, GalleryActivity.class));
                    break;
                case 2:
                    // 3D旋转
                    startActivity(new Intent(EffectActivity.this, Rotate3DActivity.class));
                    break;
                case 3:
                    // 拍照
//                    startActivity(new Intent(EffectActivity.this, TakePhotoActivity.class));
                    break;
                case 4:
					// 360度平滑摇杆（触屏方向导航）
					startActivity(new Intent(EffectActivity.this, RockerActivity.class));
					break;
                case 5:
					// 水波纹效果
//					startActivity(new Intent(EffectActivity.this, WaterActivity.class));
					break;
                case 6:
					// 界面滑动
					startActivity(new Intent(EffectActivity.this, FlingGalleryActivity.class));
					break;
                case 7:
					// 图片缩放
					startActivity(new Intent(EffectActivity.this, ImageZoomActivity.class));
					break;
                case 8:
					// ScrollView反弹效果
					startActivity(new Intent(EffectActivity.this, ScrollActivity.class));
					break;
                case 9:
					// 悬浮窗口
					startActivity(new Intent(EffectActivity.this, FloatingActivity.class));
					break;
                case 10:
					// 歌词同步界面
					startActivity(new Intent(EffectActivity.this, LyricActivity.class));
					break;
                case 11:
					// 翻页效果界面
					startActivity(new Intent(EffectActivity.this, BookActivity.class));
					break;
                case 12:
					// 转盘
//					startActivity(new Intent(EffectActivity.this, WheelActivity.class));
					break;
                case 13:
					// 滑动锁屏
					startActivity(new Intent(EffectActivity.this, LockActivity.class));
					break;
                case 14:
					// 瀑布流图片
//					startActivity(new Intent(EffectActivity.this, PhotoFlowActivity.class));
					break;
                case 15:
                    // 密码锁
                    startActivity(new Intent(EffectActivity.this, PasswordActivity.class));
                    break;
                case 16:
                    // 放大镜
                    startActivity(new Intent(EffectActivity.this, MagnifierActivity.class));
                    break;
                case 17:
                    // 火焰效果
                    startActivity(new Intent(EffectActivity.this, BlazeActivity.class));
                    break;
                case 18:
                    // 徽章效果
                    startActivity(new Intent(EffectActivity.this, BadgeActivity.class));
                    break;
                case 19:
                    // 九宫格锁屏界面
                    startActivity(new Intent(EffectActivity.this, LockPatternActivity.class));
                    break;
                case 20:
                    // 电视关闭
                    TVAnimation anim = new TVAnimation(true);
                    anim.setAnimationListener(new AnimationListener() {
                        
                        @Override
                        public void onAnimationStart(Animation animation) {
                            // TODO Auto-generated method stub
                            
                        }
                        
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            // TODO Auto-generated method stub
                            
                        }
                        
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            getWindow().getDecorView().postDelayed(new Runnable() {
                                
                                @Override
                                public void run() {
                                    lv.startAnimation(new TVAnimation(false));
                                }
                            }, 2000);
                        }
                    });
                    lv.startAnimation(anim);
                    break;
                case 21:
                    // 图片处理
                    startActivity(new Intent(EffectActivity.this, BitmapActivity.class));
                    break;
                case 22:
                    // Activity切换动画效果
                    startActivity(new Intent(EffectActivity.this, AnimationActivity.class));
                    break;
                case 23:
                    // 毛玻璃
                    startActivity(new Intent(EffectActivity.this, BlurGlassActivity.class));
                    break;

				default:
					break;
				}
			}});
    }
}