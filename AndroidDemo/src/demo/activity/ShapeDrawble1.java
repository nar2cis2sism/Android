package demo.activity;

import android.app.Activity; 
import android.content.Context; 
import android.graphics.Bitmap; 
import android.graphics.BitmapShader; 
import android.graphics.Canvas; 
import android.graphics.Color;
import android.graphics.ComposePathEffect; 
import android.graphics.CornerPathEffect; 
import android.graphics.DiscretePathEffect; 
import android.graphics.LinearGradient; 
import android.graphics.Paint; 
import android.graphics.Path; 
import android.graphics.PathEffect; 
import android.graphics.RectF; 
import android.graphics.Shader; 
import android.graphics.SweepGradient; 
import android.graphics.drawable.Drawable; 
import android.graphics.drawable.ShapeDrawable; 
import android.graphics.drawable.shapes.ArcShape; 
import android.graphics.drawable.shapes.OvalShape; 
import android.graphics.drawable.shapes.PathShape; 
import android.graphics.drawable.shapes.RectShape; 
import android.graphics.drawable.shapes.RoundRectShape; 
import android.graphics.drawable.shapes.Shape; 
import android.os.Bundle; 
import android.view.View; 

public class ShapeDrawble1 extends Activity { 
  /** Called when the activity is first created. */ 


    @Override 
    protected void onCreate(Bundle savedInstanceState) { 
      super.onCreate(savedInstanceState); 
      setContentView(new SampleView(this)); 
    } 

    private static class SampleView extends View { 
       private ShapeDrawable[] mDrawables; 

       private static Shader makeSweep() { 

         /* SweepGradient 是放射性渐变效果*/ 
         return new SweepGradient(0, 0, 
           new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFF0000 }, 

           null);// null 表示均衡变化 
       } 

       private static Shader makeLinear() { 
         //颜色按照直线线性变化的着色器 
         return new LinearGradient(100, 100, 0, 0, 
                   new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF }, 
                   null, Shader.TileMode.MIRROR); 
       } 

       private static Shader makeTiling() { 
         int[] pixels = new int[] { 0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0}; 
         Bitmap bm = Bitmap.createBitmap(pixels, 1, 1, 
                            Bitmap.Config.ARGB_8888); 
         /** 
          * BitmapShader 是一个位图着色器，这个着色器是通过 
          * 在x，y 方向重复位图 bm 的像素来着色的 
          * 
          */ 
         return new BitmapShader(bm, Shader.TileMode.REPEAT, 
                         Shader.TileMode.REPEAT); 
       } 
      /** 
       * ShapeDrawable 是绘制各种几何体的类。它注入想要绘制的形状shap 
       * 类，就可以绘制出我们想要的集合体，这个类最寒心的就是 draw （canvas） 
       * 和 onDraw     （Shape，Canvas，Paint）这个方法调用 
       * 
       */ 
       private static class MyShapeDrawable extends ShapeDrawable { 

         //Paint.ANTI_ALIAS_FLAG 代表这个画笔的图形是光滑的 
         private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG); 

         public MyShapeDrawable(Shape s) { 
           super(s); 
           mStrokePaint.setColor(Color.WHITE);
           mStrokePaint.setStyle(Paint.Style.STROKE); 
         } 

         public Paint getStrokePaint() { 
           return mStrokePaint; 
         } 

         @Override protected void onDraw(Shape s, Canvas c, Paint p) { 
           //绘制填充效果的图形 
           s.draw(c, p); 

           //绘制白边
           s.draw(c, mStrokePaint); 
         } 
       } 

       public SampleView(Context context) { 
         super(context); 
         setFocusable(true); 
         //外部圆角矩形的圆角圆半径，上面俩个角是圆 
         float[] outerR = new float[] { 12, 12, 12, 12, 0, 0, 0, 0 }; 
         // 内部矩形 
          RectF   inset = new RectF(6, 6, 6, 6); 

          // 内部圆角矩形的圆角是圆半径，左上角和右下角是圆角矩形 
          float[] innerR = new float[] { 12, 12, 0, 0, 12, 12, 0, 0 }; 
          //绘制一个顶点为下列四个点的棱形 
          Path path = new Path(); 
          path.moveTo(50, 0); 
          path.lineTo(0, 50); 
          path.lineTo(50, 100); 
          path.lineTo(100, 50); 
          //封闭前面点所绘制的路径 
          path.close(); 
 
          mDrawables = new ShapeDrawable[7]; 
          //绘制矩形 
          mDrawables[0] = new ShapeDrawable(new RectShape()); 

         //绘制椭圆 
          mDrawables[1] = new ShapeDrawable(new OvalShape()); 
         //绘制上面俩个角是圆角的矩形 
          mDrawables[2] = new ShapeDrawable(new RoundRectShape(outerR, null, 
                                       null)); 

         //绘制上面俩角是圆角，并且有一个内嵌的矩形 
          mDrawables[3] = new ShapeDrawable(new RoundRectShape(outerR, inset, 
                                       null)); 

         ////绘制上面俩角是圆角，并且有一个内嵌的矩形且左上角和右下角是圆形 矩形环 
          mDrawables[4] = new ShapeDrawable(new RoundRectShape(outerR, inset, 
                                       innerR)); 

         //绘制指定路径的集合体 
          mDrawables[5] = new ShapeDrawable(new PathShape(path, 100, 100)); 

         // 用自定的ShapDrawble 绘制开始弧度45 扫过弧度-270 的椭圆 
          mDrawables[6] = new MyShapeDrawable(new ArcShape(45, -270)); 

          mDrawables[0].getPaint().setColor(0xFFFF0000); 
          mDrawables[1].getPaint().setColor(0xFF00FF00); 
          mDrawables[2].getPaint().setColor(0xFF0000FF); 
          mDrawables[3].getPaint().setShader(makeSweep()); 
          mDrawables[4].getPaint().setShader(makeLinear()); 
          mDrawables[5].getPaint().setShader(makeTiling()); 
          mDrawables[6].getPaint().setColor(0x88FF8844); 
         //DiscretePathEffect 是一个折线路径效果，分割长度是 10，偏差时4 
          PathEffect pe = new DiscretePathEffect(10, 4); 
         //CornerPathEffect 是将2 个路径效果合并后的路径效果 
          PathEffect pe2 = new CornerPathEffect(4); 
          mDrawables[3].getPaint().setPathEffect( 
                              new ComposePathEffect(pe2, pe)); 

          MyShapeDrawable msd = (MyShapeDrawable)mDrawables[6]; 
         //设置笔画宽度等于4 
          msd.getStrokePaint().setStrokeWidth(4); 
       } 

       @Override protected void onDraw(Canvas canvas) { 

          int x = 10; 
          int y = 10; 
          int width = 300; 
          int height = 50; 
         //循环绘制 
         for (Drawable dr : mDrawables) { 
           dr.setBounds(x, y, x + width, y + height); 
           dr.draw(canvas); 
           y += height + 5; 
         } 
       } 
    } 
  } 