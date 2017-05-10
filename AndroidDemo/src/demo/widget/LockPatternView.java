package demo.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Debug;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import demo.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 图案锁屏<br>
 * Displays and detects the user's unlock attempt, which is a drag of a finger
 * across 9 regions of the screen.
 * 
 * @author Daimon
 * @version 4.0
 * @since 4/24/2013
 */

public class LockPatternView extends View {

    private static final boolean PROFILE_DRAWING = false;
    private boolean drawingProfilingStarted = false;

    private Paint paint = new Paint();
    private Paint pathPaint = new Paint();

    /**
     * How many milliseconds we spend animating each circle of a lock pattern
     * if the animating mode is set. The entire animation should take this
     * constant * the length of the pattern to complete.
     */
    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;

    private OnPatternListener onPatternListener;

    private ArrayList<Cell> patterns = new ArrayList<Cell>(Cell.ROW * Cell.COL);

    /**
     * Lookup table for the circles of the pattern we are currently drawing.
     * This will be the cells of the complete pattern unless we are animating,
     * in which case we use this to hold the cells we are drawing for the in
     * progress animation.
     */
    private boolean[][] patternsDrawLookup = new boolean[Cell.ROW][Cell.COL];

    /**
     * the in progress point:
     * - during interaction: where the user's finger is
     * - during animation: the current tip of the animating line
     */
    private float inProgressX = -1;
    private float inProgressY = -1;

    private long animatingPeriodStart;

    private DisplayMode patternDisplayMode = DisplayMode.Correct;
    private boolean inputEnabled = true;
    private boolean stealthMode = false;
    private boolean enableHapticFeedback = true;

    private boolean patternInProgress = false;

    private static final float diameterFactor = 0.1f;
    private static final int pathStrokeAlpha = 128;
    private static final float hitFactor = 0.6f;

    private float squareWidth;
    private float squareHeight;

    private Bitmap bitmapBtnDefault;
    private Bitmap bitmapBtnTouched;
    private Bitmap bitmapCircleDefault;
    private Bitmap bitmapCircleGreen;
    private Bitmap bitmapCircleRed;

    private Bitmap bitmapArrowGreenUp;
    private Bitmap bitmapArrowRedUp;

    private final Path currentPath = new Path();
    private final Rect invalidateRect = new Rect();

    private int bitmapWidth;
    private int bitmapHeight;

    private final Matrix arrowMatrix = new Matrix();
    private final Matrix circleMatrix = new Matrix();

    /**
     * Represents a cell in the 3 X 3 matrix of the unlock pattern view.
     */

    public static final class Cell {

        public static final int ROW = 3;
        public static final int COL = 3;

        private static final Cell[][] cells = new Cell[ROW][COL];

        static
        {
            for (int i = 0; i < ROW; i++)
            {
                for (int j = 0; j < COL; j++)
                {
                    cells[i][j] = new Cell(i, j);
                }
            }
        }

        int row;
        int col;

        private Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public static Cell getCell(int row, int col) {
            checkRange(row, col);
            return cells[row][col];
        }

        private static void checkRange(int row, int col) {
            if (row < 0 || row >= ROW)
            {
                throw new IllegalArgumentException(String.format("row must be in range 0-%d",
                        ROW - 1));
            }

            if (col < 0 || col >= COL)
            {
                throw new IllegalArgumentException(String.format("column must be in range 0-%d",
                        COL - 1));
            }
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        @Override
        public String toString() {
            return String.format("Cell[%d,%d]", row, col);
        }
    }

    /**
     * How to display the current pattern.
     */

    public static enum DisplayMode {

        /**
         * The pattern drawn is correct (i.e draw it in a friendly color)
         */
        Correct,

        /**
         * Animate the pattern (for demo, and help).
         */
        Animation,

        /**
         * The pattern is wrong (i.e draw a foreboding color)
         */
        Wrong
    }

    /**
     * The callback interface for detecting patterns entered by the user.
     */

    public static interface OnPatternListener {

        /**
         * The user extended the pattern currently being drawn by one cell.
         * 
         * @param pattern The pattern with newly added cell.
         */
        void onPatternCellAdded(List<Cell> pattern);

        /**
         * A new pattern has begun after the first cell being hit and added.
         */
        void onPatternStart();

        /**
         * A pattern was detected from the user.
         * 
         * @param pattern The pattern.
         */
        void onPatternDetected(List<Cell> pattern);

        /**
         * The pattern was cleared.
         */
        void onPatternCleared();
    }

    public LockPatternView(Context context) {
        super(context);
        init();
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LockPatternView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setClickable(true);

        pathPaint.setAntiAlias(true);
        pathPaint.setDither(true);
        pathPaint.setColor(Color.WHITE);
        pathPaint.setAlpha(pathStrokeAlpha);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);

        bitmapBtnDefault = getBitmap(R.drawable.lock_pattern_btn_default);
        bitmapBtnTouched = getBitmap(R.drawable.lock_pattern_btn_touched);
        bitmapCircleDefault = getBitmap(R.drawable.lock_pattern_circle_default);
        bitmapCircleGreen = getBitmap(R.drawable.lock_pattern_circle_green);
        bitmapCircleRed = getBitmap(R.drawable.lock_pattern_circle_red);

        bitmapArrowGreenUp = getBitmap(R.drawable.lock_pattern_arrow_green);
        bitmapArrowRedUp = getBitmap(R.drawable.lock_pattern_arrow_red);

        // bitmaps have the size of the largest bitmap in this group
        final Bitmap[] bitmaps = { bitmapBtnDefault, bitmapBtnTouched, bitmapCircleDefault,
                bitmapCircleGreen, bitmapCircleRed };

        for (Bitmap bitmap : bitmaps)
        {
            bitmapWidth = Math.max(bitmapWidth, bitmap.getWidth());
            bitmapHeight = Math.max(bitmapHeight, bitmap.getHeight());
        }
    }

    private Bitmap getBitmap(int resId) {
        return BitmapFactory.decodeResource(getResources(), resId);
    }

    public void enableInput() {
        inputEnabled = true;
    }

    /**
     * Disable input (for instance when displaying a message that will timeout
     * so user doesn't get view into messy state).
     */

    public void disableInput() {
        inputEnabled = false;
    }

    public boolean isStealthMode() {
        return stealthMode;
    }

    /**
     * If true, there will be no visible feedback as the user enters the
     * pattern.
     */

    public void setStealthMode(boolean stealthMode) {
        this.stealthMode = stealthMode;
    }

    /**
     * Set whether the view will use tactile feedback. If true, there will be
     * tactile feedback as the user enters the pattern.
     */

    @Override
    public void setHapticFeedbackEnabled(boolean hapticFeedbackEnabled) {
        enableHapticFeedback = hapticFeedbackEnabled;
    }

    @Override
    public boolean isHapticFeedbackEnabled() {
        return enableHapticFeedback;
    }

    /**
     * Set the callback for pattern detection.
     */

    public void setOnPatternListener(OnPatternListener onPatternListener) {
        this.onPatternListener = onPatternListener;
    }

    /**
     * Set the pattern explicitly (rather than waiting for the user to input a
     * pattern).
     */

    public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
        patterns.clear();
        patterns.addAll(pattern);
        clearPatternsDrawLookup();
        for (Cell cell : pattern)
        {
            patternsDrawLookup[cell.row][cell.col] = true;
        }

        setDisplayMode(displayMode);
    }

    /**
     * Set the display mode of the current pattern. This can be useful, for
     * instance, after detecting a pattern to tell this view whether change the
     * in progress result to correct or wrong.
     */

    public void setDisplayMode(DisplayMode displayMode) {
        if ((patternDisplayMode = displayMode) == DisplayMode.Animation)
        {
            if (patterns.isEmpty())
            {
                throw new IllegalArgumentException("you must have a pattern to "
                        + "animate if you want to set the display mode to Animation");
            }

            animatingPeriodStart = SystemClock.elapsedRealtime();
            final Cell first = patterns.get(0);
            inProgressX = getCenterX(first.col);
            inProgressY = getCenterY(first.row);
            clearPatternsDrawLookup();
        }

        invalidate();
    }

    /**
     * Clear the pattern.
     */

    public void clearPattern() {
        resetPattern();
    }

    /**
     * Reset all pattern state.
     */

    private void resetPattern() {
        patterns.clear();
        clearPatternsDrawLookup();
        patternDisplayMode = DisplayMode.Correct;
        invalidate();
    }

    /**
     * Clear the pattern lookup table.
     */

    private void clearPatternsDrawLookup() {
        for (int i = 0; i < Cell.ROW; i++)
        {
            for (int j = 0; j < Cell.COL; j++)
            {
                patternsDrawLookup[i][j] = false;
            }
        }
    }

    private float getCenterX(int col) {
        return getPaddingLeft() + col * squareWidth + squareWidth / 2;
    }

    private float getCenterY(int row) {
        return getPaddingTop() + row * squareHeight + squareHeight / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDesiredSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = getDesiredSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        width = height = Math.min(width, height);
        setMeasuredDimension(width, height);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return Cell.COL * bitmapWidth;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        // View should be large enough to contain 3 side-by-side target bitmaps
        return Cell.ROW * bitmapHeight;
    }

    private int getDesiredSize(int size, int measureSpec)
    {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.AT_MOST:
                result = Math.min(size, specSize);
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }

        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int width = w - getPaddingLeft() - getPaddingRight();
        squareWidth = width * 1.0f / Cell.COL;

        final int height = h - getPaddingTop() - getPaddingBottom();
        squareHeight = height * 1.0f / Cell.ROW;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final ArrayList<Cell> pattern = patterns;
        final int count = pattern.size();
        final boolean[][] drawLookup = patternsDrawLookup;

        if (patternDisplayMode == DisplayMode.Animation)
        {
            // figure out which circles to draw
            // +1 so we pause on complete pattern
            final int oneCycle = (count + 1) * MILLIS_PER_CIRCLE_ANIMATING;
            final int spotInCycle = (int) (SystemClock.elapsedRealtime() - animatingPeriodStart)
                    % oneCycle;
            final int numCircles = spotInCycle / MILLIS_PER_CIRCLE_ANIMATING;

            clearPatternsDrawLookup();
            for (int i = 0; i < numCircles; i++)
            {
                final Cell cell = pattern.get(i);
                drawLookup[cell.row][cell.col] = true;
            }

            // figure out in progress portion of ghosting line
            final boolean needToUpdateInProgressPoint = numCircles > 0 && numCircles < count;
            if (needToUpdateInProgressPoint)
            {
                final float percentageOfNextCircle = ((float) (spotInCycle % MILLIS_PER_CIRCLE_ANIMATING))
                        / MILLIS_PER_CIRCLE_ANIMATING;

                final Cell currentCell = pattern.get(numCircles - 1);
                final float centerX = getCenterX(currentCell.col);
                final float centerY = getCenterY(currentCell.row);

                final Cell nextCell = pattern.get(numCircles);
                final float dx = percentageOfNextCircle * (getCenterX(nextCell.col) - centerX);
                final float dy = percentageOfNextCircle * (getCenterY(nextCell.row) - centerY);
                inProgressX = centerX + dx;
                inProgressY = centerY + dy;
            }

            // Infinite loop here...
            invalidate();
        }

        final float squareWidth = this.squareWidth;
        final float squareHeight = this.squareHeight;

        float radius = (squareWidth * diameterFactor * 0.5f);
        pathPaint.setStrokeWidth(radius);

        final Path currentPath = this.currentPath;
        currentPath.rewind();

        // draw the circles
        final int paddingLeft = getPaddingLeft();
        final int paddingTop = getPaddingTop();

        for (int i = 0; i < Cell.ROW; i++)
        {
            float top = paddingTop + i * squareHeight;
            for (int j = 0; j < Cell.COL; j++)
            {
                float left = paddingLeft + j * squareWidth;
                drawCircle(canvas, (int) left, (int) top, drawLookup[i][j]);
            }
        }

        // the path should be created and cached every time we hit-detect a cell
        // only the last segment of the path should be computed here
        // draw the path of the pattern (unless the user is in progress, and we
        // are in stealth mode)
        final boolean drawPath = (!stealthMode || patternDisplayMode == DisplayMode.Wrong);

        if (drawPath)
        {
            // draw the arrows associated with the path
            boolean isFilterBitmap = paint.isFilterBitmap();
            paint.setFilterBitmap(true);// draw with higher quality since we
                                        // render with transforms

            for (int i = 0; i < count - 1; i++)
            {
                Cell cell = pattern.get(i);
                Cell next = pattern.get(i + 1);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if (!drawLookup[next.row][next.col])
                {
                    break;
                }

                float left = paddingLeft + cell.col * squareWidth;
                float top = paddingTop + cell.row * squareHeight;
                drawArrow(canvas, left, top, cell, next);
            }

            paint.setFilterBitmap(isFilterBitmap);// restore default flag

            boolean hasAnyCircles = false;
            for (int i = 0; i < count; i++)
            {
                Cell cell = pattern.get(i);

                // only draw the part of the pattern stored in
                // the lookup table (this is only different in the case
                // of animation).
                if (!drawLookup[cell.row][cell.col])
                {
                    break;
                }

                hasAnyCircles = true;
                float centerX = getCenterX(cell.col);
                float centerY = getCenterY(cell.row);
                if (i == 0)
                {
                    currentPath.moveTo(centerX, centerY);
                }
                else
                {
                    currentPath.lineTo(centerX, centerY);
                }
            }

            // add last in progress section
            if ((patternInProgress || patternDisplayMode == DisplayMode.Animation) && hasAnyCircles)
            {
                currentPath.lineTo(inProgressX, inProgressY);
            }

            canvas.drawPath(currentPath, pathPaint);
        }
    }

    private void drawCircle(Canvas canvas, int left, int top, boolean partOfPattern) {
        Bitmap outerCircle;
        Bitmap innerCircle;

        if (!partOfPattern || (stealthMode && patternDisplayMode != DisplayMode.Wrong))
        {
            // unselected circle
            outerCircle = bitmapCircleDefault;
            innerCircle = bitmapBtnDefault;
        }
        else if (patternInProgress)
        {
            // user is in middle of drawing a pattern
            outerCircle = bitmapCircleGreen;
            innerCircle = bitmapBtnTouched;
        }
        else if (patternDisplayMode == DisplayMode.Wrong)
        {
            // the pattern is wrong
            outerCircle = bitmapCircleRed;
            innerCircle = bitmapBtnDefault;
        }
        else if (patternDisplayMode == DisplayMode.Correct
                || patternDisplayMode == DisplayMode.Animation)
        {
            // the pattern is correct
            outerCircle = bitmapCircleGreen;
            innerCircle = bitmapBtnDefault;
        }
        else
        {
            throw new IllegalStateException("unknown display mode " + patternDisplayMode);
        }

        final int bitmapWidth = this.bitmapWidth;
        final int bitmapHeight = this.bitmapHeight;

        final float squareWidth = this.squareWidth;
        final float squareHeight = this.squareHeight;

        final int offsetX = (int) ((squareWidth - bitmapWidth) / 2);
        final int offsetY = (int) ((squareHeight - bitmapHeight) / 2);

        // Allow circles to shrink if the view is too small to hold them.
        float scaleX = Math.min(squareWidth / bitmapWidth, 1);
        float scaleY = Math.min(squareHeight / bitmapHeight, 1);

        circleMatrix.setTranslate(left + offsetX, top + offsetY);
        circleMatrix.preTranslate(bitmapWidth / 2, bitmapHeight / 2);
        circleMatrix.preScale(scaleX, scaleY);
        circleMatrix.preTranslate(-bitmapWidth / 2, -bitmapHeight / 2);

        canvas.drawBitmap(outerCircle, circleMatrix, paint);
        canvas.drawBitmap(innerCircle, circleMatrix, paint);
    }

    private void drawArrow(Canvas canvas, float left, float top, Cell start, Cell end) {
        boolean green = patternDisplayMode != DisplayMode.Wrong;

        final int startRow = start.row;
        final int startCol = start.col;
        final int endRow = end.row;
        final int endCol = end.col;

        final int bitmapWidth = this.bitmapWidth;
        final int bitmapHeight = this.bitmapHeight;

        final float squareWidth = this.squareWidth;
        final float squareHeight = this.squareHeight;

        // offsets for centering the bitmap in the cell
        final int offsetX = (int) ((squareWidth - bitmapWidth) / 2);
        final int offsetY = (int) ((squareHeight - bitmapHeight) / 2);

        // compute transform to place arrow bitmaps at correct angle inside
        // circle.
        // This assumes that the arrow image is drawn at 12:00 with it's top
        // edge
        // consistent with the circle bitmap's top edge.
        final Bitmap arrow = green ? bitmapArrowGreenUp : bitmapArrowRedUp;
        final float cellWidth = bitmapWidth;
        final float cellHeight = bitmapHeight;

        // the up arrow bitmap is at 12:00, so find the rotation from x axis and
        // add 90 degrees.
        final float theta = (float) Math.atan2((double) (endRow - startRow),
                (double) (endCol - startCol));
        final float angle = (float) Math.toDegrees(theta) + 90;

        // compose matrix
        float scaleX = Math.min(squareWidth / bitmapWidth, 1);
        float scaleY = Math.min(squareHeight / bitmapHeight, 1);

        arrowMatrix.setTranslate(left + offsetX, top + offsetY);// transform to
                                                                // cell position
        arrowMatrix.preTranslate(bitmapWidth / 2, bitmapHeight / 2);
        arrowMatrix.preScale(scaleX, scaleY);
        arrowMatrix.preTranslate(-bitmapWidth / 2, -bitmapHeight / 2);
        arrowMatrix.preRotate(angle, cellWidth / 2, cellHeight / 2);// rotate
                                                                    // about
                                                                    // cell
                                                                    // center
        arrowMatrix.preTranslate((cellWidth - arrow.getWidth()) / 2, 0);// translate
                                                                        // to
                                                                        // 12:00
        canvas.drawBitmap(arrow, arrowMatrix, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !inputEnabled)
        {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                if (patternInProgress)
                {
                    patternInProgress = false;
                    resetPattern();
                    notifyPatternCleared();
                }

                if (PROFILE_DRAWING)
                {
                    if (drawingProfilingStarted)
                    {
                        Debug.stopMethodTracing();
                        drawingProfilingStarted = false;
                    }
                }

                break;

            default:
                return false;
        }

        return true;
    }

    private void handleActionDown(MotionEvent event) {
        resetPattern();
        final float x = event.getX();
        final float y = event.getY();
        final Cell hitCell = detectAndAddHit(x, y);
        if (hitCell != null)
        {
            patternInProgress = true;
            patternDisplayMode = DisplayMode.Correct;
            notifyPatternStarted();
        }
        else
        {
            patternInProgress = false;
            notifyPatternCleared();
        }

        if (hitCell != null)
        {
            final float centerX = getCenterX(hitCell.col);
            final float centerY = getCenterY(hitCell.row);

            final float widthOffset = squareWidth / 2;
            final float heightOffset = squareHeight / 2;

            invalidate((int) (centerX - widthOffset),
                    (int) (centerY - heightOffset),
                    (int) (centerX + widthOffset),
                    (int) (centerY + heightOffset));
        }

        inProgressX = x;
        inProgressY = y;

        if (PROFILE_DRAWING)
        {
            if (!drawingProfilingStarted)
            {
                Debug.startMethodTracing("LockPatternView");
                drawingProfilingStarted = true;
            }
        }
    }

    /**
     * Determines whether the point x, y will add a new point to the current
     * pattern (in addition to finding the cell, also makes heuristic choices
     * such as filling in gaps based on current pattern).
     * 
     * @return
     */

    private Cell detectAndAddHit(float x, float y) {
        final Cell cell = checkForNewHit(x, y);
        if (cell != null)
        {
            // check for gaps in existing pattern
            Cell fillInGapCell = null;
            final ArrayList<Cell> pattern = patterns;
            if (!pattern.isEmpty())
            {
                final Cell lastCell = pattern.get(pattern.size() - 1);
                int dRow = cell.row - lastCell.row;
                int dCol = cell.col - lastCell.col;

                int fillInRow = lastCell.row;
                int fillInColumn = lastCell.col;

                if (Math.abs(dRow) == 2 && Math.abs(dCol) != 1)
                {
                    fillInRow = lastCell.row + ((dRow > 0) ? 1 : -1);
                }

                if (Math.abs(dCol) == 2 && Math.abs(dRow) != 1)
                {
                    fillInColumn = lastCell.col + ((dCol > 0) ? 1 : -1);
                }

                fillInGapCell = Cell.getCell(fillInRow, fillInColumn);
            }

            if (fillInGapCell != null && !patternsDrawLookup[fillInGapCell.row][fillInGapCell.col])
            {
                addCellToPattern(fillInGapCell);
            }

            addCellToPattern(cell);
            if (enableHapticFeedback)
            {
                performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY,
                        HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                | HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
            }
        }

        return cell;
    }

    /**
     * Helper method to find which cell a point maps to
     * 
     * @return Null if no hit
     */

    private Cell checkForNewHit(float x, float y) {
        int row = getHitRow(y);
        if (row < 0)
        {
            return null;
        }

        int col = getHitCol(x);
        if (col < 0)
        {
            return null;
        }

        if (patternsDrawLookup[row][col])
        {
            return null;
        }

        return Cell.getCell(row, col);
    }

    /**
     * Helper method to find the row that y falls into.
     * 
     * @return The row that y falls in, or -1 if it falls in no row.
     */

    private int getHitRow(float y) {
        final float squareHeight = this.squareHeight;
        float hitArea = squareHeight * hitFactor;

        float offset = getPaddingTop() + (squareHeight - hitArea) / 2;
        for (int i = 0; i < Cell.ROW; i++)
        {
            float top = offset + i * squareHeight;
            if (y >= top && y <= top + hitArea)
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Helper method to find the column that x falls into.
     * 
     * @return The column that x falls in, or -1 if it falls in no column.
     */

    private int getHitCol(float x) {
        final float squareWidth = this.squareWidth;
        float hitArea = squareWidth * hitFactor;

        float offset = getPaddingLeft() + (squareWidth - hitArea) / 2;
        for (int i = 0; i < Cell.COL; i++)
        {
            float left = offset + i * squareWidth;
            if (x >= left && x <= left + hitArea)
            {
                return i;
            }
        }

        return -1;
    }

    private void addCellToPattern(Cell newCell) {
        patternsDrawLookup[newCell.row][newCell.col] = true;
        patterns.add(newCell);
        notifyCellAdded();
    }

    private void handleActionMove(MotionEvent event) {
        // Handle all recent motion events so we don't skip any cells even when
        // the device is busy...
        final int historySize = event.getHistorySize();
        for (int i = 0; i < historySize + 1; i++)
        {
            final float x = i < historySize ? event.getHistoricalX(i) : event.getX();
            final float y = i < historySize ? event.getHistoricalY(i) : event.getY();
            final int patternSizePreHitDetect = patterns.size();
            Cell hitCell = detectAndAddHit(x, y);
            final int patternSize = patterns.size();
            if (hitCell != null && patternSize == 1)
            {
                patternInProgress = true;
                notifyPatternStarted();
            }

            // note current x and y for rubber banding of in progress patterns
            final float dx = Math.abs(x - inProgressX);
            final float dy = Math.abs(y - inProgressY);
            if (dx + dy > squareWidth * 0.01f)
            {
                float oldX = inProgressX;
                float oldY = inProgressY;

                inProgressX = x;
                inProgressY = y;

                if (patternInProgress && patternSize > 0)
                {
                    final ArrayList<Cell> pattern = patterns;
                    final float radius = squareWidth * diameterFactor * 0.5f;

                    final Cell lastCell = pattern.get(patternSize - 1);

                    float centerX = getCenterX(lastCell.col);
                    float centerY = getCenterY(lastCell.row);

                    float left;
                    float top;
                    float right;
                    float bottom;

                    final Rect invalidateRect = this.invalidateRect;

                    if (centerX < x)
                    {
                        left = centerX;
                        right = x;
                    }
                    else
                    {
                        left = x;
                        right = centerX;
                    }

                    if (centerY < y)
                    {
                        top = centerY;
                        bottom = y;
                    }
                    else
                    {
                        top = y;
                        bottom = centerY;
                    }

                    // Invalidate between the pattern's last cell and the
                    // current location
                    invalidateRect.set((int) (left - radius), (int) (top - radius),
                            (int) (right + radius), (int) (bottom + radius));

                    if (centerX < oldX)
                    {
                        left = centerX;
                        right = oldX;
                    }
                    else
                    {
                        left = oldX;
                        right = centerX;
                    }

                    if (centerY < oldY)
                    {
                        top = centerY;
                        bottom = oldY;
                    }
                    else
                    {
                        top = oldY;
                        bottom = centerY;
                    }

                    // Invalidate between the pattern's last cell and the
                    // previous location
                    invalidateRect.union((int) (left - radius), (int) (top - radius),
                            (int) (right + radius), (int) (bottom + radius));

                    // Invalidate between the pattern's new cell and the
                    // pattern's previous cell
                    if (hitCell != null)
                    {
                        centerX = getCenterX(hitCell.col);
                        centerY = getCenterY(hitCell.row);

                        if (patternSize >= 2)
                        {
                            // (re-using hitcell for old cell)
                            hitCell = pattern.get(patternSize - 1
                                    - (patternSize - patternSizePreHitDetect));
                            oldX = getCenterX(hitCell.col);
                            oldY = getCenterY(hitCell.row);

                            if (centerX < oldX)
                            {
                                left = centerX;
                                right = oldX;
                            }
                            else
                            {
                                left = oldX;
                                right = centerX;
                            }

                            if (centerY < oldY)
                            {
                                top = centerY;
                                bottom = oldY;
                            }
                            else
                            {
                                top = oldY;
                                bottom = centerY;
                            }
                        }
                        else
                        {
                            left = right = centerX;
                            top = bottom = centerY;
                        }

                        final float widthOffset = squareWidth / 2f;
                        final float heightOffset = squareHeight / 2f;

                        invalidateRect.set((int) (left - widthOffset),
                                (int) (top - heightOffset),
                                (int) (right + widthOffset),
                                (int) (bottom + heightOffset));
                    }

                    invalidate(invalidateRect);
                }
                else
                {
                    invalidate();
                }
            }
        }
    }

    private void handleActionUp(MotionEvent event) {
        // report pattern detected
        if (!patterns.isEmpty())
        {
            patternInProgress = false;
            notifyPatternDetected();
            invalidate();
        }

        if (PROFILE_DRAWING)
        {
            if (drawingProfilingStarted)
            {
                Debug.stopMethodTracing();
                drawingProfilingStarted = false;
            }
        }
    }

    private void notifyCellAdded() {
        if (onPatternListener != null)
        {
            onPatternListener.onPatternCellAdded(new ArrayList<Cell>(patterns));
        }

        // Accessibility description sent when user adds a cell to the pattern.
        sendAccessibilityEventInternal("Cell added");
    }

    private void notifyPatternStarted() {
        if (onPatternListener != null)
        {
            onPatternListener.onPatternStart();
        }

        // Accessibility description sent when user starts drawing a lock
        // pattern.
        sendAccessibilityEventInternal("Pattern started");
    }

    private void notifyPatternDetected() {
        if (onPatternListener != null)
        {
            onPatternListener.onPatternDetected(new ArrayList<Cell>(patterns));
        }

        // Accessibility description sent when user completes drawing a pattern.
        sendAccessibilityEventInternal("Pattern completed");
    }

    private void notifyPatternCleared() {
        if (onPatternListener != null)
        {
            onPatternListener.onPatternCleared();
        }

        // Accessibility description sent when the pattern times out and is
        // cleared.
        sendAccessibilityEventInternal("Pattern cleared");
    }

    private void sendAccessibilityEventInternal(String tip) {
        CharSequence contentDescription = getContentDescription();
        setContentDescription(tip);
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        setContentDescription(contentDescription);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), serialize(patterns),
                patternDisplayMode.ordinal(),
                inputEnabled, stealthMode, enableHapticFeedback);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setPattern(DisplayMode.values()[savedState.patternDisplayMode],
                deserialize(savedState.serializedPattern));
        inputEnabled = savedState.inputEnabled;
        stealthMode = savedState.stealthMode;
        enableHapticFeedback = savedState.enableHapticFeedback;
    }

    public static final String serialize(List<Cell> pattern) {
        if (pattern == null || pattern.isEmpty())
        {
            return "";
        }

        int size = pattern.size();
        byte[] bs = new byte[size];
        for (int i = 0; i < size; i++)
        {
            Cell cell = pattern.get(i);
            bs[i] = (byte) (cell.row * Cell.COL + cell.col);
        }

        return new String(bs);
    }

    public static final List<Cell> deserialize(String s) {
        if (TextUtils.isEmpty(s))
        {
            return new ArrayList<Cell>(0);
        }

        byte[] bs = s.getBytes();
        int size = bs.length;
        List<Cell> pattern = new ArrayList<Cell>(size);
        for (int i = 0; i < size; i++)
        {
            byte b = bs[i];
            pattern.add(Cell.getCell(b / Cell.COL, b % Cell.COL));
        }

        return pattern;
    }

    /**
     * The Parcelable for saving and restoring a lock pattern view.
     */

    private static class SavedState extends BaseSavedState {

        final String serializedPattern;
        final int patternDisplayMode;
        final boolean inputEnabled;
        final boolean stealthMode;
        final boolean enableHapticFeedback;

        /**
         * Constructor called from {@link #CREATOR}
         */

        private SavedState(Parcel source) {
            super(source);
            serializedPattern = source.readString();
            patternDisplayMode = source.readInt();
            inputEnabled = (Boolean) source.readValue(null);
            stealthMode = (Boolean) source.readValue(null);
            enableHapticFeedback = (Boolean) source.readValue(null);
        }

        /**
         * Constructor called in {@link LockPatternView#onSaveInstanceState()}.
         */

        SavedState(Parcelable superState, String serializedPattern, int patternDisplayMode,
                boolean inputEnabled, boolean stealthMode, boolean enableHapticFeedback) {
            super(superState);
            this.serializedPattern = serializedPattern;
            this.patternDisplayMode = patternDisplayMode;
            this.inputEnabled = inputEnabled;
            this.stealthMode = stealthMode;
            this.enableHapticFeedback = enableHapticFeedback;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(serializedPattern);
            dest.writeInt(patternDisplayMode);
            dest.writeValue(inputEnabled);
            dest.writeValue(stealthMode);
            dest.writeValue(enableHapticFeedback);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}