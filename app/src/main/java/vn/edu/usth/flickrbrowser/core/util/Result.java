package vn.edu.usth.flickrbrowser.core.util;
public class Result<T>{
    public final T data;
    public final Throwable error;
    private Result(T d, Throwable e){data=d;error=e;}
    public static <T> Result<T> success(T d){return new Result<>(d,null);}
    public static <T> Result<T> error(Throwable e){return new Result<>(null,e);}
    public boolean isSuccess(){return error==null;}
}