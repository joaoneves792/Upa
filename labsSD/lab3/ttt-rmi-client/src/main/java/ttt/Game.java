package ttt;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Game {
	final static String name = "TicTacToe";
	static TTTService ttt;
	static Scanner keyboardSc;
	int winner = 0;
	int player = 1;

	char playerZeroSymbol = 'O';
	char playerOneSymbol = 'X';

	public int readPlay() {
		int play;
		do {
			System.out.printf("\nPlayer %d, please enter the number of the square "
							+ "where you want to place your %c (or 0 to refresh the board): \n",
							player, (player == 1) ? playerOneSymbol : playerZeroSymbol);
			play = keyboardSc.nextInt();
		} while (play > 10 || play < 0);
		return play;
	}

	public void playGame() throws RemoteException{
		int play;
		boolean playAccepted;

		do {
			player = ++player % 2;
			do {
				System.out.println(ttt.currentBoard());
				play = readPlay();
				if (play == 10){
					System.out.println("Choose a new symbol:");
					char newSymbol = keyboardSc.next().charAt(0);
					if(newSymbol == ((player == 0)? playerOneSymbol : playerZeroSymbol)){
						System.out.println("Symbol already taken!");
						playAccepted = false;
						continue;
					}

					ttt.trocaSimbolos(newSymbol);
					if(player == 0)
						playerZeroSymbol = newSymbol;
					else
						playerOneSymbol = newSymbol;
					playAccepted = false;
				}else if (play != 0) {
					playAccepted = ttt.play( --play / 3, play % 3, player);
					if (!playAccepted)
						System.out.println("Invalid play! Try again.");
				} else
					playAccepted = false;
			} while (!playAccepted);
			winner = ttt.checkWinner();
		} while (winner == -1);
	}

	public void congratulate() {
		if (winner == 2)
			System.out.printf("\nHow boring, it is a draw\n");
		else
			System.out.printf(
					"\nCongratulations, player %d, YOU ARE THE WINNER!\n",
					winner);
	}

    public static void main(String[] args) throws Exception {
    	//Registry registry = LocateRegistry.getRegistry("//localhost", 8000);
        //ttt = (TTT) registry.lookup("//localhost:8000/" + name);
    	//System.setSecurityManager(new RMISecurityManager());
    	/* NONE OF THE ABOVE WORK! */
    	
        //ttt = (TTTService) Naming.lookup("//localhost:8000/" + name);
        ttt = (TTTService) Naming.lookup(args[0] + name);
		keyboardSc = new Scanner(System.in);
		Game g = new Game();
		try{
			g.playGame();
			g.congratulate();
		}catch(RemoteException e){
			System.out.println("Failed to comunicate with server!" + e.getMessage());
		}
    }

}
