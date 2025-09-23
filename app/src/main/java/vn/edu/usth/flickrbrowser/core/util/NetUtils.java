package vn.edu.usth.flickrbrowser.core.util;
import android.content.Context; import android.net.ConnectivityManager; import android.net.NetworkInfo;
public class NetUtils { public static boolean hasNetwork(Context ctx){ try{ ConnectivityManager cm=(ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo n= cm!=null? cm.getActiveNetworkInfo():null; return n!=null && n.isConnected(); }catch(Exception e){return true;} } }
