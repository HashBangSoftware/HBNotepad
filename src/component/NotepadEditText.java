package component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

public class NotepadEditText extends EditText
{
	private final Rect mRect;
	private final Paint mPaint;

	// This constructor is used by LayoutInflater
	public NotepadEditText(Context context, AttributeSet attrs)
	{
		super(context, attrs);

		// Creates a Rect and a Paint object, and sets the style and color of
		// the Paint object.
		mRect = new Rect();
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(0x800000FF);
	}

	/**
	 * This is called to draw the LinedEditText object
	 * 
	 * @param canvas
	 *            The canvas on which the background is drawn.
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{

		// Gets the number of lines of text in the View.
		final int count = getLineCount();

		// Gets the global Rect and Paint objects
		final Rect r = mRect;
		final Paint paint = mPaint;

		/*
		 * Draws one line in the rectangle for every line of text in the
		 * EditText
		 */
		for (int i = 0; i < count; i++)
		{

			// Gets the baseline coordinates for the current line of text
			final int baseline = getLineBounds(i, r);

			/*
			 * Draws a line in the background from the left of the rectangle to
			 * the right, at a vertical position one dip below the baseline,
			 * using the "paint" object for details.
			 */
			canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
		}

		// Finishes up by calling the parent method
		super.onDraw(canvas);
	}
}
