package demo.book;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.FloatMath;

import demo.book.BookImage.PageIndex;

/**
 * 书页卷动效果
 * @author yanhao
 * @version 1.0
 */

class CurlAnimation extends BookAnimation {
	
	private final Paint myPaint = new Paint();
	private final Paint myBackPaint = new Paint();
	private final Paint myEdgePaint = new Paint();

	private final Path myFgPath = new Path();
	private final Path myEdgePath = new Path();
	private final Path myQuadPath = new Path();

	private float mySpeedFactor = 1;

	CurlAnimation(BookImageManager manager) {
		super(manager);
		
		myBackPaint.setAntiAlias(true);
		myBackPaint.setAlpha(0x40);

		myEdgePaint.setAntiAlias(true);
		myEdgePaint.setStyle(Paint.Style.FILL);
		myEdgePaint.setShadowLayer(15, 0, 0, 0xC0000000);
	}

	@Override
	protected PageIndex getPageIndex(int x, int y) {
		if (direction == null)
		{
			return PageIndex.current;
		}
		
		switch (direction) {
		case leftToRight:
			return startX < width / 2 ? PageIndex.next : PageIndex.previous;
		case rightToLeft:
			return startX < width / 2 ? PageIndex.previous : PageIndex.next;
		case up:
			return startY < height / 2 ? PageIndex.previous : PageIndex.next;
		case down:
			return startY < height / 2 ? PageIndex.next : PageIndex.previous;

		default:
			return PageIndex.current;
		}
	}

	@Override
	protected void onAnimatedDraw(Canvas canvas) {
		canvas.drawBitmap(getBookImageTo(), 0, 0, myPaint);
		Bitmap image = getBookImageFrom();
		
		int cornerX = startX > width / 2 ? width : 0;
		int cornerY = startY > height / 2 ? height : 0;
		int oppositeX = Math.abs(width - cornerX);
		int oppositeY = Math.abs(height - cornerY);
		int x,y;
		
		if (direction.isHorizontal)
		{
			x = endX;
			if (getMode().auto)
			{
				y = endY;
			}
			else
			{
				if (cornerY == 0)
				{
					y = Math.max(1, Math.min(height / 2, endY));
				}
				else
				{
					y = Math.max(height / 2, Math.min(height - 1, endY));
				}
			}
		}
		else
		{
			y = endY;
			if (getMode().auto)
			{
				x = endX;
			}
			else
			{
				if (cornerX == 0)
				{
					x = Math.max(1, Math.min(width / 2, endX));
				}
				else
				{
					x = Math.max(width / 2, Math.min(width - 1, endX));
				}
			}
		}
		
		int dX = Math.max(1, Math.abs(x - cornerX));
		int dY = Math.max(1, Math.abs(y - cornerY));

		int x1 = cornerX == 0 ? (dY * dY / dX + dX) / 2 : cornerX - (dY * dY / dX + dX) / 2;
		int y1 = cornerY == 0 ? (dX * dX / dY + dY) / 2 : cornerY - (dX * dX / dY + dY) / 2;

		float sX, sY;
		{
			float d1 = x - x1;
			float d2 = y - cornerY;
			sX = FloatMath.sqrt(d1 * d1 + d2 * d2) / 2;
			if (cornerX == 0)
			{
				sX = -sX;
			}
		}
		{
			float d1 = x - cornerX;
			float d2 = y - y1;
			sY = FloatMath.sqrt(d1 * d1 + d2 * d2) / 2;
			if (cornerY == 0)
			{
				sY = -sY;
			}
		}

		myFgPath.rewind();
		myFgPath.moveTo(x, y);
		myFgPath.lineTo((x + cornerX) / 2, (y + y1) / 2);
		myFgPath.quadTo(cornerX, y1, cornerX, y1 - sY);
		
		if (Math.abs(y1 - sY - cornerY) < height)
		{
			myFgPath.lineTo(cornerX, oppositeY);
		}
		
		myFgPath.lineTo(oppositeX, oppositeY);
		
		if (Math.abs(x1 - sX - cornerX) < width)
		{
			myFgPath.lineTo(oppositeX, cornerY);
		}
		
		myFgPath.lineTo(x1 - sX, cornerY);
		myFgPath.quadTo(x1, cornerY, (x + x1) / 2, (y + cornerY) / 2);

		myQuadPath.moveTo(x1 - sX, cornerY);
		myQuadPath.quadTo(x1, cornerY, (x + x1) / 2, (y + cornerY) / 2);
		
		canvas.drawPath(myQuadPath, myEdgePaint);
		
		myQuadPath.rewind();
		myQuadPath.moveTo((x + cornerX) / 2, (y + y1) / 2);
		myQuadPath.quadTo(cornerX, y1, cornerX, y1 - sY);
		
		canvas.drawPath(myQuadPath, myEdgePaint);
		
		myQuadPath.rewind();

		canvas.save();
		canvas.clipPath(myFgPath);
		canvas.drawBitmap(image, 0, 0, myPaint);
		canvas.restore();
        
		myEdgePaint.setColor(getAverageColor(image));
        
		myEdgePath.rewind();
		myEdgePath.moveTo(x, y);
		myEdgePath.lineTo((x + cornerX) / 2, (y + y1) / 2);
		myEdgePath.quadTo
		(
			(x + 3 * cornerX) / 4,
			(y + 3 * y1) / 4,
			(x + 7 * cornerX) / 8,
			(y + 7 * y1 - 2 * sY) / 8
		);
		myEdgePath.lineTo
		(
			(x + 7 * x1 - 2 * sX) / 8,
			(y + 7 * cornerY) / 8
		);
		myEdgePath.quadTo
		(
			(x + 3 * x1) / 4,
			(y + 3 * cornerY) / 4,
			(x + x1) / 2,
			(y + cornerY) / 2
		);

		canvas.drawPath(myEdgePath, myEdgePaint);
		
		canvas.save();
		canvas.clipPath(myEdgePath);
		Matrix m = new Matrix();
		m.postScale(1, -1);
		m.postTranslate(x - cornerX, y + cornerY);
		float angle;
		if (cornerY == 0)
		{
			angle = -180 / 3.1416f * (float)Math.atan2(x - cornerX, y - y1);
		}
		else
		{
			angle = 180 - 180 / 3.1416f * (float)Math.atan2(x - cornerX, y - y1);
		}
		
		m.postRotate(angle, x, y);
		canvas.drawBitmap(image, m, myBackPaint);
		canvas.restore();
	}

	@Override
	protected void onAnimatedScrolling() {
		if (!getMode().auto)
		{
			return;
		}
		
		int speed = (int) Math.abs(this.speed);
		this.speed *= mySpeedFactor;
		
		int cornerX = startX > width / 2 ? width : 0;
		int cornerY = startY > height / 2 ? height : 0;
		
		int boundX, boundY;
		if (getMode() == Mode.AnimatedScrollingForward)
		{
			boundX = cornerX == 0 ? 2 * width : -width;
			boundY = cornerY == 0 ? 2 * height : -height;
		}
		else
		{
			boundX = cornerX;
			boundY = cornerY;
		}

		int deltaX = Math.abs(endX - cornerX);
		int deltaY = Math.abs(endY - cornerY);
		int speedX, speedY;
		if (deltaX == 0 || deltaY == 0)
		{
			speedX = speedY = speed;
		}
		else if (deltaX < deltaY)
		{
			speedY = (speedX = speed) * deltaY / deltaX;
		}
		else
		{
			speedX = (speedY = speed) * deltaX / deltaY;
		}

		boolean xSpeedIsPositive, ySpeedIsPositive;
		if (getMode() == Mode.AnimatedScrollingForward)
		{
			xSpeedIsPositive = cornerX == 0;
			ySpeedIsPositive = cornerY == 0;
		}
		else
		{
			xSpeedIsPositive = cornerX != 0;
			ySpeedIsPositive = cornerY != 0;
		}

		if (xSpeedIsPositive)
		{
			endX += speedX;
			if (endX >= boundX)
			{
				stop();
			}
		}
		else
		{
			endX -= speedX;
			if (endX <= boundX)
			{
				stop();
			}
		}

		if (ySpeedIsPositive)
		{
			endY += speedY;
			if (endY >= boundY)
			{
				stop();
			}
		}
		else
		{
			endY -= speedY;
			if (endY <= boundY)
			{
				stop();
			}
		}
	}

	@Override
	protected void setupAnimatedScrolling(Integer x, Integer y) {
		if (x == null || y == null)
		{
			if (direction.isHorizontal)
			{
				x = speed < 0 ? width - 3 : 3;
				y = 1;
			}
			else
			{
				x = 1;
				y = speed < 0 ? height - 3 : 3;
			}
		}
		else
		{
			int cornerX = x > width / 2 ? width : 0;
			int cornerY = y > height / 2 ? height : 0;
			int deltaX = Math.min(Math.abs(x - cornerX), width / 5);
			int deltaY = Math.min(Math.abs(y - cornerY), height / 5);
			if (direction.isHorizontal)
			{
				deltaY = Math.min(deltaY, deltaX / 3);
			}
			else
			{
				deltaX = Math.min(deltaX, deltaY / 3);
			}
			
			x = Math.abs(cornerX - deltaX);
			y = Math.abs(cornerY - deltaY);
		}
		
		endX = startX = x;
		endY = startY = y;
	}

	@Override
	protected void startAnimatedScrolling(int speed) {
		mySpeedFactor = (float)Math.pow(2.0, 0.25 * speed);
		onAnimatedScrolling();
	}
	
	/**
	 * 取图片的平均色值
	 * @param image
	 * @return
	 */

	private int getAverageColor(Bitmap image)
	{
		int w = Math.min(image.getWidth(), 7);
		int h = Math.min(image.getHeight(), 7);
		long r = 0, g = 0, b = 0;
		for (int i = 0; i < w; ++i)
		{
			for (int j = 0; j < h; ++j)
			{
				int color = image.getPixel(i, j);
				r += color & 0xFF0000;
				g += color & 0xFF00;
				b += color & 0xFF;
			}
		}
		
		r /= w * h;
		g /= w * h;
		b /= w * h;
		r >>= 16;
		g >>= 8;
			
		return Color.rgb((int)(r & 0xFF), (int)(g & 0xFF), (int)(b & 0xFF));
	}
}