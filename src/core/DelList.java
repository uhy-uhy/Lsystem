package core;
import java.util.ArrayDeque;
import java.util.Deque;

//
public class DelList {
	public Deque<CellState> queue = new ArrayDeque<CellState>();
	public void add(CellState c){
		queue.offer(c);
	}
	public void delete(){
		while(!queue.isEmpty()) {
			queue.poll().dead();
		}
	}
	public void clear(){
		queue.clear();
	}
}
