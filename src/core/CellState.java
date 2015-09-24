package core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public class CellState {

	public boolean root = false;
	public boolean next_root = false;
	public boolean dead = false;
	public int state;		//自分の満腹度（0になったら餓死）ということにしておく
	public Color c = Color.black;
	public int direction;   //親から見た方向
	public StringBuffer Lstate = new StringBuffer();
	public int x,y;
	public List<CellState> children = new ArrayList<CellState>();
	public List<CellState> next_children = new ArrayList<CellState>();
	public CellState parent = null;
	public CellState next_parent = null;
	Roots roots = new Roots();

	public CellState(){};
	public CellState(int state,int x,int y){
		this.state = state;
		this.x = x;
		this.y = y;
	}
	public void setRoot(){
		next_root = true;
	}
	public void removeRoot(){
		next_root = false;
	}
	//死滅する際の処理
	public void dead(){
		//子セルの親情報を消す
		//親の居ないセルはrootにする
		if(children.size() > 0){
			for(CellState c:children){
				c.removeParent();
			}
		}
		//親セルの子情報から自セルを消す
		if(parent != null)parent.children.remove(this);
		//子情報を消す
		clearChildren();
		
		dead = true;
		

	}
	
	public boolean onFeed(){
		return(Map.checkFeed(x, y));
	}
	public void setpColor(Color c){
		this.c = c;
	}
	public void setdirection(int d){
		this.direction = d;
	}
	public void setParent(CellState parent){
		this.next_parent = parent;
	}
	public void clearChildren(){
		next_children.clear();
	}
	//親を消す（rootになる）
	public void removeParent(){
		this.next_parent = null;
		roots.setRoot(this);
	}
	public void addChild(CellState child){
		this.next_children.add(child);
	}
	public void resetLstate(){
		this.Lstate.replace(0, Lstate.length(), "");
	}
	public void addLstate(String s){
		this.Lstate.append(s);
	}
	public void removeChild(int index){
		next_children.remove(index);
	}
	public int get_State(){
		return state;
	}
	public void set_State(int state){
		this.state = state;
	}
	public int get_x(){
		return x;
	}
	public int get_y(){
		return y;
	}
	public void set_x(int x){
		this.x = x;;
	}
	public void set_y(int y){
		this.y = y;
	}
	public void replaceLstate(String s) {
		this.Lstate.replace(0, Lstate.length(), s);
	}

	public void update(){
		children = next_children;
		parent = next_parent;
		root = next_root;
		if(dead){
			//MAPから生息情報を消す
			Map.Lmap[x][y].reducePopulation();;
			Map.Lmap[x][y].removeCell(this);
			//rootの場合、rootリストから消える
			if(root) roots.removeRoot(this);
			
			dead = false;
		}
	}
}
