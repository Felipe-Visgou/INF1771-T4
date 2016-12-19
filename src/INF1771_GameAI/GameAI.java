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
    Position player = new Position();
    String state = "ready";
    String dir = "north";
    long score = 0;
    int energy = 0;
    final int RESPAWN_DELAY = 30000; /* 30s is the respawn delay */
    private ArrayList<Position> treasurePositions = new ArrayList<Position>();
    private ArrayList<Position> pitPositions = new ArrayList<Position>();
    private ArrayList<Position> teleportPositions = new ArrayList<Position>();  
    private Hashtable<Integer, Boolean> blockPositions = new Hashtable<Integer, Boolean>();    
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
    GameState currentState = mapping;
    
    boolean isBlocked = false;
    /**
     * Refresh player status
     * @param x			player position x
     * @param y			player position y
     * @param dir		player direction
     * @param state		player state
     * @param score		player score
     * @param energy	player energy
     */
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
    	isBlocked = false;
        for (String s : o)
        {      	
            if(s.equals("blocked")){
            	
            	Position b= NextPosition();
            	
            	if(isOutofBounds(b.x,b.y))
            		return;
            	
            	boolean flag = blockPositions.getOrDefault(b.y*59 + b.x,false);
        		if(!flag)
        			blockPositions.put(b.y*59 + b.x, true);
        		
            	System.out.println("blockPos size : "+ blockPositions.size());
            	
            	System.out.println("Estou blocked");
            	
            } else if(s.equals("steps")){
            	
        
            } else if(s.equals("breeze")){
            	
            	/* Treta com os possiveis pits */
            	pitPositions.add(new Position());
            	
            } else if(s.equals("flash")){
            	
            	/* Treta com os possiveis teleports */
            	teleportPositions.add(new Position());
            	
            } else if(s.equals("blueLight")){
            	
            	Position p = new Position(player.x, player.y);
            	if(!powerUpPositions.contains(p)){
            		
            		powerUpPositions.add(new Position(player.x,player.y));
                	powerUp_respawns.add(new Respawn(player.x,player.y));
            	}            	


            } else if(s.equals("redLight")){
            	
            	Position p = new Position(player.x, player.y);
            	if(!treasurePositions.contains(p)){
            		
                	treasurePositions.add(new Position(player.x,player.y));
                	treasure_respawns.add(new Respawn(player.x,player.y));
                	
            	}	
            } else if(s.equals("greenLight")){
            	

            } else if(s.equals("weakLight")){


            }
        }

    }

    /**
     * No observations received
     */
    public void GetObservationsClean()
    {
        
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
			if(isOutofBounds(p.x, p.y))
				return "virar_direita";
			if(blockPositions.getOrDefault(p.y*59 + p.x, false)){
				return "virar_direita";
			}
			return "andar";
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
			
			return null;
		}
	}
	class Striking implements GameState{

		GameAI game_ai;
		
		public Striking(GameAI o){
			
			game_ai = o;
		}
		@Override
		public String doAction() {
			
			return null;
		}
	}
}
