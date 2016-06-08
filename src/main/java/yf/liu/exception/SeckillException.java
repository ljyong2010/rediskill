package yf.liu.exception;

/**
 * Created by Administrator on 2016/6/8.
 */
public class SeckillException extends RuntimeException{

    public SeckillException(String message){
        super(message);
    }

    public SeckillException(String message,Throwable cause){
        super(message,cause);
    }
}
