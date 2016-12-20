package INF1771_GameAI;
import INF1771_GameAI.Map.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.Timer;


public class GameAI
{
	boolean bAllClear = false;
	boolean bLights = false;
	boolean bPicked = false;
    Position player = new Position();
    Position aim = null;
    String state = "ready";
    String dir = "north";
    String lastmove;
    long score = 0;
    int energy = 0;
    final int RESPAWN_DELAY = 30000; /* 30s is the respawn delay */
    private ArrayList<Integer> Lpath = new ArrayList<Integer>();
    private ArrayList<Position> Sunknown = new ArrayList<Position>();
    private ArrayList<Position> treasurePositions = new ArrayList<Position>();
    private ArrayList<Position> pitPositions = new ArrayList<Position>();
    private ArrayList<Position> teleportPositions = new ArrayList<Position>();  
    private Hashtable<Integer, Enum> mapPositions = new Hashtable<Integer, Enum>();
    private Hashtable<Integer, Node> Tmap = new Hashtable<Integer, Node>();
    private ArrayList<Position> powerUpPositions = new ArrayList<Position>();
    private static ArrayList<Respawn> treasure_respawns = new ArrayList<Respawn>();
    private static ArrayList<Respawn> powerUp_respawns = new ArrayList<Respawn>();
    
    /* Game States */
    GameState mapping = new Mapping(this);
    GameState searching_gold = new SearchingGold(this);
    GameState searching_pu = new SearchingPowerUp(this);
    GameState fleeing = new Fleeing(this);
    GameState pursuing = new Pursuing(this);
    GameState striking = new Striking(this);
    GameState roaming = new Roaming(this);
    GameState currentState = mapping;
    
    boolean isBlocked = false;
    boolean isBreezy;
    boolean isBluey;
    boolean isReddy;
    boolean isFlashy;
    boolean isNoisy;
    boolean isEnemy;
    boolean isDamage;
    boolean isHit;
    
    boolean inDanger = false;
    
    int enemyGap;
    /**
     * Refresh player status
     * @param x			player position x
     * @param y			player position y
     * @param dir		player direction
     * @param state		player state
     * @param score		player score
     * @param energy	player energy
     */
    
    /* Get State Methods */
    GameState getMappingState(){
        return this.mapping;
    }
    GameState getSearchingGoldState(){
        return this.searching_gold;
    }
    GameState getSearchingPowerUpState(){
        return this.searching_pu;
    }
    GameState getFleeingState(){
        return this.fleeing;
    }
    GameState getPursuingState(){
        return this.pursuing;
    }
    GameState getStrikingState(){
        return this.striking;
    }
    GameState getCurrentState(){
        return this.currentState;
    }
    void changeState(GameState new_current_state){
    	this.currentState = new_current_state;
    }
       
    /* Enumeration that fills the Hash */
    public enum Tile{
    	Error, Unmapped, Clear, mayPit, Wall, mayTeleport, mayPitport
    }
    
    public static void SetNeighbors(Node N){
    	if (N.getIndex() - 59 > -1)
			N.setNeighbor(N.getIndex() - 59);
		if (N.getIndex() + 59 < 59*34)
			N.setNeighbor(N.getIndex() + 59);
		if (N.getIndex() - 1 > 0 && (N.getIndex()-1)%59 != 58)
			N.setNeighbor(N.getIndex() - 1);
		if (N.getIndex() + 1 < 59*34 && (N.getIndex()+1)%59 != 0)
			N.setNeighbor(N.getIndex() + 1);
		if (N.getNeighborList().size() == 0){
			System.out.println("Fudeu");
		}
    }
    
    public String DoBestMove (Position S, Position F){ //Must be adjacent
    	if (F.x < S.x){
    		System.out.println("Quero West, estou " + dir);
			if (dir.equals("west"))
				return "andar";
			else if (dir.equals("east"))
				return "andar_re";
			else if (dir.equals("north"))
				return "virar_esquerda";
			else
				return "virar_direita";
		}
    	else if (F.x > S.x){
    		System.out.println("Quero East, estou " + dir);
			if (dir.equals("west"))
				return "andar_re";
			else if (dir.equals("east"))
				return "andar";
			else if (dir.equals("north"))
				return "virar_direita";
			else
				return "virar_esquerda";
		}
    	else if (F.y > S.y){
    		System.out.println("Quero South, estou " + dir);
			if (dir.equals("west"))
				return "virar_esquerda";
			else if (dir.equals("east"))
				return "virar_direita";
			else if (dir.equals("north"))
				return "andar_re";
			else
				return "andar";
		}
    	else if (F.y < S.y){
    		System.out.println("Quero North, estou " + dir);
			if (dir.equals("west"))
				return "virar_direita";
			else if (dir.equals("east"))
				return "virar_esquerda";
			else if (dir.equals("north"))
				return "andar";
			else
				return "andar_re";
		}
    	System.out.println("To doidao");
    	return "";
    }
    public void SetStatus(int x, int y, String dir, String state, long score, int energy)
    {
        player.x = x;
        player.y = y;
        this.dir = dir.toLowerCase();

        this.state = state;
        this.score = score;
        this.energy = energy;
    }

    /**
     * Get list of observable adjacent positions
     * @return List of observable adjacent positions 
     */
    public List<Position> GetObservableAdjacentPositions()
    {
        List<Position> ret = new ArrayList<Position>();

        ret.add(new Position(player.x - 1, player.y));
        ret.add(new Position(player.x + 1, player.y));
        ret.add(new Position(player.x, player.y - 1));
        ret.add(new Position(player.x, player.y + 1));

        return ret;
    }

    /**
     * Get list of all adjacent positions (including diagonal)
     * @return List of all adjacent positions (including diagonal)
     */
    public List<Position> GetAllAdjacentPositions()
    {
        List<Position> ret = new ArrayList<Position>();

        ret.add(new Position(player.x - 1, player.y - 1));
        ret.add(new Position(player.x, player.y - 1));
        ret.add(new Position(player.x + 1, player.y - 1));

        ret.add(new Position(player.x - 1, player.y));
        ret.add(new Position(player.x + 1, player.y));

        ret.add(new Position(player.x - 1, player.y + 1));
        ret.add(new Position(player.x, player.y + 1));
        ret.add(new Position(player.x + 1, player.y + 1));

        return ret;
    }

    /**
     * Get next forward position
     * @return next forward position
     */
    public Position NextPosition()
    {
        Position ret = null;
        if(dir.equals("north"))
                ret = new Position(player.x, player.y - 1);
        else if(dir.equals("east"))
                ret = new Position(player.x + 1, player.y);
        else if(dir.equals("south"))
                ret = new Position(player.x, player.y + 1);
        else if(dir.equals("west"))
                ret = new Position(player.x - 1, player.y);

        return ret;
    }

    /**
     * Player position
     * @return player position
     */
    public Position GetPlayerPosition()
    {
        return player;
    }
    
    /**
     * Set player position
     * @param x		x position
     * @param y		y position
     */
    public void SetPlayerPosition(int x, int y)
    {
        player.x = x;
        player.y = y;

    }

    /**
     * Observations received
     * @param o	 list of observations
     */
    public void GetObservations(List<String> o)
    {	
    	Position p = GetPlayerPosition();    	
    	Position n = NextPosition();
    	Boolean bPresence = false;
    	List<Position> Ladj = GetObservableAdjacentPositions();
    	
    	/*Sensor Flags */
    	bLights = false;
    	isDamage = false;
        isEnemy = false;
        isHit = false;
        isBreezy = false;
        isFlashy = false;
        isBluey = false;
        isReddy = false;
        isNoisy = false;
        isBlocked = false;
        enemyGap = -1;
        
        for (String s : o)
        {
        	      	
            if(s.equals("blocked")){    
            	isBlocked = true;
            	Position m = null;
            	if (lastmove.equals("andar_re")){
            		if (dir.equals("east"))
            			m = new Position(player.x-1, player.y);
            		else if (dir.equals("west"))
                        m = new Position(player.x+1, player.y);
            		else if (dir.equals("north"))
                        m = new Position(player.x, player.y+1);
            		else if (dir.equals("south"))
                        m = new Position(player.x, player.y-1);
            	}
            	else
            		m = n;
            	if(isOutofBounds(m.x,m.y))
            		break;
            	
            	Enum flag = mapPositions.getOrDefault(m.y*59 + m.x, Tile.Unmapped);
        		if(flag != Tile.Wall){
        			Node N = new Node(1);
        			N.setIndex(-1);        			
        			SetNeighbors(Tmap.getOrDefault(m.y*59 + m.x, N));
        			mapPositions.put(m.y*59 + m.x, Tile.Wall);
        			Tmap.put(m.y*59 + m.x, new Node(1000000));
        			Tmap.get(m.y*59 + m.x).setIndex(m.y*59 + m.x);
        			SetNeighbors(Tmap.get(m.y*59 + m.x));
        		}
            	
            	System.out.println("Estou blocked");
            	
            } else if(s.equals("steps")){
            	
            	isNoisy = true;
            } else if(s.equals("breeze")){
            	bPresence = true;
            	isBreezy = true;
            	System.out.println("MMM BREEZE");
            	
            	for (int i = 0; i < Ladj.size(); i++){
            		if (!isOutofBounds(Ladj.get(i).y, Ladj.get(i).x)){
            			if (mapPositions.getOrDefault(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped) == Tile.Unmapped){
            				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.mayPit);
            				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1000000));
            				Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
                			SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
                			mapPositions.put(p.y*59 + p.x, Tile.Clear); //I'm clear
                        	Tmap.put(p.y*59 + p.x, new Node(1));
                        	Tmap.get(p.y*59 + p.x).setIndex(p.y*59 + p.x);
                        	SetNeighbors(Tmap.get(p.y*59 + p.x));
            			}
            			else if (mapPositions.getOrDefault(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped) == Tile.mayTeleport){
            				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.mayPitport);
            				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1000000));
            				Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
                			SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
                			mapPositions.put(p.y*59 + p.x, Tile.Clear); //I'm clear
                        	Tmap.put(p.y*59 + p.x, new Node(1));
                        	Tmap.get(p.y*59 + p.x).setIndex(p.y*59 + p.x);
                        	SetNeighbors(Tmap.get(p.y*59 + p.x));
        				}
            		}
            	}
            	
            } else if(s.equals("flash")){
            	bPresence = true;
            	isFlashy = true;
            	System.out.println("WOW FLASHES");
            	
            	for (int i = 0; i < Ladj.size(); i++){
            		if (!isOutofBounds(Ladj.get(i).y, Ladj.get(i).x)){
            			if (mapPositions.getOrDefault(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped) == Tile.Unmapped){
            				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.mayTeleport);
            				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1000000));
            				Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
                			SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
                			mapPositions.put(p.y*59 + p.x, Tile.Clear); //I'm clear
                        	Tmap.put(p.y*59 + p.x, new Node(1));
                        	Tmap.get(p.y*59 + p.x).setIndex(p.y*59 + p.x);
                        	SetNeighbors(Tmap.get(p.y*59 + p.x));
            			}
            			else if (mapPositions.getOrDefault(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped) == Tile.mayPit){
            				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.mayPitport);
            				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1000000));
            				Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
                			SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
                			mapPositions.put(p.y*59 + p.x, Tile.Clear); //I'm clear
                        	Tmap.put(p.y*59 + p.x, new Node(1));
                        	Tmap.get(p.y*59 + p.x).setIndex(p.y*59 + p.x);
                        	SetNeighbors(Tmap.get(p.y*59 + p.x));
        				}
            		}
            	}
            	
            } else if(s.equals("blueLight")){
            	bLights = true;
            	isBluey = true;
            	Position p1 = new Position(player.x, player.y);
            	if(!powerUpPositions.contains(p1)){
            		
            		powerUpPositions.add(new Position(player.x,player.y));
                	powerUp_respawns.add(new Respawn(player.x,player.y));
            	}            	


            } else if(s.equals("redLight")){
            	bLights = true;
            	isReddy = true;
            	Position p1 = new Position(player.x, player.y);
            	if(!treasurePositions.contains(p1)){
            		
                	treasurePositions.add(new Position(player.x,player.y));
                	if (treasurePositions.size() >= 2)
                		currentState = searching_gold;
                	treasure_respawns.add(new Respawn(player.x,player.y));
                	
            	}	
            } else if(s.equals("greenLight")){
            	

            } else if(s.equals("weakLight")){


            } else {            	
            	System.out.println("MAS QUE PORRA EH ESSA: " + s);
				String[] split = s.split("#");
				if (split[0].equals("enemy")) {
						isEnemy = true;
						enemyGap = Integer.parseInt(split[1]);
				}
				else{
					isHit = true;
				}
				
            }
        }
        if (bPresence == false && mapPositions.getOrDefault(p.y*59 + p.x, Tile.Unmapped) != Tile.Wall){ //Didn't feel any Pit or Teleport or Wall
        	mapPositions.put(p.y*59 + p.x, Tile.Clear); //I'm clear
        	Tmap.put(p.y*59 + p.x, new Node(1));
        	Tmap.get(p.y*59 + p.x).setIndex(p.y*59 + p.x);
			SetNeighbors(Tmap.get(p.y*59 + p.x));
        	for (int i = 0; i < Ladj.size(); i++){ //My adjacent tiles are clear of Pit/Teleport
        		if (!isOutofBounds(Ladj.get(i).y, Ladj.get(i).x)){        			
        			if (mapPositions.get(Ladj.get(i).y*59 + Ladj.get(i).x) == Tile.mayPit){
        				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped);
        				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1));
        	        	Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
        				SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
        			}
        			else if (mapPositions.get(Ladj.get(i).y*59 + Ladj.get(i).x) == Tile.mayTeleport){
        				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped);
        				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1));
        	        	Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
        				SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
        			}
        			else if (mapPositions.get(Ladj.get(i).y*59 + Ladj.get(i).x) == Tile.mayPitport){
        				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped);
        				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1));
        	        	Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
        				SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
        			}
        			else if (mapPositions.get(Ladj.get(i).y*59 + Ladj.get(i).x) == Tile.Wall || mapPositions.get(Ladj.get(i).y*59 + Ladj.get(i).x) == Tile.Clear){
            			//System.out.println("Funciona");
        			}
        			else{
        				Sunknown.add(0, Ladj.get(i));
        				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped);
        				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1));
        	        	Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
        				SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
        			}
        		}
        	}
        }

    }

    /**
     * No observations received
     */
    public void GetObservationsClean()
    {
    	List<String> L = new ArrayList<String>();
    	GetObservations(L);
    }

    /**
     * Get Decision
     * @return command string to new decision
     */
    public String GetDecision()
    {
    	if (isNoisy || isEnemy || isDamage)
    		currentState = roaming;

    	String ret = currentState.doAction();
    	lastmove = ret;
    	return ret;
    	
    	
    }
    
    /* Class responsible to gather the items respawn informations */
	private class Respawn implements ActionListener {
		
		Timer item_respawn;
		Position item_position;
		boolean isRespawning;
		
		Respawn(int x, int y){
			item_respawn = new Timer(RESPAWN_DELAY,this);
			item_position = new Position(x,y);
			isRespawning = false;
		}
		void timerStart(){
			item_respawn.start();
			isRespawning = true;
		}
		void timerStop(){
			item_respawn.stop();
			isRespawning = false;
			if (treasurePositions.size() > 1)
				currentState = searching_gold;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			/* When the action performed is activated, it means there's a treasure avaible again 
			 * to pick it up, so we gotta change our bot state priority.(pick the recent respawned item)
			 */
			
			this.timerStop();
			
		}
	}
	boolean isOutofBounds(int x, int y){
		
		if(x < 0)
			return true;
		else if(x > 58)
			return true;
		else if(y < 0)
			return true;
		else if(y > 33)
			return true;
		return false;
	}
	/* State Classes */
	
	class Mapping implements GameState{

		GameAI game_ai;
		
		public Mapping(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			Position p = NextPosition();
			String S;
			if (bLights == true){
				System.out.println("peguei");
				for (int i =0; i<treasure_respawns.size(); i++){
					if (treasurePositions.get(i).x == player.x && treasurePositions.get(i).y == player.y){
						treasure_respawns.get(i).timerStart();
						break;
					}
				}
				return "pegar_anel";
			}
			if (Lpath.size() != 0){
				System.out.println("to no path");

				List<Position> Ladj = GetObservableAdjacentPositions();
				for (int i = 0; i < Ladj.size(); i++){
					if (!isOutofBounds (Ladj.get(i).x, Ladj.get(i).y)){
						if (mapPositions.getOrDefault(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped) == Tile.Unmapped){					
							Lpath.clear();
							Lpath.add(Ladj.get(i).y*59 + Ladj.get(i).x);
							break;
						}
					}
				}

				Position temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
				if (temp.x == player.x && temp.y == player.y){
					Lpath.remove(0);
					temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
				}
				if (temp.x != player.x && temp.y != player.y){
					Lpath.clear();
					Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(temp.y*59 + temp.x), 59*34);
					Lpath.remove(0);
				}
				String ret = DoBestMove(player, temp);
				if (ret.equals("andar") || ret.equals("andar_re"))
					Lpath.remove(0);
				System.out.println(temp.x + "," + temp.y);
				System.out.println(player.x + "," + player.y);
				System.out.println(ret);
				return ret;
				
			}
			if (isOutofBounds(p.x, p.y))
				S = Tile.Wall.toString();			
			else
				S = mapPositions.getOrDefault(p.y*59 + p.x, Tile.Unmapped).toString();
			switch (S){
				case "Unmapped":
					System.out.println("Andei");
					return "andar";
				case "Clear":
					System.out.println(Tmap.get(player.y*59 + player.x).getNeighborList().size());
					if (Tmap.get(player.y*59 + player.x).getNeighborList().size() == 0)
						SetNeighbors(Tmap.get(player.y*59 + player.x));
					
					while(true){
						if (Sunknown.size() == 0){
							bAllClear = true;
							break;
						}
						if (player.x == Sunknown.get(0).x && player.y == Sunknown.get(0).y){
							Sunknown.remove(0);
							continue;
						}
						if (mapPositions.getOrDefault(Sunknown.get(0), Tile.Unmapped) == Tile.Unmapped){
							Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(Sunknown.get(0).y*59 + Sunknown.get(0).x), 59*34);
							break;
						}
						Sunknown.remove(0);
					}
					System.out.println("Player: " + player.x + "," + player.y);

					if (bAllClear)
						return "";
					Lpath.remove(0);
					Sunknown.remove(0);					
					Position temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
					System.out.println(temp.x + "," + temp.y);
					System.out.println(player.x + "," + player.y);
					if (temp.x == player.x && temp.y == player.y){
						Lpath.remove(0);
						temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
					}
					if (temp.x != player.x && temp.y != player.y){
						Lpath.clear();
						Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(temp.y*59 + temp.x), 59*34);
						Lpath.remove(0);
					}
					String ret = DoBestMove(player, temp);
					System.out.println(ret);
					if (ret.equals("andar") || ret.equals("andar_re"))
						Lpath.remove(0);
					return ret;
				case "mayPit": case "mayTeleport": case "mayPitport": case "Wall":
					System.out.println("Virei");
					return "virar_direita"; //Change to better action
				default:
					System.out.println("default");
					return "andar";
			}
		}
	}
	class SearchingGold implements GameState{

		GameAI game_ai;
		
		public SearchingGold(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			Position p = NextPosition();
			String S;
			if (aim != null && bLights == true && (aim.x == player.x && aim.y == player.y)){
				System.out.println("peguei");
				for (int i =0; i<treasure_respawns.size(); i++){
					if (treasurePositions.get(i).x == player.x && treasurePositions.get(i).y == player.y){
						treasure_respawns.get(i).timerStart();
						break;
					}
				}
				aim = null;
				return "pegar_anel";
			}
			else if (bLights == true){
				System.out.println("peguei");
				for (int i =0; i<treasure_respawns.size(); i++){
					if (treasurePositions.get(i).x == player.x && treasurePositions.get(i).y == player.y){
						treasure_respawns.get(i).timerStart();
						break;
					}
				}
				return "pegar_anel";
			}
			if (Lpath.size() != 0){
				List<Position> Ladj = GetObservableAdjacentPositions();
				for (int i = 0; i < Ladj.size(); i++){
					if (isOutofBounds(p.x, p.y))
						S = Tile.Wall.toString();			
					else
						S = mapPositions.getOrDefault(Lpath.get(0), Tile.Unmapped).toString();
					System.out.println(S);
					Position temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
					if (temp.x == player.x && temp.y == player.y){
						Lpath.remove(0);
						temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
					}
					if (temp.x != player.x && temp.y != player.y){
						Lpath.clear();
						Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(temp.y*59 + temp.x), 59*34);
						Lpath.remove(0);
					}
					String ret = DoBestMove(player, temp);

					
					switch (S){
						case "Unmapped":							
							if (ret.equals("andar") || ret.equals("andar_re"))
								Lpath.remove(0);
							return ret;
						case "Clear":
							if (ret.equals("andar") || ret.equals("andar_re"))
								Lpath.remove(0);
							return ret;
						case "mayPit": case "mayTeleport": case "mayPitport": case "Wall":
							Lpath.clear();
							Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(aim.y*59 + aim.x), 59*34);
							if (ret.equals("andar") || ret.equals("andar_re"))
								Lpath.remove(0);
							return ret;
						default:
							System.out.println("default");
							return "andar";							
					}					
				}								
			}
			else{
				if (aim == null){
					int val = 0, idx = -1;
					System.out.println(treasurePositions.size());
					for (int i = 0; i < treasurePositions.size(); i++){
						if (treasure_respawns.get(i).isRespawning)
							continue;
						int temp = Math.abs(treasurePositions.get(i).x - player.x);
						temp += Math.abs(treasurePositions.get(i).y - player.y);
						if (temp > val){
							val = temp;
							idx = i;
						}
					}
					System.out.println(idx);
					if (idx != -1)
						aim = treasurePositions.get(idx);
					else{
						currentState = mapping;
						return "";
					}
				}
				System.out.println("My aim: " + aim.x + "," + aim.y);
				String ret;				
				Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(aim.y*59 + aim.x), 59*34);
				Lpath.remove(0);
				if (Lpath.size() == 0){
					currentState = roaming;
					return "";
				}
				Position temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
				if (temp.x == player.x && temp.y == player.y){
					Lpath.remove(0);
					temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
				}
				if (temp.x != player.x && temp.y != player.y){
					Lpath.clear();
					Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(temp.y*59 + temp.x), 59*34);
					Lpath.remove(0);
				}
				ret = DoBestMove(player, temp);
				if (ret.equals("andar") || ret.equals("andar_re"))
					Lpath.remove(0);
				return ret;				
			}
			return "";
		}
	}
	class SearchingPowerUp implements GameState{

		GameAI game_ai;
		
		public SearchingPowerUp(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			Position p = NextPosition();
			String S;
			if (aim != null && bLights == true && (aim.x == player.x && aim.y == player.y)){
				System.out.println("peguei");
				for (int i =0; i<powerUp_respawns.size(); i++){
					if (powerUpPositions.get(i).x == player.x && powerUpPositions.get(i).y == player.y){
						powerUp_respawns.get(i).timerStart();
						break;
					}
				}
				aim = null;
				return "pegar_anel";
			}
			else if (bLights == true){
				System.out.println("peguei");
				for (int i =0; i<treasure_respawns.size(); i++){
					if (powerUpPositions.get(i).x == player.x && powerUpPositions.get(i).y == player.y){
						powerUp_respawns.get(i).timerStart();
						break;
					}
				}
				return "pegar_anel";
			}
			if (Lpath.size() != 0){
				List<Position> Ladj = GetObservableAdjacentPositions();
				for (int i = 0; i < Ladj.size(); i++){
					if (isOutofBounds(p.x, p.y))
						S = Tile.Wall.toString();			
					else
						S = mapPositions.getOrDefault(Lpath.get(0), Tile.Unmapped).toString();
					System.out.println(S);
					Position temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
					if (temp.x == player.x && temp.y == player.y){
						Lpath.remove(0);
						temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
					}
					if (temp.x != player.x && temp.y != player.y){
						Lpath.clear();
						Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(temp.y*59 + temp.x), 59*34);
						Lpath.remove(0);
					}
					String ret = DoBestMove(player, temp);
					switch (S){
						case "Unmapped":							
							if (ret.equals("andar") || ret.equals("andar_re"))
								Lpath.remove(0);
							return ret;
						case "Clear":
							if (ret.equals("andar") || ret.equals("andar_re"))
								Lpath.remove(0);
							return ret;
						case "mayPit": case "mayTeleport": case "mayPitport": case "Wall":
							Lpath.clear();
							Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(aim.y*59 + aim.x), 59*34);
							if (ret.equals("andar") || ret.equals("andar_re"))
								Lpath.remove(0);
							return ret;
						default:
							System.out.println("default");
							return "andar";							
					}					
				}								
			}
			else{
				if (aim == null){
					Lpath.clear();
					int val = 0, idx = -1;
					System.out.println(treasurePositions.size());
					for (int i = 0; i < treasurePositions.size(); i++){
						if (treasure_respawns.get(i).isRespawning)
							continue;
						int temp = Math.abs(treasurePositions.get(i).x - player.x);
						temp += Math.abs(treasurePositions.get(i).y - player.y);
						if (temp > val){
							val = temp;
							idx = i;
						}
					}
					System.out.println(idx);
					if (idx != -1)
						aim = treasurePositions.get(idx);
					else{
						currentState = roaming;
						return "";
					}
				}
				System.out.println("My aim: " + aim.x + "," + aim.y);
				String ret;				
				Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(aim.y*59 + aim.x), 59*34);
				Lpath.remove(0);
				Position temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
				if (temp.x == player.x && temp.y == player.y){
					Lpath.remove(0);
					temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
				}
				if (temp.x != player.x && temp.y != player.y){
					Lpath.clear();
					Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(temp.y*59 + temp.x), 59*34);
					Lpath.remove(0);
				}
				ret = DoBestMove(player, temp);
				if (ret.equals("andar") || ret.equals("andar_re"))
					Lpath.remove(0);
				return ret;				
			}
			return "";
		}
	}
	class Fleeing implements GameState{

		GameAI game_ai;
		
		public Fleeing(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			return null;
		}
	}
	class Pursuing implements GameState{
		
		GameAI game_ai;
		
		public Pursuing(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			return "atirar";
		}
	}
	class Striking implements GameState{

		GameAI game_ai;
		
		public Striking(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			if(isHit)
				return "atirar";
			changeState(getMappingState());
			return "";
		}
	}
	class Pondering implements GameState{

		GameAI game_ai;
		
		public Pondering(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			return "andar";
		}
	}
	class Roaming implements GameState{

		GameAI game_ai;
		int contador = 0;
		int actionC = 0;
		boolean acertei = false;
		int wait = 0;

		java.util.Random bin = new java.util.Random();
		public Roaming(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			
			String S;
			Position p = NextPosition();
			int  n = bin.nextInt(10);
			if(isBlocked)
				if(n%2 == 0)
					return "virar_esquerda";
				else
					return "virar_direita";
						
			if(isHit){
				acertei = true;
				return "atacar";
			}
			if(isEnemy){
				System.out.println("LEEROY");
				for(int i = 0; i < enemyGap; i++)
					if(mapPositions.getOrDefault(p.y * 59 + p.x + i, Tile.Unmapped).toString().equals("Wall"))
						return "andar";
				if(wait >= 10){
					wait = 0;
					return "virar_direita";
				}
				wait++;
				return "atacar";
			}
			if(isBluey){
				
				for(int i = 0; i < treasurePositions.size(); i++){
					if(p.x == treasurePositions.get(i).x && p.y == treasurePositions.get(i).y)
						treasure_respawns.get(i).timerStart();
				}
				return "pegar_ouro";
			}
			if(isReddy){
				
				for(int i = 0; i < powerUpPositions.size(); i++){
					if(p.x == powerUpPositions.get(i).x && p.y == powerUpPositions.get(i).y)
						powerUp_respawns.get(i).timerStart();
				}
				return "pegar_powerup";
			}
			if(isBreezy || isFlashy){
				inDanger = true;
				return "andar_re";
			}
			if (isOutofBounds(p.x, p.y)){
				contador = 0;
				return "virar_direita";
			}
			if(contador >= n+4){
				contador = 0;
				if(n%2 == 0){
					return "virar_direita";
				}
				else{
					return "virar_esquerda";
				}
			}
			if(inDanger){
				contador = 0;
				actionC++;
				if(actionC == 2){
					inDanger = false;
					actionC = 0;
					return "andar";
				}
				return "virar_direita";
			}
				S = mapPositions.getOrDefault(p.y * 59 + p.x, Tile.Unmapped).toString();
			switch (S) {
			case "Unmapped":
				contador++;
				return "andar";
			case "Clear":
				contador++;
				return "andar";
			case "mayPit":
			case "mayTeleport":
			case "mayPitport":
				System.out.println("Q periiigo");
				inDanger = true;
				return "andar_re";
			case "Wall":
				contador = 0;
				if(n%2 == 0)
					return "virar_direita";
				else 
					return "vira_esquerda";
			default:

			}
			return "";
		}
	}
}
