package core;
import java.util.ArrayList;


public class Roots{
	public static ArrayList<CellState> roots = new ArrayList<CellState>();
	public Roots(){};
	public void setRoot(CellState cs){
		if(cs.parent == null){
			cs.setRoot();
			roots.add(cs);
		}
		else{
			System.out.println("親がいるのにroot付けてるよ");
		}

		
	}
	public void removeRoot(CellState cs){
		if(cs.root){
			//root属性を子セルに移す
			for(CellState cell:cs.children){
				setRoot(cell);
			}
			//削除
			cs.removeRoot();
			roots.remove(cs);
		}
		else{
			System.out.println("rootじゃないのにroot属性を消そうとしている");
		}
	}


}
