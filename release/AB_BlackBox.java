import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.lang.Float;

public class AB_BlackBox {
  private boolean useMoveClassifier = false;
	private static long time = 0;
	private static int hashHits = 0;
	private static int statesVisited = 0;
	private static int totalStatesVisited = 0;
	private static int numtimes = 0;

	int thisPlayer;
	int otherPlayer;

	HashMap<HashableBoard, Float> maxPlayerHash = new HashMap<HashableBoard, Float>(100000);
	HashMap<HashableBoard, Float> minPlayerHash = new HashMap<HashableBoard, Float>(100000);

	public float horzDistWeight = 1;
	public float vertDistWeight = 1;
	public float stragglerWeight = 1;
	public float chainWeight = 1;
	
	public void setWeights (float horz, float vert, float straggler, float chain){
		horzDistWeight=horz;
		vertDistWeight=vert;
		stragglerWeight = straggler;
		chainWeight=chain;
	}
	
	public static enum Message{
		MOVE_FOUND,NEED_TO_RECOMPUTE;
		//if move found, store it as 
		private Move move=null;
		public void setMove(Move m){
			if(this==MOVE_FOUND){
				move=m;
			}else{
				System.err.println("Wtf, why are you assigning a move if ab search needs to recompute?");
			}
		}
		public Move getMove(){
			if(this==MOVE_FOUND){
				return move;
			}else{
				System.err.println("Shit dawg, why are you querying for a move? I told you there's none.");
				return null;
			}
		}
	}

	public AB_BlackBox(int whichPlayer, float horz, float vert, float straggler, float chain){
		thisPlayer=whichPlayer;
		otherPlayer=3-thisPlayer;
		
		horzDistWeight=horz;
		vertDistWeight=vert;
		stragglerWeight = straggler;
		chainWeight=chain;
	}

	//main interaction interface
	//public AB_BlackBox.Message gimmeAMove(GameHistory gh){
	public AB_BlackBox.Message gimmeAMove(Board b,int depth){
		maxPlayerHash.clear();
		minPlayerHash.clear();
		hashHits = 0;
		statesVisited = 0;
    useMoveClassifier = Const.LEARN_LIKE_A_BOSS ? true : false;

		Move best = recompute(b,depth);
    if (best == null) {
      // try again without the move classifier
      System.err.println("Move classifier off...");
      useMoveClassifier = false;
      best = recompute(b, depth);
    }

		// begin data printing:
		System.err.println("ABStates visited: " + statesVisited);
		System.err.println("Hash hits: " + hashHits);
		totalStatesVisited += statesVisited;
		numtimes += 1;
		float average = ((float) totalStatesVisited) / ((float) numtimes);
		System.err.println("Total states visited: " + totalStatesVisited);
		System.err.println("Average states visited: " + average);
		System.err.println("Time taken sorting: " + (time * Math.pow(10, -9)));
		System.err.println("Hashed states: " + maxPlayerHash.size() + minPlayerHash.size());

		if(best==null){
			return Message.NEED_TO_RECOMPUTE;
		}else{
			Message message=Message.MOVE_FOUND;
			message.setMove(best);
			return message;
		}
	}

	//run alpha beta pruning on minmax tree of specified depth
	public Move recompute(final Board b,int depth){
		if(depth<=0){
			System.err.println("Shitshitshit! Nonpositive depth for alpha/beta search.");
			return null;
		}
		if(b.getTurn()!=thisPlayer){
			System.err.println("Wrong turn on the board for alpha/beta search.");
			return null;
		}

		//custom version of abMax below
		int depthLeft=depth;
		float alpha=Float.NEGATIVE_INFINITY;
		float beta=Float.POSITIVE_INFINITY;
		
		if(depthLeft==0 || b.checkWin(thisPlayer) || b.checkWin(otherPlayer)){
			//we were given a terminal state!!
			System.err.println("Terminal node given to alpha/beta search.");
			return null;
		}
		float nodeValue=Float.NEGATIVE_INFINITY;
		Move bestMove=null;
		for(Move move : getMoveSet(b)) {
			///go deeper
			b.move(move);
			float childValue=abMin(b,depthLeft-1,alpha,beta);
			b.backwardMove(move);
			///analyze results
			if(bestMove==null || childValue>nodeValue){
				bestMove=move;
			}
			nodeValue=Math.max(nodeValue, childValue);
			//update alpha
			alpha=Math.max(nodeValue, alpha);
		}
		//if still no moves
		if(bestMove==null){
			System.err.println("No moves state given to alpha/beta search");
		}
		return bestMove;
	}

	private float abMax(final Board b, int depthLeft, float alpha, float beta){
		if (maxPlayerHash.containsKey(new HashableBoard(b)) && Const.AB_USE_HASHING) {
			hashHits++;
//			System.err.println("Boom, hashed.");
			return maxPlayerHash.get(new HashableBoard(b));
		}

		if(depthLeft==0 || b.checkWin(thisPlayer) || b.checkWin(otherPlayer)){
			float terminalValue=utilityOfState(b,thisPlayer);
			if (Const.AB_USE_HASHING) {
				maxPlayerHash.put(new HashableBoard(b), terminalValue);
			}
			return terminalValue;
		}

		List<Move> moveSet = getMoveSet(b);
    if (useMoveClassifier) {
      moveSet = Util.removeLosingMoves(moveSet);
    }

		if (Const.AB_USE_MOVE_ORDERING) {
			long startTime = System.nanoTime();
			Collections.sort(moveSet, new Comparator<Move>(){
        float baseUtility = utilityOfState(b, thisPlayer);
				@Override
				public int compare(Move m1, Move m2) {
          float utility1 = baseUtility;
          float utility2 = baseUtility;
          utility1 -= getSinglePieceUtility(m1.r1, m1.c1, b, thisPlayer);
          b.move(m1);
          utility1 += getSinglePieceUtility(m1.r2, m1.c2, b, thisPlayer);
          b.backwardMove(m1);
          utility2 -= getSinglePieceUtility(m2.r1, m2.c1, b, thisPlayer);
          b.move(m2);
          utility2 += getSinglePieceUtility(m2.r2, m2.c2, b, thisPlayer);
          b.backwardMove(m2);

					float diff = utility1 - utility2;
					if (diff > 0) return -1;
					else if (diff < 0) return 1;
					else return 0;
				}
			});
			long endTime = System.nanoTime();
		 	time += (endTime - startTime);
		}

		float nodeValue = Float.NEGATIVE_INFINITY;
		for(Move move : moveSet) {
			statesVisited++;
			//go deeper
			b.move(move);
			float childValue=abMin(b,depthLeft-1,alpha,beta);
			b.backwardMove(move);
			//analyze results
			nodeValue=Math.max(nodeValue, childValue);
			if(nodeValue>=beta) {
				if (Const.AB_USE_HASHING) {
					maxPlayerHash.put(new HashableBoard(b), nodeValue);
				}
				return nodeValue;
			}
			alpha=Math.max(nodeValue, alpha);
		}

		if (Const.AB_USE_HASHING) {
			maxPlayerHash.put(new HashableBoard(b), nodeValue);
		}
		return nodeValue;
	}

	private float abMin(final Board b, int depthLeft, float alpha, float beta){
		if (minPlayerHash.containsKey(new HashableBoard(b)) && Const.AB_USE_HASHING) {
			hashHits++;
//			System.err.println("Boom, hashed.");
			return minPlayerHash.get(new HashableBoard(b));
		}

		if(depthLeft==0 || b.checkWin(thisPlayer) || b.checkWin(otherPlayer)){
			float terminalValue=utilityOfState(b,thisPlayer);
			if (Const.AB_USE_HASHING) {
				minPlayerHash.put(new HashableBoard(b), terminalValue);
			}
			return terminalValue;
		}

		List<Move> moveSet = getMoveSet(b);
    if (useMoveClassifier) {
      moveSet = Util.removeLosingMoves(moveSet);
    }

		if (Const.AB_USE_MOVE_ORDERING) {
			long startTime = System.nanoTime();
			Collections.sort(moveSet, new Comparator<Move>(){
        float baseUtility = utilityOfState(b, thisPlayer);
				@Override
				public int compare(Move m1, Move m2) {
          float utility1 = baseUtility;
          float utility2 = baseUtility;
          utility1 -= getSinglePieceUtility(m1.r1, m1.c1, b, thisPlayer);
          b.move(m1);
          utility1 += getSinglePieceUtility(m1.r2, m1.c2, b, thisPlayer);
          b.backwardMove(m1);
          utility2 -= getSinglePieceUtility(m2.r1, m2.c1, b, thisPlayer);
          b.move(m2);
          utility2 += getSinglePieceUtility(m2.r2, m2.c2, b, thisPlayer);
          b.backwardMove(m2);

					float diff = utility1 - utility2;
					if (diff > 0) return 1;
					else if (diff < 0) return -1;
					else return 0;
				}
			});
			long endTime = System.nanoTime();
			time += (endTime - startTime);
		}

		float nodeValue = Float.POSITIVE_INFINITY;
		for(Move move : moveSet) {
			statesVisited++;
			//go deeper
			b.move(move);
			float childValue=abMax(b,depthLeft-1,alpha,beta);
			b.backwardMove(move);
			//analyze results
			nodeValue=Math.min(nodeValue, childValue);
			if(nodeValue<=alpha) {
				if (Const.AB_USE_HASHING) {
					minPlayerHash.put(new HashableBoard(b), nodeValue);
				}
				return nodeValue;
			}
			beta=Math.min(nodeValue, beta);
		}

		if (Const.AB_USE_HASHING) {
			minPlayerHash.put(new HashableBoard(b), nodeValue);
		}
		return nodeValue;
	}

	// We go through all our marbles and get all possible locations we can move to.
	private static List<Move> getMoveSet(Board board) {
		int turn=board.getTurn();
		List<Move> moveSet = new LinkedList<Move>();
		for (int i = 0; i < 17; i++) {
			for (int j = 0; j < 25; j++) {
				if (board.at(i, j) == turn) {
					HashSet<Integer> destinations = new HashSet<Integer>();
					board.legalMoves(i, j, destinations);
					for (Integer dest : destinations) {
						int r = dest / 25;
						int c = dest % 25;
						Move m = new Move(0, 0, 0, i, j, r, c, -1, -1);
						moveSet.add(m);
					}
				}
			}
		}
		return moveSet;
	}

	//higher utility is better
	private float utilityOfState(Board board, int turn) {
		float utility = 0;
		for (int i = 0; i < 17; i++) {
			for (int j = 0; j < 25; j++) {
        //update utility
        float utilToAdd = getSinglePieceUtility(i, j, board, turn);
        utility+=utilToAdd;
      }
    }
	
		float chainUtil = 0; float stragglerUtil = 0; float horzDistUtil=0; float vertDistUtil=0; 
		//TODO implement / segment up Utility into separate Utils for chains, stragglers, horizontal dist, and vert dist.
		return vertDistWeight*utility + horzDistUtil + stragglerUtil + chainUtil;
		//return vertDistWeight*utility + horzDistWeight*utility + chainWeight*chainUtil + stragglerWeight*stragglerUtil;
	}

  private float getSinglePieceUtility(int i, int j, Board board, int turn) {
    int middleR, middleC;
    int oppMiddleR, oppMiddleC;

    if (turn == 1) {
      middleR = 2;
      middleC = 12;
      oppMiddleR = 14;
      oppMiddleC = 12;
    } else {
      middleR = 14;
      middleC = 12;
      oppMiddleR = 2;
      oppMiddleC = 12;
    }

    int at=board.at(i, j);
    float utilToAdd = 0;
    //count only pieces belonging to the players
    if(at==1 || at==2){
      boolean isInTargetTriangle = Util.isInTargetTriangle(i, at);
      int dx,dy;
      if(at==turn){
        dy=(middleR-i);
        dx=(middleC-j);
      }else{
        dy=(oppMiddleR-i);
        dx=(oppMiddleC-j);
      }
      //change utility function based on where we are
      //if we're in the goal space, try to go for the center
      //where score is 0 at the center of triangle
      //1 on the hexagon around
      //and 2 at the corners
      //are we in the target triangle?
      if (isInTargetTriangle && at==turn) {
        //					if(Math.abs(dy)<=1){
        //yes
        //weigh distance laterally and sideways as well
        //use hexagonal grid distance
        //abs(dx)==2 means lateral pieces

        if (Const.AB_TRY_NEW_WEIGHTS) {
          if(dx==0 && dy==0){
            utilToAdd=0;
          }else if(Math.abs(dx)<=3 && dy!=0){
            if (dy == 1) {
              utilToAdd=6;
            } else {
              utilToAdd=4;
            }
          }else{
            utilToAdd=1;
          }
        } else {
          if(dx==0 && dy==0){
            utilToAdd=0;
          }else if(Math.abs(dx)<=2 && dy!=0){
            utilToAdd=-1;
          }else{
            utilToAdd=-3;
          }

          if(at!=turn){
            utilToAdd=-utilToAdd;
          }
        }
      }else{
        //we're not in the winning corner
        //prioritize the score to make it sort first by y distance
        utilToAdd=(-(Math.abs(dy)*13+Math.abs(dx)));

        if (Const.AB_TRY_NEW_WEIGHTS) {
        // weight multipliers in order to get our pieces out of our
        // home area and into our opponent's half
        // there must be a smarter way of doing this.
          if (at == turn) {
            if (!Util.isInOpponentsHalf(i, at))
              utilToAdd *= 2;
            else if (Util.isInHomeTriangle(i, at))
              utilToAdd *= 3;
          }
        }

        if(at!=turn){
          utilToAdd=-utilToAdd;
        }
      }
    }

    return utilToAdd;
  }

	private static class HashableBoard {

		byte[] boardByteArray = new byte[(Const.NUM_VALID_CELLS+3)/4];

		public HashableBoard(Board b) {
			//optimization:
			//each byte can actually hold the representation of 4 cells
			int index = 0;
			int indexInByte = 0;
			for (int i = 0; i < Const.BOARD_HEIGHT; i++) {
				for (int j = 0; j < Const.BOARD_WIDTH; j++) {
					int at=b.at(i, j);
					if (at != -1) {
						//then the integer is 0,1,2, or 3
						boardByteArray[index] += (byte)(at<<indexInByte);
						//update positions to add representation
						indexInByte+=2;
						if(indexInByte==8){
							index++;
							indexInByte=0;
						}
					}
				}
			}
		}

		public byte[] getBoardByteArray() { return boardByteArray; }

		@Override
			public int hashCode() {
				return Arrays.hashCode(boardByteArray);
			}

		@Override
			public boolean equals(Object o) {
				HashableBoard otherBoard = (HashableBoard) o;
				return Arrays.equals(boardByteArray, otherBoard.getBoardByteArray());
			}
	}


	/*
	class HashableBoard {
		short[] pieces = new short[20 + AwesomeAI.specialMarblesToAdd];

		public HashableBoard(Board b) {
			int index = 0;

			for (int i = 0; i < Const.BOARD_HEIGHT; i++) {
				for (int j = 0; j < Const.BOARD_WIDTH; j++) {
					if (b.at(i, j) > 0) {
						if (!AwesomeAI.defaultSpecialMarbles.contains(new Cell(i, j))) {
							StringBuilder sb = new StringBuilder("");
							sb.append(b.at(i, j));
							sb.append(i);
							sb.append(j);
							pieces[index++] = Short.parseShort(sb.toString());
						}
					}
				}
			}
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(pieces);
		}

		@Override
		public boolean equals(Object o) {
			HashableBoard otherBoard = (HashableBoard) o;
			return Arrays.equals(pieces, otherBoard.pieces);
		}

	}
	*/
}
