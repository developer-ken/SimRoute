package tech.msgp.simroute;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.gsm.GsmCellLocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XLocationChanger_ABANDONED implements IXposedHookLoadPackage {
    //public double latitude, longitude;
    //public float accuracy;
    static public int lac=0,cid=0,satellites=8;
    static public ClassLoader classLoader;
     public Thread updaterthread;
    static XSharedPreferences pp;

     private Object os_LocationManagerService;
     private Method os_ReportLocation;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("XLocationChanger loading for " + lpparam.packageName);
        if (updaterthread == null) {
            updaterthread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(2000);
                        os_ReportLocationCall(getLocationX(), false);
                        while (!ishookenabled()) Thread.sleep(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        /*
            XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                    "getCellLocation", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("XLocationChanger.getCellLocation");
                            if (!ishookenabled()) return;
                            GsmCellLocation gsmCellLocation = new GsmCellLocation();
                            gsmCellLocation.setLacAndCid(lac, cid);
                            param.setResult(gsmCellLocation);
                        }
                    });

            XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", classLoader,
                    "onCellLocationChanged", CellLocation.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("XLocationChanger.onCellLocationChanged");
                            if (!ishookenabled()) return;
                            GsmCellLocation gsmCellLocation = new GsmCellLocation();
                            gsmCellLocation.setLacAndCid(lac, cid);
                            param.setResult(gsmCellLocation);
                        }
                    });

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                        "getPhoneCount", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                if (!ishookenabled()) return;
                                param.setResult(1);
                            }
                        });
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                        "getNeighboringCellInfo", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                if (!ishookenabled()) return;
                                param.setResult(new ArrayList<>());
                            }
                        });
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", classLoader,
                        "getAllCellInfo", new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                if (!ishookenabled()) return;
                                param.setResult(getCell(460, 0, lac, cid, 0, 0));
                            }
                        });
                XposedHelpers.findAndHookMethod("android.telephony.PhoneStateListener", classLoader,
                        "onCellInfoChanged", List.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                if (!ishookenabled()) return;
                                param.setResult(getCell(460, 0, lac, cid, 0, 0));
                            }
                        });
            }

            XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getScanResults", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!ishookenabled()) return;
                    param.setResult(new ArrayList<>());
                }
            });

            XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "getWifiState", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!ishookenabled()) return;
                    param.setResult(1);
                }
            });

            XposedHelpers.findAndHookMethod("android.net.wifi.WifiManager", classLoader, "isWifiEnabled", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!ishookenabled()) return;
                    param.setResult(true);
                }
            });

            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getMacAddress", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!ishookenabled()) return;
                    param.setResult("00-00-00-00-00-00-00-00");
                }
            });

            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getSSID", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!ishookenabled()) return;
                    param.setResult("null");
                }
            });

            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", classLoader, "getBSSID", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!ishookenabled()) return;
                    param.setResult("00-00-00-00-00-00-00-00");
                }
            });


            XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                    "getTypeName", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!ishookenabled()) return;
                            param.setResult("WIFI");
                        }
                    });
            XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                    "isConnectedOrConnecting", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!ishookenabled()) return;
                            param.setResult(true);
                        }
                    });

            XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                    "isConnected", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!ishookenabled()) return;
                            param.setResult(true);
                        }
                    });

            XposedHelpers.findAndHookMethod("android.net.NetworkInfo", classLoader,
                    "isAvailable", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!ishookenabled()) return;
                            param.setResult(true);
                        }
                    });

            XposedHelpers.findAndHookMethod("android.telephony.CellInfo", classLoader,
                    "isRegistered", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            if (!ishookenabled()) return;
                            param.setResult(true);
                        }
                    });

            XposedHelpers.findAndHookMethod(LocationManager.class, "getLastLocation", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("XLocationChanger.getLastLocation");
                    if (!ishookenabled()) return;
                    param.setResult(getLocationX());
                }
            });

            XposedHelpers.findAndHookMethod(LocationManager.class, "getLastKnownLocation", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("XLocationChanger.getLastKnownLocation");
                    if (!ishookenabled()) return;
                    param.setResult(getLocationX());
                }
            });


            XposedBridge.hookAllMethods(LocationManager.class, "getProviders", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("XLocationChanger.getProviders");
                    if (!ishookenabled()) return;
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add("gps");
                    param.setResult(arrayList);
                }
            });

            XposedHelpers.findAndHookMethod(LocationManager.class, "getBestProvider", Criteria.class, Boolean.TYPE, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("XLocationChanger.getBestProvider");
                    if (!ishookenabled()) return;
                    param.setResult("gps");
                }
            });

            XposedHelpers.findAndHookMethod(LocationManager.class, "addGpsStatusListener", GpsStatus.Listener.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("XLocationChanger.addGpsStatusListener");
                    if (!ishookenabled()) return;
                    if (param.args[0] != null) {
                        XposedHelpers.callMethod(param.args[0], "onGpsStatusChanged", 1);
                        XposedHelpers.callMethod(param.args[0], "onGpsStatusChanged", 3);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(LocationManager.class, "addNmeaListener", GpsStatus.NmeaListener.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("XLocationChanger.addNmeaListener");
                    if (!ishookenabled()) return;
                    param.setResult(false);
                }
            });
            GpsStatus stac;
            XposedHelpers.findAndHookMethod("android.location.LocationManager", classLoader,
                    "getGpsStatus", GpsStatus.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("XLocationChanger.getGpsStatus  V="+Data().getInt("satellites",-1));
                            if (!ishookenabled()) return;
                            GpsStatus gss = (GpsStatus) param.getResult();
                            if (gss == null)
                                return;
                            Class<?> clazz = GpsStatus.class;
                            Method m = null;
                            for (Method method : clazz.getDeclaredMethods()) {
                                if (method.getName().equals("setStatus")) {
                                    if (method.getParameterTypes().length > 1) {
                                        m = method;
                                        break;
                                    }
                                }
                            }
                            if (m == null)
                                return;

                            //access the private setStatus function of GpsStatus
                            m.setAccessible(true);

                            //make the apps belive GPS works fine now
                            /*
                            satellites = Data().getInt("satellites",8);
                            int svCount = satellites;
                            int[] prns = new int[satellites];
                            float[] snrs = new float[satellites];
                            float[] elevations = new float[satellites];
                            float[] azimuths = new float[satellites];

                            for (int i = 0; i < satellites; i++) {
                                prns[i] = i + 1;
                                snrs[i] = (int) (Math.random() * (59) + 40);
                                elevations[i] = 0;
                                azimuths[i] = (float) Math.random();
                            }

                            int ephemerisMask = 0x1f;
                            int almanacMask = 0x1f;

                            //5 satellites are fixed
                            int usedInFixMask = 0x1f;
                            /
                            int svCount = 5;
                            int[] prns = {1, 2, 3, 4, 5};
                            float[] snrs = {0, 0, 0, 0, 0};
                            float[] elevations = {0, 0, 0, 0, 0};
                            float[] azimuths = {0, 0, 0, 0, 0};
                            int ephemerisMask = 0x1f;
                            int almanacMask = 0x1f;

                            //5 satellites are fixed
                            int usedInFixMask = 0x1f;


                            XposedHelpers.callMethod(gss, "setStatus", svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                            param.args[0] = gss;
                            param.setResult(gss);
                            try {
                                m.invoke(gss, svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
                                param.setResult(gss);
                            } catch (Exception e) {
                                XposedBridge.log(e);
                            }
                        }
                    });

            for (Method method : LocationManager.class.getDeclaredMethods()) {
                if (method.getName().equals("requestLocationUpdates")
                        && !Modifier.isAbstract(method.getModifiers())
                        && Modifier.isPublic(method.getModifiers())) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("XLocationChanger.requestLocationUpdates");
                            if (!ishookenabled()) return;
                            if (param.args.length >= 4 && (param.args[3] instanceof LocationListener)) {

                                LocationListener ll = (LocationListener) param.args[3];

                                Class<?> clazz = LocationListener.class;
                                Method m = null;
                                for (Method method : clazz.getDeclaredMethods()) {
                                    if (method.getName().equals("onLocationChanged") && !Modifier.isAbstract(method.getModifiers())) {
                                        m = method;
                                        break;
                                    }
                                }
                                while(lock){Thread.sleep(0);};
                                lock=true;
                                listeners.add(ll);
                                lock=false;
                                try {
                                    if (m != null) {
                                        m.invoke(ll, getLocationX());
                                    }
                                } catch (Exception e) {
                                    XposedBridge.log(e);
                                }
                            }
                        }
                    });
                }

                if (method.getName().equals("requestSingleUpdate")
                        && !Modifier.isAbstract(method.getModifiers())
                        && Modifier.isPublic(method.getModifiers())) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("XLocationChanger.requestSingleUpdate");
                            if (!ishookenabled()) return;
                            if (param.args.length >= 3 && (param.args[1] instanceof LocationListener)) {

                                LocationListener ll = (LocationListener) param.args[3];

                                Class<?> clazz = LocationListener.class;
                                Method m = null;
                                for (Method method : clazz.getDeclaredMethods()) {
                                    if (method.getName().equals("onLocationChanged") && !Modifier.isAbstract(method.getModifiers())) {
                                        m = method;
                                        break;
                                    }
                                }

                                try {
                                    //mlisteners.put(m,ll);
                                    m.invoke(ll,getLocationX());
                                } catch (Exception e) {
                                    XposedBridge.log(e);
                                }
                            }
                        }
                    });
                }
            }
            */
        /*
        XposedBridge.hookAllMethods(Location.class, "getLatitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!ishookenabled()) return;
                XSharedPreferences a = Data();
                Double lat = Double.longBitsToDouble(a.getLong("lat",0));
                XposedBridge.log("XLocationChanger.getLatitude="+lat);
                param.setResult((double)lat);
            }
        });

        XposedBridge.hookAllMethods(Location.class, "getLongitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!ishookenabled()) return;
                XSharedPreferences a = Data();
                Double lon = Double.longBitsToDouble(a.getLong("lon",0));
                XposedBridge.log("XLocationChanger.getLongitude="+lon);
                param.setResult((double)lon);
            }
        });
        */
        XposedBridge.hookAllConstructors(LocationManager.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (param.args.length == 2) {
                    Context context = (Context) param.args[0];

                    XposedHelpers.findAndHookMethod(context.getClass(), "checkCallingOrSelfPermission", String.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            if (ishookenabled())
                                param.setResult(PackageManager.PERMISSION_GRANTED);
                        }
                    });
                    /*
                    os_LocationManagerService = param.args[1];
                    Class<?> clazz = os_LocationManagerService.getClass();
                    os_ReportLocation = null;
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.getName().equals("reportLocation") && !Modifier.isAbstract(method.getModifiers())) {
                            os_ReportLocation = method;
                            break;
                        }
                    }
                    if (os_ReportLocation != null) {
                        updaterthread.start();
                    }
                    *.
                     */
                }
            }
        });
/*
        XposedHelpers.findAndHookMethod("com.android.server.LocationManagerService", lpparam.classLoader, "reportLocation", Location.class, boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Location location = (Location) param.args[0];
                XposedBridge.log("实际 系统 经度"+location.getLatitude() +" 系统 纬度"+location.getLongitude() +"系统 精度 "+location.getAccuracy());
                //XSharedPreferences xsp =new XSharedPreferences("com.markypq.gpshook","markypq");
                if (ishookenabled()){
                    location = modyLocationX(location);
                    XposedBridge.log("hook 系统 经度"+location.getLatitude() +" 系统 纬度"+location.getLongitude() +"系统 加速度 "+location.getAccuracy());
                }

            }
        });
        XposedBridge.log("XLocationChanger loaded for "+lpparam.packageName);
    }

 */
    }
    public void os_ReportLocationCall(Location location, boolean passive) throws InvocationTargetException, IllegalAccessException {
        os_ReportLocation.invoke(os_LocationManagerService,location,passive);
    }

/*
    @org.jetbrains.annotations.NotNull
    private static ArrayList getCell(int mcc, int mnc, int lac, int cid, int sid, int networkType) {
        ArrayList arrayList = new ArrayList();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            CellInfoGsm cellInfoGsm = null;
            cellInfoGsm = (CellInfoGsm) XposedHelpers.newInstance(CellInfoGsm.class);
            XposedHelpers.callMethod(cellInfoGsm, "setCellIdentity", XposedHelpers.newInstance(CellIdentityGsm.class, new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(
                    lac), Integer.valueOf(cid)}));
            CellInfoCdma cellInfoCdma = null;
            cellInfoCdma = (CellInfoCdma) XposedHelpers.newInstance(CellInfoCdma.class);
            XposedHelpers.callMethod(cellInfoCdma, "setCellIdentity", XposedHelpers.newInstance(CellIdentityCdma.class, new Object[]{Integer.valueOf(lac), Integer.valueOf(sid), Integer.valueOf(cid), Integer.valueOf(0), Integer.valueOf(0)}));
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) XposedHelpers.newInstance(CellInfoWcdma.class);
            XposedHelpers.callMethod(cellInfoWcdma, "setCellIdentity", XposedHelpers.newInstance(CellIdentityWcdma.class, new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(lac), Integer.valueOf(cid), Integer.valueOf(300)}));
            CellInfoLte cellInfoLte = (CellInfoLte) XposedHelpers.newInstance(CellInfoLte.class);
            XposedHelpers.callMethod(cellInfoLte, "setCellIdentity", XposedHelpers.newInstance(CellIdentityLte.class, new Object[]{Integer.valueOf(mcc), Integer.valueOf(mnc), Integer.valueOf(cid), Integer.valueOf(300), Integer.valueOf(lac)}));
            if (networkType == 1 || networkType == 2) {
                arrayList.add(cellInfoGsm);
            } else if (networkType == 13) {
                arrayList.add(cellInfoLte);
            } else if (networkType == 4 || networkType == 5 || networkType == 6 || networkType == 7 || networkType == 12 || networkType == 14) {
                arrayList.add(cellInfoCdma);
            } else if (networkType == 3 || networkType == 8 || networkType == 9 || networkType == 10 || networkType == 15) {
                arrayList.add(cellInfoWcdma);
            }
        }
        return arrayList;
    }
*/
    public static Location getLocationX(){
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setAltitude(Double.longBitsToDouble(Data().getLong("alt",0)));
        loc.setLatitude(Double.longBitsToDouble(Data().getLong("lat",0)));
        loc.setLongitude(Double.longBitsToDouble(Data().getLong("lon",0)));
        loc.setAccuracy(Data().getFloat("acc",0));
        loc.setBearing(Data().getFloat("bea",0));
        loc.setSpeed(Data().getFloat("speed",0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            loc.setSpeedAccuracyMetersPerSecond(2F);
        }
        Bundle bundle = new Bundle();
        bundle.putInt("satellites", Data().getInt("satellites",8));
        loc.setExtras(bundle);
        loc.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= 17) {
            loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        XposedBridge.log("Location generated:"+loc.getLatitude()+","+loc.getLongitude());
        return loc;
    }

    public static Location modyLocationX(Location loc){
        loc.setAltitude(Double.longBitsToDouble(Data().getLong("alt",0)));
        loc.setLatitude(Double.longBitsToDouble(Data().getLong("lat",0)));
        loc.setLongitude(Double.longBitsToDouble(Data().getLong("lon",0)));
        loc.setAccuracy(Data().getFloat("acc",loc.getAccuracy()));
        loc.setBearing(Data().getFloat("bea",loc.getBearing()));
        loc.setSpeed(Data().getFloat("speed",loc.getSpeed()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            loc.setSpeedAccuracyMetersPerSecond(2F);
        }
        Bundle bundle = new Bundle();
        bundle.putInt("satellites", Data().getInt("satellites",8));
        loc.setExtras(bundle);
        loc.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= 17) {
            loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        XposedBridge.log("Location patched:"+loc.getLatitude()+","+loc.getLongitude());
        return loc;
    }

    public boolean ishookenabled(){
        //XposedBridge.log("XLocationChanger mode="+Data().getBoolean("hook",false));
        return Data().getBoolean("hook",false);
        //return true;
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
