package pt.upa.transporter.ws.cli;

import pt.upa.transporter.ws.BadJobFault_Exception;

/**
 * Created by joao on 5/2/16.
 */
public class TransporterClientException extends Exception{
    TransporterClientException(String m){
       super(m);
    }
}
