package INF1771_GameAI;
import java.util.ArrayList;

public class Node {
	private int iIndex;
	private int iVal;
	private int iFloatFactor;
	private ArrayList<Integer> idxNeighbors;
	
	public Node (int val){
		iVal = val;
		iFloatFactor = 1;
		idxNeighbors = new ArrayList<Integer>();
	}
	
	public Node (int val, int factor){
		iVal = val;
		iFloatFactor = factor;
		idxNeighbors = new ArrayList<Integer>();
	}
	public void setIndex(int ind){
		iIndex = ind;
	}
	public int getVal(){
		return iVal;
	}
	
	public int getFactor(){//If used with Float Values, RealVal = iVal/iFloatFactor
		return iFloatFactor;
	}
	
	public int getIndex(){
		return iIndex;
	}
	
	public void setNeighbor (int index){
		idxNeighbors.add(index);
	}
	
	public ArrayList<Integer> getNeighborList (){
		return idxNeighbors;
	}
}
