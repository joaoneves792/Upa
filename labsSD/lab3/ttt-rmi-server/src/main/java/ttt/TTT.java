package ttt;

public class TTT extends java.rmi.server.UnicastRemoteObject implements TTTService {
	private static final long serialVersionUID = -1675320738247932782L;
	char board[][] = {
		  {'1','2','3'},          /* Initial values are reference numbers */
		  {'4','5','6'},          /* used to select a vacant square for   */
		  {'7','8','9'}           /* a turn.                              */
		};
	int nextPlayer = 0;
	int numPlays = 0;

	char playerZeroSymbol = 'O';
	char playerOneSymbol = 'X';

	public TTT() throws java.rmi.RemoteException{};
	
    public String currentBoard() throws java.rmi.RemoteException {
    	String s = "\n\n " + 
    				board[0][0]+" | " +
    				board[0][1]+" | " +
    				board[0][2]+" " +
    				"\n---+---+---\n " +
    				board[1][0]+" | " +
    				board[1][1]+" | " +
    				board[1][2]+" " +
    				"\n---+---+---\n " +
    				board[2][0]+" | " +
    				board[2][1]+" | " +
    				board[2][2] + " \n";
    	return s;
    }

    public boolean play(int row, int column, int player) throws java.rmi.RemoteException{
		if (!(row >=0 && row <3 && column >= 0 && column < 3))
			return false;
		if (board[row][column] > '9')
			return false;
		if (player != nextPlayer) 
			return false;

		if (numPlays == 9) 
			return false;

		board[row][column] = (player == 1) ? playerOneSymbol : playerZeroSymbol;        /* Insert player symbol   */
		nextPlayer = (nextPlayer + 1) % 2;
		numPlays ++;
		return true;	
    }

    public int checkWinner() throws java.rmi.RemoteException{
    	  int i;
    	  /* Check for a winning line - diagonals first */     
    	  if((board[0][0] == board[1][1] && board[0][0] == board[2][2]) ||
    	     (board[0][2] == board[1][1] && board[0][2] == board[2][0])) {
    		  if (board[1][1]==playerOneSymbol)
    			  return 1;
    		  else 
    			  return 0;
    	  }
    	  else
    	    /* Check rows and columns for a winning line */
    	    for(i = 0; i <= 2; i ++){
    	      if((board[i][0] == board[i][1] && board[i][0] == board[i][2])) {
    	    	  if (board[i][0]==playerOneSymbol)
    	    		  return 1;
    	    	  else 
    	    		  return 0;
    	      }

    	     if ((board[0][i] == board[1][i] && board[0][i] == board[2][i])) {
    	    	 if (board[0][i]==playerOneSymbol) 
    	    		 return 1;
    	    	 else 
    	    		 return 0;
    	     }
    	    }
    	  	if (numPlays == 9)
    	  		return 2; /* A draw! */
    	  	else
    	  		return -1; /* Game is not over yet */
	}

    public void trocaSimbolos(char simbolo) throws java.rmi.RemoteException {
	char oldSymbol;
	    
	if(nextPlayer == 0){
	    oldSymbol = playerZeroSymbol;
	    playerZeroSymbol = simbolo;
	}
	else{
	    oldSymbol = playerOneSymbol;
            playerOneSymbol = simbolo;	
	}

	for(int i=0; i<3; i++)
	     for(int j=0; j<3; j++)
		if(board[i][j] == oldSymbol)
		    board[i][j] = simbolo;
    }

}
