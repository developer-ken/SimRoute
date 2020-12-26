package tech.msgp.simroute;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import java.lang.reflect.Method;
import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XLocationChanger implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        //HTTPHook.initHooking(lpparam);
        XposedBridge.hookAllConstructors(LocationManager.class,new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (param.args.length == 2) {
                    Context context = (Context) param.args[0]; //这里的 context
                    // XposedBridge.log(" 对 " + getProgramNameByPackageName(context) + " 模拟位置");
                    //把权限的检查 hook掉
                    XposedHelpers.findAndHookMethod(context.getClass(), "checkCallingOrSelfPermission", String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            //  XposedBridge.log("检测权限"+param.args[0].toString()+" 结果"+param.getResult());
                            if (param.args[0].toString().contains("INSTALL_LOCATION_PROVIDER")) {
                                param.setResult(PackageManager.PERMISSION_GRANTED);
                            }
                        }
                    });
                    XposedBridge.log("LocationManager : " + context.getPackageName() + " class:= " + param.args[1].getClass().toString());
                    Method[] methods = param.args[1].getClass().getMethods();
                    for (Method m : methods) {
                        //LocationHook. hookLoctionChanged(m);
                        //   if ((!param.args[1].getClass().equals("com.android.server.LocationManagerService"))) {
                        if (m.getName().equals("reportLocation")) {
                            m.setAccessible(true);
                            XposedBridge.log("hook" + param.args[1].getClass().toString());
                            XposedBridge.hookMethod(m, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    super.beforeHookedMethod(param);
                                    Location location = (Location) param.args[0];
                                    XposedBridge.log("多样化实际    系统 经度" + location.getLatitude() + " 系统 纬度" + location.getLongitude() + "系统 加速度 " + location.getAccuracy());
                                    if (ishookenabled()) {
                                        location=modyLocationX((Location)param.getResult());
                                        XposedBridge.log("多样化hook 系统 经度" + location.getLatitude() + " 系统 纬度" + location.getLongitude() + "系统 加速度 " + location.getAccuracy());
                                        param.args[0] =location;
                                    }

                                }
                            });
                        } else if (m.getName().equals("getLastLocation") || m.getName().equals("getLastKnownLocation")) {
                            XposedBridge.hookMethod(m, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    Location location = getLocationX();
                                    if (param.getResult() != null) {
                                        Location lo = modyLocationX((Location)param.getResult());
                                        lo.setLatitude(location.getLatitude());
                                        lo.setLongitude(location.getLongitude());
                                        param.setResult(lo);
                                    } else {
                                        param.setResult(location);
                                    }
                                }
                            });
                        }
                        //}
                    }
                }
            }
        });

        if (lpparam.packageName.contains("pao") || lpparam.packageName.contains("gps")  || lpparam.packageName.contains("run")){ //注意 不加包名过滤 容易把手机干的开不了机
            XposedBridge.log(" 抓取到目标应用 " +lpparam.packageName + " ！");
            LocationHook.HookAndChange(lpparam.classLoader,0,0);
        }
    }
    /**
     * 通过包名获取应用程序的名称。
     * @param context
     *            Context对象。
     *            包名。
     * @return 返回包名所对应的应用程序的名称。
     */
    public static String getProgramNameByPackageName(Context context) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            name = pm.getApplicationLabel(
                    pm.getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    public static XSharedPreferences pp;

    public static Location modyLocationX(Location loc){
        loc.setAltitude(Double.longBitsToDouble(Data().getLong("alt",0)));
        loc.setLatitude(Double.longBitsToDouble(Data().getLong("lat",0)));
        loc.setLongitude(Double.longBitsToDouble(Data().getLong("lon",0)));
        loc.setAccuracy(Data().getFloat("acc",loc.getAccuracy()));
        loc.setBearing(Data().getFloat("bea",loc.getBearing()));
        loc.setSpeed(Data().getFloat("speed",loc.getSpeed()));
        XposedBridge.log("Location patched:"+loc.getLatitude()+","+loc.getLongitude());
        return loc;
    }

    public static Location getLocationX(){
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setAltitude(Double.longBitsToDouble(Data().getLong("alt",0)));
        loc.setLatitude(Double.longBitsToDouble(Data().getLong("lat",0)));
        loc.setLongitude(Double.longBitsToDouble(Data().getLong("lon",0)));
        loc.setAccuracy(Data().getFloat("acc",10));
        loc.setBearing(Data().getFloat("bea",0));
        loc.setSpeed(Data().getFloat("speed",0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            loc.setSpeedAccuracyMetersPerSecond(2F);
        }
        Bundle bundle = new Bundle();
        bundle.putInt("satellites", Data().getInt("satellites",5));
        loc.setExtras(bundle);
        loc.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= 17) {
            loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        XposedBridge.log("Location generated:"+loc.getLatitude()+","+loc.getLongitude());
        return loc;
    }

    public static boolean ishookenabled(){
        return Data().getBoolean("hook",false);
    }

    public static XSharedPreferences Data(){
        if(pp==null){
            pp=new XSharedPreferences("tech.msgp.simroute", "XLocationChanger");
        }else{
            pp.reload();
        }
        return pp;
    }
}
