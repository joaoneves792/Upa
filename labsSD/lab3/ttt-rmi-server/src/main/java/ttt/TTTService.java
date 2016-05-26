package ttt;

public interface TTTService extends java.rmi.Remote {
	public String currentBoard() throws java.rmi.RemoteException;
	
	public boolean play(int row, int column, int player) throws java.rmi.RemoteException; 
	
	public int checkWinner() throws java.rmi.RemoteException;

	public void trocaSimbolos(char simbolo) throws java.rmi.RemoteException;
}
