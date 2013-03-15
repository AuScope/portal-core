package org.auscope.portal.core.exceptions;

public class InitializationException extends Exception{

    /**
     * Generated uid;
     */
    private static final long serialVersionUID = -7587273790081702602L;

    public InitializationException(){
        super();
    }

    public InitializationException(String msg){
        super(msg);
    }

    public InitializationException(String msg, Throwable cause){
        super(msg,cause);
    }

    public InitializationException(Throwable cause){
        super(cause);
    }

}
