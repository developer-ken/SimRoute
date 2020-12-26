package tech.msgp.simroute;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.GpsStatus;
import android.location.ILocationManager;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Random;

import javax.xml.transform.Source;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class MainActivity extends AppCompatActivity {

    Button gogogo;
    EditText textbox;
    Handler han;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getPreferencesAndKeepItReadable(this,"XLocationChanger");
        editor = sp.edit();
        gogogo = (Button)findViewById(R.id.gogogo);
        textbox = (EditText)findViewById(R.id.routedata);
        han = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Toast.makeText(MainActivity.this,(String)msg.obj, Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void pop(String msg){
        Message message = new Message();
        message.obj = msg;
        han.sendMessage(message);
    }

    public static SharedPreferences getPreferencesAndKeepItReadable(Context ctx, String prefName) {
        SharedPreferences prefs = ctx.getSharedPreferences(prefName,MODE_WORLD_READABLE);
        File prefsFile = new File(ctx.getFilesDir() + "/../shared_prefs/" + prefName + ".xml");
        prefsFile.setReadable(true, false);
        return prefs;
    }

    public void getClipboard(View sender){
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData data = cm.getPrimaryClip();
        ClipData.Item item = data.getItemAt(0);
        textbox.setText(item.getText().toString());
    }

    public void gogogo(View sender){
        new Thread(()->{
            byte[] base64decodedBytes = new byte[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                base64decodedBytes = Base64.getDecoder().decode(textbox.getText().toString());
                if(base64decodedBytes.length < 5) base64decodedBytes = Base64.getDecoder().decode("ew0KICAgICJtZXRhIjoNCiAgICB7DQogICAgICAgICJzb3VyY2UiOiJjb2RpbmciLA0KICAgICAgICAidGl0bGUiOiJFeGFtcGxlIFJvdXRlRmlsZSIsDQogICAgICAgICJjb21tZW50IjoiVGhpcyBpcyBqdXN0IGFuIGV4YW1wbGUuIiwNCiAgICAgICAgInNpZ25hdHVyZSI6IioqKioqKioqKioqKiINCiAgICB9LA0KICAgICJjbWQiOlsNCiAgICAgICAgew0KICAgICAgICAgICAgImMiOiJ2IiwgDQogICAgICAgICAgICAidiI6IjEzIg0KICAgICAgICB9LA0KICAgICAgICB7DQogICAgICAgICAgICAiYyI6ImwiLCANCiAgICAgICAgICAgICJvIjoxMTguODg3ODQ0LCANCiAgICAgICAgICAgICJhIjozMS45Mjg1MDksIA0KICAgICAgICAgICAgImgiOjEwMCANCiAgICAgICAgfSwNCiAgICAgICAgew0KICAgICAgICAgICAgImMiOiJkIiwgIA0KICAgICAgICAgICAgInQiOjEwMDAwDQogICAgICAgIH0sDQogICAgICAgIHsNCiAgICAgICAgICAgICJjIjoidiIsIA0KICAgICAgICAgICAgInYiOiIxMyINCiAgICAgICAgfSwNCiAgICAgICAgew0KICAgICAgICAgICAgImMiOiJsIiwgDQogICAgICAgICAgICAibyI6MTE4Ljg4NzU0NCwgDQogICAgICAgICAgICAiYSI6MzEuOTI4NTE3LCANCiAgICAgICAgICAgICJoIjoxMDAgDQogICAgICAgIH0sDQogICAgICAgIHsNCiAgICAgICAgICAgICJjIjoidiIsIA0KICAgICAgICAgICAgInYiOjEwDQogICAgICAgIH0sDQogICAgICAgIHsNCiAgICAgICAgICAgICJjIjoiZCIsICANCiAgICAgICAgICAgICJ0IjoxMDAwMA0KICAgICAgICB9LA0KICAgICAgICB7DQogICAgICAgICAgICAiYyI6ImwiLCANCiAgICAgICAgICAgICJvIjoxMTguODg3NDQ4LCANCiAgICAgICAgICAgICJhIjozMS45MjgyNzksIA0KICAgICAgICAgICAgImgiOjEwMCANCiAgICAgICAgfSwNCiAgICAgICAgew0KICAgICAgICAgICAgImMiOiJkIiwgIA0KICAgICAgICAgICAgInQiOjEwMDAwDQogICAgICAgIH0sDQogICAgICAgIHsNCiAgICAgICAgICAgICJjIjoibCIsIA0KICAgICAgICAgICAgIm8iOjExOC44ODcwOCwgDQogICAgICAgICAgICAiYSI6MzEuOTI3ODI3LCANCiAgICAgICAgICAgICJoIjoxMDAgDQogICAgICAgIH0sDQogICAgICAgIHsNCiAgICAgICAgICAgICJjIjoiZCIsICANCiAgICAgICAgICAgICJ0IjoxMDAwMA0KICAgICAgICB9LA0KICAgICAgICB7DQogICAgICAgICAgICAiYyI6InYiLCANCiAgICAgICAgICAgICJ2Ijo5DQogICAgICAgIH0sDQogICAgICAgIHsNCiAgICAgICAgICAgICJjIjoibCIsIA0KICAgICAgICAgICAgIm8iOjExOC44ODY4NDYsIA0KICAgICAgICAgICAgImEiOjMxLjkyNzcwNSwgDQogICAgICAgICAgICAiaCI6MTAwIA0KICAgICAgICB9LA0KICAgICAgICB7DQogICAgICAgICAgICAiYyI6InYiLCANCiAgICAgICAgICAgICJ2IjoiMTMiDQogICAgICAgIH0sDQogICAgICAgIHsNCiAgICAgICAgICAgICJjIjoiZCIsICANCiAgICAgICAgICAgICJ0IjoxMDAwMA0KICAgICAgICB9LA0KICAgICAgICB7DQogICAgICAgICAgICAiYyI6ImwiLCANCiAgICAgICAgICAgICJvIjoxMTguODg2MDkyLCANCiAgICAgICAgICAgICJhIjozMS45Mjc4MTIsIA0KICAgICAgICAgICAgImgiOjEwMCANCiAgICAgICAgfSwNCiAgICAgICAgew0KICAgICAgICAgICAgImMiOiJkIiwgIA0KICAgICAgICAgICAgInQiOjEwMDAwDQogICAgICAgIH0sDQogICAgICAgIHsNCiAgICAgICAgICAgICJjIjoibCIsIA0KICAgICAgICAgICAgIm8iOjExOC44ODU2NDIsIA0KICAgICAgICAgICAgImEiOjMxLjkyODMzMywgDQogICAgICAgICAgICAiaCI6MTAwIA0KICAgICAgICB9LA0KICAgICAgICB7DQogICAgICAgICAgICAiYyI6InYiLCANCiAgICAgICAgICAgICJ2Ijo4DQogICAgICAgIH0sDQogICAgICAgIHsNCiAgICAgICAgICAgICJjIjoiZCIsICANCiAgICAgICAgICAgICJ0IjoxMDAwMA0KICAgICAgICB9LA0KICAgICAgICB7DQogICAgICAgICAgICAiYyI6ImwiLCANCiAgICAgICAgICAgICJvIjoxMTguODg1NzE0LCANCiAgICAgICAgICAgICJhIjozMS45Mjg2NjMsIA0KICAgICAgICAgICAgImgiOjEwMCANCiAgICAgICAgfSwNCiAgICAgICAgew0KICAgICAgICAgICAgImMiOiJ2IiwgDQogICAgICAgICAgICAidiI6IjEzIg0KICAgICAgICB9LA0KICAgICAgICB7DQogICAgICAgICAgICAiYyI6ImQiLCAgDQogICAgICAgICAgICAidCI6MTAwMDANCiAgICAgICAgfSwNCiAgICAgICAgew0KICAgICAgICAgICAgImMiOiJsIiwgDQogICAgICAgICAgICAibyI6MTE4Ljg4NTkzOSwgDQogICAgICAgICAgICAiYSI6MzEuOTI5MDA3LCANCiAgICAgICAgICAgICJoIjoxMDAgDQogICAgICAgIH0sDQogICAgICAgIHsNCiAgICAgICAgICAgICJjIjoiZCIsICANCiAgICAgICAgICAgICJ0IjoxMDAwMA0KICAgICAgICB9LA0KICAgICAgICB7DQogICAgICAgICAgICAiYyI6ImwiLCANCiAgICAgICAgICAgICJvIjoxMTguODg2NDMzLCANCiAgICAgICAgICAgICJhIjozMS45MjkyOTgsIA0KICAgICAgICAgICAgImgiOjEwMCANCiAgICAgICAgfSwNCiAgICAgICAgew0KICAgICAgICAgICAgImMiOiJ2IiwgDQogICAgICAgICAgICAidiI6IjEzIg0KICAgICAgICB9LA0KICAgICAgICB7DQogICAgICAgICAgICAiYyI6InYiLCANCiAgICAgICAgICAgICJ2Ijo5DQogICAgICAgIH0sDQogICAgICAgIHsNCiAgICAgICAgICAgICJjIjoiZCIsICANCiAgICAgICAgICAgICJ0IjoxMDAwMA0KICAgICAgICB9LA0KICAgICAgICB7DQogICAgICAgICAgICAiYyI6ImwiLCANCiAgICAgICAgICAgICJvIjoxMTguODg3Mjg2LCANCiAgICAgICAgICAgICJhIjozMS45MjkwNTMsIA0KICAgICAgICAgICAgImgiOjEwMCANCiAgICAgICAgfQ0KICAgIF0NCn0=");
                //gICAgICAgIHsKICAgICAgICAgICAgImMiOiJkIiwgCiAgICAgICAgICAgICJ0Ijo1MDAwCiAgICAgICAgfQogICAgXQp9");
            }
            try {
                Random rand = new Random();
                String jsonstr = new String(base64decodedBytes, "utf-8");
                JSONObject jsonObject = new JSONObject(jsonstr);
                JSONArray jarray = jsonObject.getJSONArray("cmd");

                double lastlat=25565,lastlon=25565,lastalt = 25565;
                float accurancy=2F;
                float speedaccurancy=1F;
                float bearing=1.0F;
                int satellites=8;
                editor.putBoolean("hook",true);
                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject jb = jarray.getJSONObject(i);
                    switch(jb.getString("c")){
                        case "l"://Set location (lat,lon,alt)
                            pop("-=路点=-\n"+jb.getDouble("a")+","+jb.getDouble("o"));
                        {
                            editor.putLong("alt",Double.doubleToLongBits(jb.getDouble("h")));
                            editor.putLong("lat",Double.doubleToLongBits(jb.getDouble("a")));
                            editor.putLong("lon",Double.doubleToLongBits(jb.getDouble("o")));
                            editor.putFloat("acc",accurancy);
                            editor.putFloat("bea",bearing);
                            editor.putInt("satellites",satellites);
                            editor.putBoolean("hook",true);
                            editor.commit();
                            lastlat = jb.getDouble("a");
                            lastalt = jb.getDouble("h");
                            lastlon = jb.getDouble("o");
                            /*
                            Bundle bundle = new Bundle();
                            bundle.putInt("satellites", satellites);
                            loc.setExtras(bundle);
                            */
                        }
                            break;
                        case "d"://Delay for miliseconds
                            pop("休息"+jb.getInt("t")+"ms");
                            sleep(jb.getInt("t"));
                            break;
                        case "s"://Auto-Smooth the movement between two locations
                            // (draw a line and add "l"s on the line)
                            int timesleep = jb.getInt("t");
                            if(timesleep<2000){
                                pop("E:平滑移动条件t不成立\n休息");
                            }else{
                                int parts = timesleep/2000;
                                int extra = timesleep%2000;
                                Boolean changed=false;
                                double nextlat=lastlat,nextalt=lastalt,nextlon=lastlon;
                                {//get next pos
                                    for(int j=i;j<jarray.length();j++){
                                        JSONObject jj = jarray.getJSONObject(j);
                                        if(jj.getString("c").equals("l")){
                                            nextalt = jj.getDouble("h");
                                            nextlat = jj.getDouble("a");
                                            nextlon = jj.getDouble("o");
                                            changed = true;
                                            break;
                                        }
                                    }
                                }
                                if(!changed){
                                    pop("E:平滑移动条件d不成立\n休息"+jb.getInt("t")+"ms");
                                }else{
                                    pop("平滑移动"+jb.getInt("t")+"ms");
                                }
                                double stepalt = (nextalt-lastalt)/(double)parts,
                                        steplat = (nextlat-lastlat)/(double)parts,
                                        steplon = (nextlon-lastlon)/(double)parts;
                                double avgspeed = getDistance(lastlon,lastlat,nextlon,nextlat)/timesleep;
                                for(double j=1D;j<=parts;j++) {
                                    {
                                        pop("平滑移动\n"+steplat+","+steplon+"*"+j+"\n"+(lastlat+(steplat*j))+","+(lastlon+(steplon*j)));
                                        editor.putLong("alt",Double.doubleToLongBits(lastalt+(stepalt*j)));
                                        editor.putLong("lat",Double.doubleToLongBits(lastlat+(steplat*j)));
                                        editor.putLong("lon",Double.doubleToLongBits(lastlon+(steplon*j)));
                                        editor.putFloat("acc",accurancy);
                                        editor.putFloat("bea",bearing);
                                        editor.putFloat("speed",(float)avgspeed);
                                        editor.putInt("satellites",satellites);
                                        editor.putBoolean("hook",true);
                                        editor.commit();
                                    }
                                    sleep(2000);
                                }
                                sleep(extra);
                            }
                            break;
                        case "c":// Set the accurancy
                            try {
                                accurancy = (float) jb.getDouble("p");
                            }catch(Exception ignored){}
                            try {
                                speedaccurancy = (float)jb.getDouble("s");
                            }catch(Exception ignored){}
                            editor.putFloat("acc",accurancy);
                            editor.commit();
                            pop("位置精度："+accurancy);
                            break;
                        case "b":// Set the bearing (direction towards)
                            bearing = (float)jb.getDouble("v");
                            editor.putFloat("bea",bearing);
                            editor.commit();
                            pop("朝向："+bearing);
                            break;
                        case "v":// Set the statellite count
                            satellites = jb.getInt("v");
                            editor.putInt("satellites",satellites);
                            editor.commit();
                            pop("卫星："+satellites);
                            break;
                        case "r":
                            i=0;
                            pop("循环");
                            break;
                     }

                }
                pop("-=完成=-");
            } catch (UnsupportedEncodingException | JSONException e) {
                pop("无法解析路径数据:\n"+e.getMessage());
                e.printStackTrace();
            }catch(SecurityException e){
                pop("请求权限提升失败\n"+e.getMessage());
            }catch(Throwable err){
                pop("未知错误:"+err.getMessage());
            }
            editor.putBoolean("hook",false);
            editor.commit();
        }).start();
    }

    /**
     * 通过AB点经纬度获取距离
     * @return 距离(单位：米)
     */
    public static double getDistance(double long1, double lat1, double long2, double lat2) {
        double a, b, R;
        R = 6378137; //地球半径
        lat1 = lat1 * Math.PI / 180.0;
        lat2 = lat2 * Math.PI / 180.0;
        a = lat1 - lat2;
        b = (long1 - long2) * Math.PI / 180.0;
        double d;
        double sa2, sb2;
        sa2 = Math.sin(a / 2.0);
        sb2 = Math.sin(b / 2.0);
        d = 2 * R * Math.asin(Math.sqrt(sa2 * sa2 + Math.cos(lat1) * Math.cos(lat2) * sb2 * sb2));
        return d;
    }

    private void sleep(long milisec) throws InterruptedException {
        long last = SystemClock.elapsedRealtime();
        while((SystemClock.elapsedRealtime()-last)<milisec){Thread.sleep(0);}
    }
}