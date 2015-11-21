package serachItem;

import java.util.ArrayList;

import org.apache.batik.bridge.UpdateManager;

public abstract class Cell {
	//状態
	protected String state;
	//更新用状態
	protected String next_state;
	//死滅フラグ
	protected boolean dead = false;

	public Cell(){
	}
	
	
	public String getState(){
		return state;
	}
	public abstract void update();

	public abstract void setState(String state);
}
