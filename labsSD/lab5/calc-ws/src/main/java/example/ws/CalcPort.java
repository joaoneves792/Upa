package calc;

import java.util.Random;

import javax.jws.WebService;

@WebService(
    endpointInterface="calc.CalcPortType",
    wsdlLocation="Calc.wsdl",
    name="Calc",
    portName="CalcPort",
    targetNamespace="urn:calc",
    serviceName="CalcService"
)
public class CalcPort implements CalcPortType {

	private Random random = new Random();

	/*@Override
	public String ping(String name) throws CalcFault_Exception {
		int nextInt = random.nextInt(3);
		if (nextInt == 0) {
			CalcFault faultInfo = new CalcFault();
			faultInfo.setNumber(nextInt);
			throw new CalcFault_Exception("simulated error in server", faultInfo);
		}
		return "Pong " + name + "!";
	}*/

    @Override
    public int sum(int a, int b){
        return a+b;
    }
    
    @Override
    public int sub(int a, int b){
        return a-b;
    }
    
    @Override
    public int mult(int a, int b){
        return a*b;
    }
    
    @Override
    public int intdiv(int a, int b)throws DivideByZero{
        if(b != 0)
            return a/b;
        else{
			DivideByZeroType faultInfo = new DivideByZeroType();
            throw new DivideByZero("Division by Zero!", faultInfo);
        }
    }    

}
