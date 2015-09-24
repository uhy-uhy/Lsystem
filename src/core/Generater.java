package core;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class Generater {
	public Deque<CellState> cells;
	public Deque<CellState> dead_cells;
	Lsystem ls;

	public Generater(){
		cells = new ArrayDeque<CellState>();
		dead_cells = new ArrayDeque<CellState>();
		ls = new Lsystem(SimulationStatus.Size_X,SimulationStatus.Size_Y,SimulationStatus.Nutrition,SimulationStatus.All_Nutrition);
	}
	public void generate(){
		//一番古い親・・・・・・・・・・一番若い子の幅優先探索の順番でキューに入れていく
		Deque<CellState> queue = new ArrayDeque<CellState>();
		//ルートからスタート
		for(CellState rc:Roots.roots) queue.offer(rc);
		CellState node;
		while(!queue.isEmpty()) {
			node = queue.poll();

			for(CellState childNode : node.children) {
				queue.offer(childNode); //現セルの子セルをキューの末尾に追加
			}
			//ルールを適用
			ls.apply_rule(node); //nullが返ってきた場合、MAP上から消えた（死滅した）
			//nodeがnull →　削除された
			if(node != null){
				if(node.Lstate.equals("d")) dead_cells.offer(node);
				//子の生成
				if(node.Lstate.length() > 2) ls.set_Tree(node);

			}
		}
		
	}
	
	
}
