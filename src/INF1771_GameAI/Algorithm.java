package INF1771_GameAI;
import java.util.ArrayList;
import java.util.Hashtable;

public class Algorithm {
	
	private static ArrayList<Node> lchkVisited = new ArrayList<Node>();
	
	
	public static ArrayList<Integer> AStar (Hashtable<Integer, Node> TMap, Node NStart, Node NFinish, int NSize){
		//System.out.println(NStart.getIndex() + "," + NFinish.getIndex());
	    // The set of nodes already evaluated.
		// Non-Full, Non-Ordered
	    ArrayList<Node> closedSet = new ArrayList<Node> ();
	    // The set of currently discovered nodes still to be evaluated.
	    // Initially, only the start node is known.
	    ArrayList<Node> openSet = new ArrayList<Node> ();
	    openSet.add(NStart);
	    // For each node, which node it can most efficiently be reached from.
	    // If a node can be reached from many nodes, cameFrom will eventually contain the
	    // most efficient previous step.
	    // -1 represents NONE
	    ArrayList<Integer> cameFrom = new ArrayList<Integer> ();
	    setupDefaultList(cameFrom, NSize, -1);
	    // For each node, the cost of getting from the start node to that node.
	    // -1 represents infinity
	    ArrayList<Integer> gScore = new ArrayList<Integer> ();
	    setupDefaultList(gScore, NSize, -1);
	    // The cost of going from start to start is zero.
	    gScore.set(NStart.getIndex(), new Integer(0));
	    // For each node, the total cost of getting from the start node to the goal
	    // by passing by that node. That value is partly known, partly heuristic.
	    ArrayList<Integer> fScore = new ArrayList<Integer> ();
	    setupDefaultList(fScore, NSize, -1);
	    // For the first node, that value is completely heuristic.
	    fScore.set(NStart.getIndex(), new Integer(gScore.get(0) + hFunction(NStart, NFinish)));
	
	    while (openSet.size() != 0){
	    	ArrayList<Integer> OSVal = new ArrayList<Integer>();
	    	for (int i = 0; i < openSet.size(); i++){
	    		OSVal.add(new Integer(fScore.get(openSet.get(i).getIndex())));
	    	}
	    	Node nCurrent = openSet.get(minval(OSVal));
	        if (nCurrent == NFinish)
	            break;
	
	        openSet.remove(nCurrent);
	        closedSet.add(nCurrent);
	        for (int i = 0; i< nCurrent.getNeighborList().size(); i++){
	        	int idxNeighbor = nCurrent.getNeighborList().get(i);
	        	Node nNeighbor = TMap.getOrDefault(idxNeighbor, new Node(1));
	        	nNeighbor.setIndex(idxNeighbor);
	        	GameAI.SetNeighbors(nNeighbor);
	            if (findidx(idxNeighbor, closedSet))
	                continue;		// Ignore the neighbor which is already evaluated.
	            // The distance from start to a neighbor
	            int tentative_gScore = gScore.get(nCurrent.getIndex()) + nNeighbor.getVal();
	            if (!findidx(idxNeighbor, openSet))	// Discover a new node
	                openSet.add(nNeighbor);
	            else if (tentative_gScore >= gScore.get(idxNeighbor))
	                continue;		// This is not a better path.
	
	            // This path is the best until now. Record it!
	            cameFrom.set(idxNeighbor, nCurrent.getIndex());
	            gScore.set(idxNeighbor, tentative_gScore);
	            fScore.set(idxNeighbor, gScore.get(idxNeighbor) + hFunction(nNeighbor, NFinish));
	        }
	        //System.out.println(nCurrent.getIndex());
	    }
	    
	    int iPrev = NFinish.getIndex();
	    ArrayList<Integer> lPath = new ArrayList<Integer>();
	    lPath.add(new Integer (iPrev));
	    while (cameFrom.get(iPrev) != -1){
	    	iPrev = cameFrom.get(iPrev);
	    	lPath.add(0, new Integer (iPrev));
	    }
	    lchkVisited.clear();
	    return lPath;
	}
	
	private static void setupDefaultList (ArrayList<Integer> LIST, int SIZE, int DEFAULT){
		for(int i = 0; i < SIZE; i++)
			LIST.add(new Integer(DEFAULT));
	}
	
	public static int hFunction (Node START, Node FINISH){
		int dx = Math.abs((int) START.getIndex()/41 - (int) FINISH.getIndex()/41);
		int dy = Math.abs(START.getIndex()%41 - FINISH.getIndex()%41);
		return 1 * (dx + dy);
	}	
	
	public static int minval (ArrayList<Integer> LIST){
		int idx = 0, min = LIST.get(0);
		for (int i = 0; i < LIST.size(); i++){
			if (LIST.get(i) < min){
				min = LIST.get(i);
				idx = i;
			}
		}
		return idx;
	}
	
	private static boolean findidx (int idx, ArrayList<Node> LIST){
		for (int i = 0; i < LIST.size(); i++){
			if (LIST.get(i).getIndex() == idx)
				return true;
		}
		return false;
	}
	
	private static boolean chkAvailableNeighbors (ArrayList<Node> LMAP, ArrayList<Node> LVISITED, Node N, Node NEND){
		for (int i = 0; i < N.getNeighborList().size(); i++){
			if (LMAP.get(N.getNeighborList().get(i)) == NEND)
				return true;
		}
		
		if (!lchkVisited.containsAll(LVISITED))
			lchkVisited.addAll(LVISITED);
		lchkVisited.add(N);
		//System.out.println(N.getIndex());
		
		for (int i = 0; i < N.getNeighborList().size(); i++){
			if (lchkVisited.contains(LMAP.get(N.getNeighborList().get(i))))
				continue;
			else{
				if(chkAvailableNeighbors(LMAP, LVISITED, LMAP.get(N.getNeighborList().get(i)), NEND))
					return true;
			}
		}
		
		return false;
	}
}
