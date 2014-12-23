package se.sics.sse.fresta.wirelessino;
public class Options {

	private static Options instance;
	private int leftBar,rightBar;
	
	public Options(){
		rightBar=leftBar=200;
	}
	
	public static synchronized Options getInstance() {
		if(instance == null)
			instance = new Options();
		return instance;
	}
	
	public int getLBarValue(){
		return leftBar;
	}
	
	public int getRBarValue(){
		return rightBar;
	}
	
	public void setValues(int l, int r){
		leftBar=l;
		rightBar=r;
	}
	
	public void setLBarValue(int n){
		leftBar=n;
	}
	
	public void setRBarValue(int n){
		rightBar=n;
	}
	
}
