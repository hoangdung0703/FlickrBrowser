package vn.edu.usth.flickrbrowser.core.model;
public class PhotoItem {
    public String id="", server="", secret="", title="", owner="";
    public String thumbUrl="", fullUrl="", pageUrl="";
    public String getThumbUrl(){
        if (thumbUrl!=null && !thumbUrl.isEmpty()) return thumbUrl;
        if (!server.isEmpty() && !id.isEmpty() && !secret.isEmpty())
            return "https://live.staticflickr.com/"+server+"/"+id+"_"+secret+"_w.jpg";
        return "";
    }
    public String getFullUrl(){
        if (fullUrl!=null && !fullUrl.isEmpty()) return fullUrl;
        if (!server.isEmpty() && !id.isEmpty() && !secret.isEmpty())
            return "https://live.staticflickr.com/"+server+"/"+id+"_"+secret+"_b.jpg";
        return getThumbUrl();
    }
}
