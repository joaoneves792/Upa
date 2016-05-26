package ttt;


//import java.rmi.Naming;
import java.rmi.registry.*;

public class TTTServer {
	public static void main(String args[]){
		int registryPort = 8000;
		
        try{
            TTTService ttt = new TTT();
            
            Registry reg = LocateRegistry.createRegistry(registryPort);
			reg.rebind("TicTacToe", ttt);
			
	    System.out.println("Registry Created.");
			
	    //Comentário do professor no ficheiro de exemplo 
	    //
	    //Alternativa mais realista seria ter um RMI Registry autï¿½nomo
	    //disponï¿½vel no porto default (implica definir "codebase" para
	    //permitir ao RMI Registry obter remotamente a interface dos
	    //objectos que sejam registados):
	    //Naming.rebind("//localhost/TicTacToe", ttt); //Error?: connection refused
           
            System.out.println("Server Ready.");
        }catch(Exception e) {
            System.out.println("Server Exception: " + e.getMessage());
        }
    }
}
