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
	boolean bLights = false;
	boolean bPicked = false;
    Position player = new Position();
    String state = "ready";
    String dir = "north";
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
    GameState pondering = new Pondering(this);
    GameState roaming = new Roaming(this);
    GameState currentState = roaming;
    
    boolean isBreezy;
    boolean isBluey;
    boolean isReddy;
    boolean isFlashy;
    boolean isNoisy;
    boolean isBlocked;
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
    
    public enum Tile{
    	Unmapped, Clear, mayPit, Wall, mayTeleport, mayPitport
    }
    
    public void SetNeighbors(Node N){
    	if (N.getIndex() - 59 > 0)
			N.setNeighbor(N.getIndex() - 59);
		if (N.getIndex() + 59 < 59*34)
			N.setNeighbor(N.getIndex() + 59);
		if (N.getIndex() - 1 > 0 && (N.getIndex()-1)%59 != 58)
			N.setNeighbor(N.getIndex() - 1);
		if (N.getIndex() + 1 < 59*34 && (N.getIndex()+1)%59 != 0)
			N.setNeighbor(N.getIndex() + 1);
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
/*    public void GetObservations(List<String> o)
    {	
    	
    	Position p = GetPlayerPosition();
    	Position n = NextPosition();
    	Boolean bPresence = false;
    	List<Position> Ladj = GetObservableAdjacentPositions();
    	bLights = false;
    	isBlocked = false;
    	isBreezy = false;
        isBluey = false;
        isReddy = false;
        isFlashy = false;
        isNoisy = false;
        isDamage = false;
        isEnemy = false;
        isHit = false;
        enemyGap = -1;
        for (String s : o)
        {      	
            if(s.equals("blocked")){            	
            	isBlocked = true;
            	if(isOutofBounds(p.x,p.y))
            		return;
            	
            	Enum flag = mapPositions.getOrDefault(n.y*59 + n.x, Tile.Unmapped);
        		if(flag != Tile.Wall){
        			mapPositions.put(n.y*59 + n.x, Tile.Wall);
        			Tmap.put(n.y*59 + n.x, new Node(1000000));
        			Tmap.get(n.y*59 + n.x).setIndex(n.y*59 + n.x);
        			SetNeighbors(Tmap.get(n.y*59 + n.x));
        		}
            	
            	System.out.println("Estou blocked");
            	
            } else if(s.equals("steps")){
            	isNoisy = true;
        
            } else if(s.equals("breeze")){
            	isBreezy = true;
            	bPresence = true;
            	System.out.println("MMM BREEZE");
            	
            	for (int i = 0; i < Ladj.size(); i++){
            		if (!isOutofBounds(Ladj.get(i).y, Ladj.get(i).x)){
            			if (mapPositions.getOrDefault(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped) == Tile.Unmapped){
            				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.mayPit);
            				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1000000));
            				Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
                			SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
            			}
            			else if (mapPositions.getOrDefault(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped) == Tile.mayTeleport){
            				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.mayPitport);
            				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1000000));
            				Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
                			SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
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
            			}
            			else if (mapPositions.getOrDefault(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped) == Tile.mayPit){
            				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.mayPitport);
            				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1000000));
            				Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
                			SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
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
                	treasure_respawns.add(new Respawn(player.x,player.y));
                	
				}
			} else if (s.equals("greenLight")) {

			} else if (s.equals("weakLight")) {

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
        				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Clear);
        				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1));
        	        	Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
        				SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
        			}
        			else if (mapPositions.get(Ladj.get(i).y*59 + Ladj.get(i).x) == Tile.mayTeleport){
        				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Clear);
        				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1));
        	        	Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
        				SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
        			}
        			else if (mapPositions.get(Ladj.get(i).y*59 + Ladj.get(i).x) == Tile.mayPitport){
        				mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Clear);
        				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1));
        	        	Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
        				SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
        			}
        			else{
        				Sunknown.add(0, Ladj.get(i));
        				Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1));
        	        	Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
        				SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
        			}
        		}
        	}
        }

    }*/
 public void GetObservations(List<String> o)
    { 
     Position p = GetPlayerPosition();
     Position n = NextPosition();
     Boolean bPresence = false;
     List<Position> Ladj = GetObservableAdjacentPositions();
     bLights = false;
 	isBlocked = false;
 	isBreezy = false;
     isBluey = false;
     isReddy = false;
     isFlashy = false;
     isNoisy = false;
     isDamage = false;
     isEnemy = false;
     isHit = false;
     enemyGap = -1;
        for (String s : o)
        {       
            if(s.equals("blocked")){
            	isBlocked = true;
             
             if(isOutofBounds(p.x,p.y))
              return;
             
             Enum flag = mapPositions.getOrDefault(n.y*59 + n.x, Tile.Unmapped);
          if(flag != Tile.Wall){
           System.out.println("Funciona");
           mapPositions.put(n.y*59 + n.x, Tile.Wall);
           Tmap.put(n.y*59 + n.x, new Node(1000000));
           Tmap.get(n.y*59 + n.x).setIndex(n.y*59 + n.x);
           SetNeighbors(Tmap.get(n.y*59 + n.x));
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
               }
               else if (mapPositions.getOrDefault(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped) == Tile.mayTeleport){
                mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.mayPitport);
                Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1000000));
                Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
                   SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
                   mapPositions.put(p.y*59 + p.x, Tile.Clear); //I'm clear
                         Tmap.put(p.y*59 + p.x, new Node(1));
                         Tmap.get(p.y*59 + p.x).setIndex(p.y*59 + p.x);
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
               }
               else if (mapPositions.getOrDefault(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.Unmapped) == Tile.mayPit){
                mapPositions.put(Ladj.get(i).y*59 + Ladj.get(i).x, Tile.mayPitport);
                Tmap.put(Ladj.get(i).y*59 + Ladj.get(i).x, new Node(1000000));
                Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x).setIndex(Ladj.get(i).y*59 + Ladj.get(i).x);
                   SetNeighbors(Tmap.get(Ladj.get(i).y*59 + Ladj.get(i).x));
                   mapPositions.put(p.y*59 + p.x, Tile.Clear); //I'm clear
                         Tmap.put(p.y*59 + p.x, new Node(1));
                         Tmap.get(p.y*59 + p.x).setIndex(p.y*59 + p.x);
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
           else if (mapPositions.get(Ladj.get(i).y*59 + Ladj.get(i).x) == Tile.Wall){
               System.out.println("Funciona");
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
    	
    	return currentState.doAction();
    	
    	
       /* java.util.Random rand = new java.util.Random();
    
        	//pensar no que fazer
	    	int  n = rand.nextInt(8);
	    	//System.out.println("decision now is : "+ n);
	    	switch(n){
	     	case 0:
	            return "virar_direita";
	    	case 1:
	            return "virar_esquerda";
	    	case 2:*/
	            //return "andar";
	    	/*case 3:
	            return "atacar";
	    	case 4:
	    		//aciona o timer desse ouro
	            return "pegar_ouro";
	    	case 5:
	            return "pegar_anel";
	    	case 6:
	           return "pegar_powerup";
	    	case 7:
	          return "andar_re";
	    }
	    	return "";*/
    }
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
    GameState getPonderingState(){
    	return this.pondering;
    }
    GameState getRoamingState(){
    	return this.roaming;
    }
    GameState getCurrentState(){
    	return this.currentState;
    }
    void changeState(GameState new_current_state){
    	this.currentState = new_current_state;
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
			
			//System.out.println("do Action do Mapping");
			Position p = NextPosition();
			String S;
			if (bLights == true){
				System.out.println("peguei");
				return "pegar_anel";
			}
			if(isEnemy){
				changeState(getStrikingState());
				return "atacar";
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
				String ret = DoBestMove(player, temp);
				if (ret == "andar" || ret == "andar_re")
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
			System.out.println(S);
			switch (S){
				case "Unmapped":
					System.out.println("Andei");
					return "andar";
				case "Clear":
					Lpath = Algorithm.AStar(Tmap, Tmap.get(player.y*59 + player.x), Tmap.get(Sunknown.get(0).y*59 + Sunknown.get(0).x), 59*34);
					Lpath.remove(0);
					Position temp = new Position(Lpath.get(0)%59, Lpath.get(0)/59);
					System.out.println(temp.x + "," + temp.y);
					System.out.println(player.x + "," + player.y);					
					String ret = DoBestMove(player, temp);
					System.out.println(ret);
					if (ret == "andar" || ret == "andar_re")
						Lpath.remove(0);
					return ret;
				case "mayPit": case "mayTeleport": case "mayPitport": case "Wall":
					System.out.println("Virei");
					return "virar_direita"; //Change to better action
				default:
					System.out.println("default");
					return "andar";
			}
			
			
						
			/*if(isOutofBounds(p.x, p.y))
				return "virar_direita";
			if(mapPositions.getOrDefault(p.y*59 + p.x, Tile.Unmapped) == Tile.Wall){
				return "virar_direita";
			}*/
			//return null;
		}
	}
	class SearchingGold implements GameState{

		GameAI game_ai;
		
		public SearchingGold(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			return null;
		}
	}
	class SearchingPowerUp implements GameState{

		GameAI game_ai;
		
		public SearchingPowerUp(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			return null;
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
			
			/*String S;
			Position p = NextPosition();
			if (isOutofBounds(p.x, p.y))
				S = Tile.Wall.toString();			
			else
				S = mapPositions.getOrDefault(p.y*59 + p.x, Tile.Unmapped).toString();
			System.out.println(S);
			switch (S){
				case "Unmapped":

				case "Clear":

				case "mayPit": case "mayTeleport": case "mayPitport": case "Wall":

				default:

			}*/
			
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
			
			/*Position p = new Position(player.x, player.y);
			Position pn = NextPosition();

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
			if(isNoisy){
				fleeingIterator = 0;
				changeState(getFleeingState());
				return "andar_re";
			}
			if(isBreezy || isFlashy){
				fleeingIterator = 0;
				changeState(getFleeingState());
				return "andar_re";
			}
			
			if(isOutofBounds(pn.x, pn.y))
				return "virar_direita";
			if(blockPositions.getOrDefault(pn.y*59 + pn.x, false)){
				return "virar_direita";
			}
			changeState(getMappingState());*/
			return "andar";
		}
	}
	class Roaming implements GameState{

		GameAI game_ai;
		int contador = 0;
		int actionC = 0;
		boolean acertei = false;
		int wait = 0;
	       /* java.util.Random rand = new java.util.Random();
	    
    	//pensar no que fazer
    	int  n = rand.nextInt(8);*/
		java.util.Random bin = new java.util.Random();
		public Roaming(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			
			String S;
			Position p = NextPosition();
			int  n = bin.nextInt(10);
			if(isHit){
				acertei = true;
				return "atacar";
			}
			if(isEnemy){
				System.out.println("is enemy is true");
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
				if(actionC == 3){
					inDanger = false;
					actionC = 0;
					return "andar";
				}
				return "virar_direita";
			}
				S = mapPositions.getOrDefault(p.y * 59 + p.x, Tile.Unmapped).toString();
			System.out.println(S);
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
