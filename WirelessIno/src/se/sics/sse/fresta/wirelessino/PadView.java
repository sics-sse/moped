package se.sics.sse.fresta.wirelessino;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import android.content.pm.ActivityInfo;
import android.app.Activity;

public class PadView extends SurfaceView implements Callback, Runnable {
	private boolean run;
	private SurfaceHolder sh;
        private Paint p, pRed, pBlue, pYellow, pControls;
	public Ball balls[]   = new Ball[2];
	private int touchX[]  = new int[balls.length], 
				touchY[]  = new int[balls.length],
				origenY[] = new int[balls.length], 
				origenX[] = new int[balls.length], 
				idMap[]   = new int[balls.length]; // pointerId depends on how many fingers are pressed and can exceed the number of balls. Thus, a mapping is needed.
	private int textSize, w, h;
	public Rect screen, bar1, bar2, notif;
	private Thread tDraw;
	private Bitmap bluinoBMP;
	public static final int UMBRAL_TACTIL = 70;
	private static String canTextL = "", canTextR = "";

        private Activity host;

	public PadView(Context context) {
		super(context);
		sh = getHolder();
		sh.addCallback(this); 
	}

	public void initPaints() {
		p = new Paint();
		p.setColor(Color.WHITE);
		p.setTextAlign(Align.CENTER);
		p.setTypeface(Typeface.createFromAsset(this.getContext().getAssets(),
				"fonts/KellySlab-Regular.ttf"));
		p.setTextSize(textSize);
		p.setAntiAlias(true);
		pRed = new Paint();
		pRed.setColor(Color.RED);
		pRed.setAntiAlias(true);
		pBlue = new Paint();
		pBlue.setColor(Color.BLUE);
		pBlue.setAntiAlias(true);
		pYellow = new Paint();
		pYellow.setColor(Color.YELLOW);
		pYellow.setAntiAlias(true);
		pControls = new Paint();
		pControls.setColor(Color.argb(220, 100, 180, 180));
		pControls.setAntiAlias(true);

	}

	public void onDraw(Canvas canvas) {
		try {
			canvas.drawColor(Color.DKGRAY);
			if (Main.socket == null) {
				canvas.drawCircle(getWidth() / 2, getHeight() / 8, balls[0]
						.getRect().width() / 2, pRed);
				canvas.drawText(
						getContext().getString(R.string.title_not_connected),
						screen.centerX(), (getHeight() / 8) + textSize * 2, p);
			} else if (Main.socket != null) {
				canvas.drawCircle(screen.exactCenterX(), screen.height() / 8,
						balls[0].getRect().width() / 2, pBlue);
				canvas.drawText(getContext()
						.getString(R.string.title_connected), screen.centerX(),
						(getHeight() / 8) + textSize * 2, p);
			} else {
				canvas.drawCircle(getWidth() / 2, getHeight() / 8, balls[0]
						.getRect().width() / 2, pRed);
				canvas.drawText(
						getContext().getString(R.string.title_not_connected),
						screen.centerX(), (getHeight() / 8) + textSize * 2, p);
			}
			canvas.drawRect(bar1, p);
			canvas.drawRect(bar2, p);
			canvas.drawRect(balls[0].getRect(), pControls);
			// canvas.drawCircle(balls[0].getRect().centerX(),
			// balls[0].getRect().centerY(), balls[0].getRect().width() / 2,
			// pControls);
			// canvas.drawRect(balls[1].getRect(),pControls);
			canvas.drawCircle(balls[1].getRect().centerX(), balls[1].getRect()
					.centerY(), balls[1].getRect().width() / 2, pControls);

			canvas.drawBitmap(bluinoBMP, screen.centerX() - w * 7 / 2,
					6 * getHeight() / 8, null);

			// Arndt: this will become a signalling button
			canvas.drawCircle(getWidth() / 4, getHeight() / 8, balls[0]
					  .getRect().width(), pYellow);


			drawText(canvas, p);
		} catch (Exception e) {
			Log.e(Main.TAG, "onDraw - ", e);
		}
	}

	private void drawText(Canvas canvas, Paint paint) {
		canvas.drawText(canTextL, balls[0].getRect().centerX(),
				7 * getHeight() / 8, paint);
		canvas.drawText(canTextR, balls[1].getRect().centerX(),
				7 * getHeight() / 8, paint);
	}

	public Bitmap resizeImage(Context ctx, int resId, int w, int h) {

		// load the origial Bitmap
		Bitmap BitmapOrg = BitmapFactory.decodeResource(ctx.getResources(),
				resId);

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);

		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
		return resizedBitmap;

	}

	@SuppressLint("WrongCall")
	@Override
	public void run() {
		Canvas canvas = null;
		while (run) {
			canvas = null;
			try {
				canvas = sh.lockCanvas(null);
				if (canvas != null) {
					synchronized (sh) {
						onDraw(canvas);
					}
				}
			} finally {
				if (canvas != null)
					sh.unlockCanvasAndPost(canvas);
			}
		}
	}
	
	@SuppressLint("ClickableViewAccessibility")
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		int pointerIndex = event.getActionIndex();
		int pointerId = event.getPointerId(pointerIndex);
		
		switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				if (Main.D) {
					Log.v(Main.TAG,
							"+DragandDropVIEW OnTouchEvent(Action_Down) ID:"
									+ pointerId + " - Index" + pointerIndex);
				}
				
				int x = (int) event.getX(pointerIndex);
				int y = (int) event.getY(pointerIndex);
	
				if (notif.contains(x, y)) {
					if (Main.D)
						Log.i(Main.TAG,
								"+OnTouchEvent(Action_Down) SECREEETtouch!!; ");
					if (Main.socket != null)
						Main.send("S0008T0007");// secret code to run a
														// secret routine in
														// Arduino
				}
	
				/* Check if a new ball should be activated */
				for (int i=0; i<balls.length; i++) {
					Rect aux = new Rect(balls[i].getRect());
					aux.set(aux.left - UMBRAL_TACTIL, aux.top - UMBRAL_TACTIL,
							aux.right + UMBRAL_TACTIL, aux.bottom + UMBRAL_TACTIL);
					if (aux.contains(x, y) && idMap[i] < 0) {
						idMap[i] = pointerId;
						host.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

						origenX[i] = touchX[i] = x;
						origenY[i] = touchY[i] = y;
					}
				}
				
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_CANCEL:
				/* If this was a finger that was not moving on a bar, 
				 * no need to adjust anything, just return */
				int ballId = getBallId(pointerId);
				if (ballId < 0)
					return true;
				
				/* Reset */
				touchX[ballId] = touchY[ballId] = -1;
				idMap[ballId] = -1;
				
				if (idMap[0] == -1 && idMap[1] == -1) {
				    host.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
				}

				balls[ballId].moveToCenter();
				transformPWM();
//				if (balls[ballId].getmoveType() == Ball.RIGHT_BAR) {
//					transformPWM(balls[ballId], bar2, Options
//							.getInstance().getRBarValue());
//				} else {
//					transformPWM(balls[ballId], bar1, Options
//							.getInstance().getLBarValue());
//				}
	
				if (Main.D)
					Log.v(Main.TAG,
							"+DragandDropVIEW OnTouchEvent(Action_Up) ID:"
									+ pointerId + " - Index" + pointerIndex);
				break;
			case MotionEvent.ACTION_MOVE:
				int pointerCount = event.getPointerCount();
	
				for (int i = 0; i < pointerCount; i++) {
					pointerIndex = i;
					pointerId = event.getPointerId(pointerIndex);
					
					/* If this is a finger that is moving on a bar, just skip it */
					ballId = getBallId(pointerId);
					if (ballId < 0)
						continue;
					
					touchX[ballId] = (int) event.getX(pointerIndex);
					touchY[ballId] = (int) event.getY(pointerIndex);
	
					if (balls[ballId].getmoveType() == Ball.RIGHT_BAR) {
						// lastWheelValue =
						// Options.getInstance().getRBarValue();
						if (balls[ballId].puedoMover(touchX[ballId]
								- origenX[ballId], touchY[ballId]
								- origenY[ballId], bar2)) {
							(balls[ballId]).move(touchX[ballId]
									- origenX[ballId], touchY[ballId]
									- origenY[ballId]);
							
							transformPWM(); //balls[ballId], bar2, Options.getInstance().getRBarValue());
						}
	
					} else if (balls[ballId].puedoMover(touchX[ballId]
							- origenX[ballId], touchY[ballId]
							- origenY[ballId], bar1)) {
						(balls[ballId]).move(touchX[ballId]
								- origenX[ballId], touchY[ballId]
								- origenY[ballId]);
						
						transformPWM(); //balls[ballId], bar1, Options.getInstance().getLBarValue());
	
					}
					origenY[ballId] = touchY[ballId];
					origenX[ballId] = touchX[ballId];
				}
				break;
			default:		
		}
		
		return true;
	}
	
	/* 
	 * Checks if the pointerId corresponds to one of the Balls.
	 * If not, -1 is returned.  
	 */
	private int getBallId(int pointerId) {
		for (int i=0; i<idMap.length; i++) {
			if (idMap[i] == pointerId)
				return i;
		}
		
		return -1;
	}

	public synchronized void transformPWM() {
		double dist;
		int x;
		String out = "";
		
		/* Calc and output speed values using the left bar */ 
		dist = bar1.bottom - balls[0].getRect().centerY();
		x = (int) Math.ceil((Options.getInstance().getLBarValue() / (bar1.height() / dist)) - 100);
		if (x < 0)
			x--;
		canTextL = intToString(x);
		out = "V" + canTextL;
		
		int speed = x;

		/* Calc and output steering values using the right bar */
		dist = balls[1].getRect().centerX() - bar2.left;
		x = (int) Math.ceil((Options.getInstance().getRBarValue() / (bar2.width() / dist)) - 100);
		if (x < 0)
			x--;

		double lambda = x/100.0;
		if (lambda < 0)
		    lambda = -lambda;

		double x2 = x*x/100.0;
		if (x < 0)
		    x2 = -x2;

		int x3 = (int) (lambda*x2 + (1-lambda)*x);

		canTextR = intToString(x3);
		out += "H" + canTextR;

		/* Send speed and steering values through the socket */ 
		if (Main.socket != null)
			Main.send(out);
	}

	private String intToString(int x) {
		String padding = "0";
		if (x < 0) {
			padding = "-";
		}

		x = Math.abs(x);
		if (x < 100) {
			padding += "0";
		
			if (x < 10) {
				padding += "0";
			}
		}
		
		return padding + x;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	    host = (Activity) getContext();

	    host.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

		w = getWidth() / 60;
		h = getHeight() / 2;
		int left = getWidth() / 6, top = (getHeight() / 4), right = left + w, bottom = 3 * top;
		textSize = getHeight() / 15;
		initPaints();
		screen = new Rect(0, 0, getWidth(), getHeight());
		bar1 = new Rect(left, top, right, bottom);
		bar2 = new Rect(3 * left, h - w / 2, 5 * left, h + w / 2);
		balls[0] = new Ball(left - w, top - 5 - w + (bottom - top) / 2, right + w,
							  top + 5 + w + (bottom - top) / 2, Ball.LEFT_BAR);
		balls[1] = new Ball(4 * left - w - (w / 2), top - w - 5 + (bottom - top)
							  / 2, 4 * left + w + (w / 2), top + w + 5 + (bottom - top) / 2,
							  Ball.RIGHT_BAR);
		notif = new Rect(screen.centerX() - w - (UMBRAL_TACTIL / 2),
				(screen.height() / 8) - w - 5, (screen.centerX()) + w
						+ (UMBRAL_TACTIL / 2), (screen.height() / 8) + w + 5);

		bluinoBMP = resizeImage(this.getContext(), R.drawable.bluinotooth,
				7 * w, 2 * getHeight() / 8);
		
		for (int i=0; i<idMap.length; i++) {
			idMap[i] = -1;
		}

		tDraw = new Thread(this);
		run = true;
		tDraw.start();

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
 
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (Main.D)
			Log.v(Main.TAG, "+PGVIEW surfaceDestroyed");
		boolean retry = true;
		run = false;
		while (retry) {
			try {
				if (tDraw != null)
					tDraw.join();
				retry = false;
			} catch (InterruptedException e) {
				if (Main.D)
					Log.e(Main.TAG, "PGView-SD: " + e.getMessage());
			}
		}

	}

}