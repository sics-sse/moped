package se.sics.sse.fresta.wirelessino;

import android.graphics.Rect;

public class Ball {

	private Rect rect;
	
	private int initL,initT;
	private int moveType;
	public static final int LEFT_BAR=0,RIGHT_BAR=1;
	
	
	public Ball(int l, int t, int r, int b, int movement){
		initL=l;
		initT=t;
		rect=new Rect(l,t,r,b);
		moveType=movement;
		
	}
	
	public void moveToCenter(){
		rect.offsetTo(initL, initT);
	}
	
	public Rect getRect(){
		return rect;
	}
	
	public boolean puedoMover(int x, int y, Rect sc){
		if(moveType==RIGHT_BAR)
			return sc.left<(rect.centerX()+x)&&sc.right>rect.centerX()+x;//sc.intersects(rect.left + x, rect.top, rect.right + x, rect.bottom);
		else{
			return (sc.top<(rect.centerY()+y)&& sc.bottom>(rect.centerY()+y));
		}
	}
	
	public int getmoveType(){
		return moveType;
	}
	
	public void move(int x, int y) {
		if(moveType==RIGHT_BAR)
			rect.offsetTo(rect.left+x, rect.top);
		else
			rect.offsetTo(rect.left, rect.top+y);
	}
}
