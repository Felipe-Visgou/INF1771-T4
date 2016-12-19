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
    private String[] fleeingActions = {"virar_direita","andar","andar"}; 
    private int fleeingIterator = 0;
    boolean isBreezy;
    boolean isBluey;
    boolean isReddy;
    boolean isFlashy;
    boolean isNoisy;
    boolean isBlocked;
    
    /* Game States */
    GameState mapping = new Mapping(this);
    GameState searching_gold = new SearchingGold(this);
    GameState searching_pu = new SearchingPowerUp(this);
    GameState fleeing = new Fleeing(this);
    GameState pursuing = new Pursuing(this);
    GameState striking = new Striking(this);
    GameState pondering = new Pondering(this);
    GameState currentState = mapping;
    
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
    	isBreezy = false;
        isBluey = false;
        isReddy = false;
        isFlashy = false;
        isNoisy = false;
    //    System.out.println("----SETEI AS FLAGS FALSAS------");
        for (String s : o)
        {      	
            if(s.equals("blocked")){
            	
            	isBlocked = true;
            	
            	Position b= NextPosition();
            	
            	if(isOutofBounds(b.x,b.y))
            		return;
            	
            	boolean flag = blockPositions.getOrDefault(b.y*59 + b.x,false);
            	
        		if(!flag)
        			blockPositions.put(b.y*59 + b.x, true);
        		
            	System.out.println("blockPos size : "+ blockPositions.size());
            	System.out.println("Estou blocked");
            	
            } else if(s.equals("steps")){
            	
            	isNoisy = true;
        
            } else if(s.equals("breeze")){
            	
            	isBreezy = true;
            	/* Treta com os possiveis pits */
            	pitPositions.add(new Position());
            	
            } else if(s.equals("flash")){
            	
            	isFlashy = true;
            	/* Treta com os possiveis teleports */
            	teleportPositions.add(new Position());
            	
            } else if(s.equals("blueLight")){
            	
            	isBluey = true;
            	
            	Position p = new Position(player.x, player.y);
            	
            	for(Position e : powerUpPositions){
            		if(e.x == player.x && e.y == player.y)
            			break;
            		else{
                		powerUpPositions.add(new Position(player.x,player.y));
                    	powerUp_respawns.add(new Respawn(player.x,player.y));
            		}	
            	}           	

            } else if(s.equals("redLight")){
            	
            	isReddy = true;
            	
            	Position p = new Position(player.x, player.y);
            	
            	for(Position e : treasurePositions){
            		if(e.x == player.x && e.y == player.y)
            			break;
            		else{
            			treasurePositions.add(new Position(player.x,player.y));
            			treasure_respawns.add(new Respawn(player.x,player.y));
            		}	
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
    	List<String> s = new ArrayList<String>();
    	GetObservations(s);
    }

    /**
     * Get Decision
     * @return command string to new decision
     */
    public String GetDecision()
    {
    	System.out.println("Current State is "+currentState.toString());
    	/*System.out.println("My sensors : ");
    	System.out.printf("isBreezy - ");
    	f(isBreezy)
    		System.out.printf("Yes;");
    	else
    		System.out.printf("No;");
    	System.out.printf("isFlashy - ");
    	if(isFlashy)
    		System.out.printf("Yes;");
    	else
    		System.out.printf("No;");
    	System.out.printf("isBluey - ");
    	if(isBluey)
    		System.out.printf("Yes;");
    	else
    		System.out.printf("No;");
    	System.out.printf("isReddy - ");
    	if(isReddy)
    		System.out.printf("Yes;");
    	else
    		System.out.println("No;");
    	System.out.printf("isBlocked - ");
    	if(isBlocked)
    		System.out.printf("Yes;");
    	else
    		System.out.printf("No;");
    	System.out.printf("isNoisy - ");
    	if(isNoisy)
    		System.out.printf("Yes;\n");
    	else
    		System.out.printf("No;\n");*/
    	String action = currentState.doAction();
    	System.out.println("Now I'm in state "+ currentState.toString());
    	System.out.printf("\n");
    	System.out.println("I'm gonna do : "+ action);
    	System.out.printf("\n");

    	return action;
    	
    	
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
			
			/* Descomente quando searching gold estiver implementado */
			//changeState(getSearchingGoldState());
			
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
			Position p = new Position(player.x, player.y);
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
				//fleeingIterator = 0;
				//changeState(getFleeingState());
				//return "andar_re";
			}
			if(isBreezy || isFlashy){
				//fleeingIterator = 0;
				//changeState(getFleeingState());
				//return "andar_re";
			}
			if(isOutofBounds(pn.x, pn.y))
				return "virar_direita";
			if(blockPositions.getOrDefault(pn.y*59 + pn.x, false)){
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
			
		/*	Position pn = NextPosition();
			if(fleeingIterator == 2){
				fleeingIterator = 0;
				changeState(getPonderingState());
				return "andar";
			}
			if(isOutofBounds(pn.x, pn.y))
				return "virar_direita";
			if(blockPositions.getOrDefault(pn.y*59 + pn.x, false)){
				return "virar_direita";
			}
			String action = fleeingActions[fleeingIterator];
			
			fleeingIterator++;*/
			return "andar";
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
}
