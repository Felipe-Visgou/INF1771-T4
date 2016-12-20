package INF1771_GameAI;
import java.util.ArrayList;
import java.util.Hashtable;

public class Algorithm {
	
	private static ArrayList<Node> lchkVisited = new ArrayList<Node>();
	
	
	public static ArrayList<Integer> AStar (Hashtable<Integer, Node> TMap, Node NStart, Node NFinish, int NSize){
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
	
	/*public static int hFunction (ArrayList<Node> LMap, Node START, Node FINISH){
		int i, iStart, iFinish, j, jStart, jFinish, iCost=0;
		char last = 'i'; //Forced Start with i
		
		iStart = (int) START.getIndex()/41;
		jStart = START.getIndex()%41;
		iFinish = (int) FINISH.getIndex()/41;
		jFinish = FINISH.getIndex()%41;
		
		i = iStart;
		j = jStart;
		
		while (i != iFinish || j != jFinish){
			int iDif, jDif;
			iDif = iFinish - i;
			jDif = jFinish - j;
			
			if (Math.abs(iDif) > Math.abs(jDif)){
				if (iDif < 0)
					i--;
				else if (iDif > 0)
					i++;
				last = 'i';
			}
			else if (Math.abs(iDif) < Math.abs(jDif)){
				if (jDif < 0)
					j--;
				else if (jDif > 0)
					j++;
				last = 'j';
			}
			else{
				if (iDif == 0)
					break;
				else{
					if (last == 'i'){
						if (jDif < 0)
							j--;
						else if (jDif > 0)
							j++;
						last = 'j';
					}
					else if (last == 'j'){
						if (iDif < 0)
							i--;
						else if (iDif > 0)
							i++;
						last = 'i';
					}
				}
			}
			iCost += LMap.get((i*41) + j).getVal();
		}
		return iCost;
	}*/
	
	/*public static int hFunction (ArrayList<Node> LMap, Node START, Node FINISH){
		int i, iDif, iStart, iFinish, j, jDif, jStart, jFinish, iCost=0, idx, itCost;
		float factor;
		Node nTemp;
		ArrayList<Node> lVisited;
		
		iStart = (int) START.getIndex()/41;
		jStart = START.getIndex()%41;
		iFinish = (int) FINISH.getIndex()/41;
		jFinish = FINISH.getIndex()%41;
		
		nTemp = START;
		idx = START.getIndex();
		lVisited = new ArrayList<Node>();
		
		
		iDif = iFinish - iStart;
		jDif = jFinish - jStart;
		
		while (nTemp != FINISH){
			Node nTemp2 = null, nTemp3 = null, nTemp4 = null;
			lVisited.add(nTemp);
			itCost = 500;
			
			if (nTemp.getIndex() == 906)
				itCost = 500;
			
			
			if (Math.abs(iDif) >= Math.abs(jDif) && iDif < 0){ //Preferably Upwards
				for (i = 0; i < nTemp.getNeighborList().size(); i++){
					
					//Check if Neighbor is up
					lchkVisited.clear();
					if(nTemp.getNeighborList().get(i) == (nTemp.getIndex() - 41) && !lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))) && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){ 
						itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
						nTemp2 = LMap.get(nTemp.getNeighborList().get(i));
						nTemp3 = nTemp2;
						nTemp4 = nTemp3;
					}
				}
				for (i = 0; i < nTemp.getNeighborList().size(); i++){
					if (LMap.get(nTemp.getNeighborList().get(i)) == nTemp2 || lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))))//Skip checked and already visited
						continue;
					if (jDif < 0){//Secondarily Preferably Leftwards
						//Check if Neighbor is left and if its cost is lower
						lchkVisited.clear();
						if (nTemp.getNeighborList().get(i) == (nTemp.getIndex() - 1) && LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){
							itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
							nTemp3 = LMap.get(nTemp.getNeighborList().get(i));
							nTemp4 = nTemp3;
						}
					}
					else if (jDif > 0){//Secondarily Preferably Rightwards
						//Check if Neighbor is right and if its cost is lower
						lchkVisited.clear();
						if (nTemp.getNeighborList().get(i) == (nTemp.getIndex() + 1) && LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){
							itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
							nTemp3 = LMap.get(nTemp.getNeighborList().get(i));
							nTemp4 = nTemp3;
						}
					}
				}
				for (i = 0; i < nTemp.getNeighborList().size(); i++){//Check Others
					if (LMap.get(nTemp.getNeighborList().get(i)) == nTemp3 || LMap.get(nTemp.getNeighborList().get(i)) == nTemp2 || lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))))//Skip checked and already visited
						continue;
					lchkVisited.clear();
					if (LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){//Non-visited easier path
						itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
						nTemp4 = LMap.get(nTemp.getNeighborList().get(i));
					}
				}
			}
			
			
			else if (Math.abs(iDif) >= Math.abs(jDif) && iDif > 0){ //Preferably Downwards
				for (i = 0; i < nTemp.getNeighborList().size(); i++){
					
					//Check if Neighbor is down
					lchkVisited.clear();
					if(nTemp.getNeighborList().get(i) == (nTemp.getIndex() + 41) && !lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))) && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){ 
						itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
						nTemp2 = LMap.get(nTemp.getNeighborList().get(i));
						nTemp3 = nTemp2;
						nTemp4 = nTemp3;
					}
				}
				for (i = 0; i < nTemp.getNeighborList().size(); i++){
					if (LMap.get(nTemp.getNeighborList().get(i)) == nTemp2 || lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))))//Skip checked and already visited
						continue;
					if (jDif < 0){//Secondarily Preferably Leftwards
						//Check if Neighbor is left and if its cost is lower
						lchkVisited.clear();
						if (nTemp.getNeighborList().get(i) == (nTemp.getIndex() - 1) && LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){
							itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
							nTemp3 = LMap.get(nTemp.getNeighborList().get(i));
							nTemp4 = nTemp3;
						}
					}
					else if (jDif > 0){//Secondarily Preferably Rightwards
						//Check if Neighbor is right and if its cost is lower
						lchkVisited.clear();
						if (nTemp.getNeighborList().get(i) == (nTemp.getIndex() + 1) && LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){
							itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
							nTemp3 = LMap.get(nTemp.getNeighborList().get(i));
							nTemp4 = nTemp3;
						}
					}
				}
				for (i = 0; i < nTemp.getNeighborList().size(); i++){//Check Others
					if (LMap.get(nTemp.getNeighborList().get(i)) == nTemp3 || LMap.get(nTemp.getNeighborList().get(i)) == nTemp2 || lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))))//Skip checked and already visited
						continue;
					lchkVisited.clear();
					if (LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){//Non-visited easier path
						itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
						nTemp4 = LMap.get(nTemp.getNeighborList().get(i));
					}
				}
			}
			
			
			else if (Math.abs(iDif) <= Math.abs(jDif) && jDif < 0){ //Preferably Leftwards
				for (i = 0; i < nTemp.getNeighborList().size(); i++){
					
					//Check if Neighbor is left
					lchkVisited.clear();
					if(nTemp.getNeighborList().get(i) == (nTemp.getIndex() - 1) && !lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))) && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){ 
						itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
						nTemp2 = LMap.get(nTemp.getNeighborList().get(i));
						nTemp3 = nTemp2;
						nTemp4 = nTemp3;
					}
				}
				for (i = 0; i < nTemp.getNeighborList().size(); i++){
					if (LMap.get(nTemp.getNeighborList().get(i)) == nTemp2 || lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))))//Skip checked and already visited
						continue;
					if (iDif < 0){//Secondarily Preferably Upwards
						//Check if Neighbor is up and if its cost is lower
						lchkVisited.clear();
						if (nTemp.getNeighborList().get(i) == (nTemp.getIndex() - 41) && LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){
							itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
							nTemp3 = LMap.get(nTemp.getNeighborList().get(i));
							nTemp4 = nTemp3;
						}
					}
					else if (iDif > 0){//Secondarily Preferably Downwards
						//Check if Neighbor is down and if its cost is lower
						lchkVisited.clear();
						if (nTemp.getNeighborList().get(i) == (nTemp.getIndex() + 41) && LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){
							itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
							nTemp3 = LMap.get(nTemp.getNeighborList().get(i));
							nTemp4 = nTemp3;
						}
					}
				}
				for (i = 0; i < nTemp.getNeighborList().size(); i++){//Check Others
					if (LMap.get(nTemp.getNeighborList().get(i)) == nTemp3 || LMap.get(nTemp.getNeighborList().get(i)) == nTemp2 || lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))))//Skip checked and already visited
						continue;
					lchkVisited.clear();
					if (LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){//Non-visited easier path
						itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
						nTemp4 = LMap.get(nTemp.getNeighborList().get(i));
					}
				}
			}
			
			
			else if (Math.abs(iDif) <= Math.abs(jDif) && jDif > 0){ //Preferably Rightwards
				for (i = 0; i < nTemp.getNeighborList().size(); i++){
					
					//Check if Neighbor is right
					lchkVisited.clear();
					if(nTemp.getNeighborList().get(i) == (nTemp.getIndex() + 1) && !lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))) && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){ 
						itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
						nTemp2 = LMap.get(nTemp.getNeighborList().get(i));
						nTemp3 = nTemp2;
						nTemp4 = nTemp3;
					}
				}
				for (i = 0; i < nTemp.getNeighborList().size(); i++){
					if (LMap.get(nTemp.getNeighborList().get(i)) == nTemp2 || lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))))//Skip checked and already visited
						continue;
					if (iDif < 0){//Secondarily Preferably Upwards
						//Check if Neighbor is up and if its cost is lower
						lchkVisited.clear();
						if (nTemp.getNeighborList().get(i) == (nTemp.getIndex() - 41) && LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){
							itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
							nTemp3 = LMap.get(nTemp.getNeighborList().get(i));
							nTemp4 = nTemp3;
						}
					}
					else if (iDif > 0){//Secondarily Preferably Downwards
						//Check if Neighbor is down and if its cost is lower
						lchkVisited.clear();
						if (nTemp.getNeighborList().get(i) == (nTemp.getIndex() + 41) && LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){
							itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
							nTemp3 = LMap.get(nTemp.getNeighborList().get(i));
							nTemp4 = nTemp3;
						}
					}
				}
				for (i = 0; i < nTemp.getNeighborList().size(); i++){//Check Others
					if (LMap.get(nTemp.getNeighborList().get(i)) == nTemp3 || LMap.get(nTemp.getNeighborList().get(i)) == nTemp2 || lVisited.contains(LMap.get(nTemp.getNeighborList().get(i))))//Skip checked and already visited
						continue;
					lchkVisited.clear();
					if (LMap.get(nTemp.getNeighborList().get(i)).getVal() < itCost && chkAvailableNeighbors(LMap, lVisited, LMap.get(nTemp.getNeighborList().get(i)), FINISH)){//Non-visited easier path
						itCost = LMap.get(nTemp.getNeighborList().get(i)).getVal();
						nTemp4 = LMap.get(nTemp.getNeighborList().get(i));
					}
				}
			}
			
			
			
			System.out.println(nTemp4.getIndex());
			nTemp = nTemp4;
			iCost += itCost;
			i = (int) nTemp.getIndex()/41;
			j = nTemp.getIndex()%41;
			iDif = iFinish - i;
			jDif = jFinish - j;
		}
		return iCost;
	}*/
	
	
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
