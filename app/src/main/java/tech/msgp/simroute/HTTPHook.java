package tech.msgp.simroute;

import android.os.Build;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class HTTPHook {
    public static void initHooking(XC_LoadPackage.LoadPackageParam lpparam) throws NoSuchMethodException {
        final Class <?> httpUrlConnection = findClass("java.net.HttpURLConnection",lpparam.classLoader);
        hookAllConstructors(httpUrlConnection, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args.length != 1 || param.args[0].getClass() != URL.class)
                    return;

                //TEST.log("HttpURLConnection: " + param.args[0] + "");
            }
        });

        XC_MethodHook ResponseHook = new XC_MethodHook() {

            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                HttpURLConnection urlConn = (HttpURLConnection) param.thisObject;

                if (urlConn != null) {
                    StringBuilder sb = new StringBuilder();
                    int code = urlConn.getResponseCode();
                    if(code==200){

                        Map<String, List<String>> properties = urlConn.getHeaderFields();
                        if (properties != null && properties.size() > 0) {

                            for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
                                sb.append(entry.getKey() + ": " + entry.getValue() + ", ");
                            }
                        }
                    }

                    //TEST.log( "RESPONSE: method=" + urlConn.getRequestMethod() + " " +
                     //       "URL=" + urlConn.getURL().toString() + " " +
                    //        "Params=" + sb.toString());
                }

            }
        };




        findAndHookMethod("java.io.OutputStream", lpparam.classLoader, "write", byte[].class,int.class,int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                OutputStream os = (OutputStream)param.thisObject;
                if(!os.toString().contains("internal.http"))
                    return;
                String print = new String((byte[]) param.args[0]);
                //TEST.log("DATA"+print.toString());
                Pattern pt = Pattern.compile("(\\w+=.*)");
                Matcher match = pt.matcher(print);
                if(match.matches())
                {
                    //TEST.log("POST DATA: "+print.toString());
                }
            }
        });


        findAndHookMethod("java.io.OutputStream", lpparam.classLoader, "write", byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                OutputStream os = (OutputStream)param.thisObject;
                if(!os.toString().contains("internal.http"))
                    return;
                String print = new String((byte[]) param.args[0]);
                //TEST.log("DATA: "+print.toString());
                Pattern pt = Pattern.compile("(\\w+=.*)");
                Matcher match = pt.matcher(print);
                if(match.matches())
                {
                    //TEST.log("POST DATA: "+print.toString());
                }
            }
        });

        try {
            final Class<?> okHttpClient = findClass("com.android.okhttp.OkHttpClient", lpparam.classLoader);
            if(okHttpClient != null) {
                findAndHookMethod(okHttpClient, "open", URI.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        URI uri = null;
                        if (param.args[0] != null)
                            uri = (URI) param.args[0];
                        //TEST.log( "OkHttpClient: " + uri.toString() + "");
                    }
                });
            }
        } catch (Error e) {

        }



        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {

                findAndHookMethod("libcore.net.http.HttpURLConnectionImpl", lpparam.classLoader, "getOutputStream", ResponseHook);
            } else {
                findAndHookMethod("com.android.okhttp.internal.http.HttpURLConnectionImpl", lpparam.classLoader, "getOutputStream", ResponseHook);
                findAndHookMethod("com.android.okhttp.internal.http.HttpURLConnectionImpl", lpparam.classLoader, "getInputStream", ResponseHook);
            }
        } catch (Error e){
        }


        /* Hook org.apache.http 包中的 HttpPost 请求 */
        /*
        findAndHookMethod("org.apache.http.impl.client.AbstractHttpClient",
                lpparam.classLoader, "execute", HttpUriRequest.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!param.args[0].getClass().getCanonicalName().contains("HttpPost")) {
                            return;
                        }
                        HttpPost request = (HttpPost) param.args[0];
                        String url = request.getURI().toString();
                        String test1=  request.getMethod();


                        //TEST.log("HttpPost——getURI:"+url);
                        //TEST.log("HttpPost——getMethod:"+test1);



                        PreferenceActivity.Header[] headers = request.getAllHeaders();
                        if (headers != null) {
                            for (int i = 0; i < headers.length; i++) {
                                TEST.log("headers:"+headers[i].getName() + ":" + headers[i].getValue());
                            }
                        }
                        HttpPost httpPost = (HttpPost) request;

                        HttpEntity entity = httpPost.getEntity();
                        String contentType = null;
                        if (entity.getContentType() != null) {
                            contentType = entity.getContentType().getValue();
                            if (URLEncodedUtils.CONTENT_TYPE.equals(contentType)) {

                                try {
                                    byte[] data = new byte[(int) entity.getContentLength()];
                                    entity.getContent().read(data);
                                    String content = new String(data, HTTP.UTF_8);
                                    TEST.log("HTTP POST Content : " + content);
                                } catch (IllegalStateException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            } else if (contentType.startsWith(HTTP.UTF_8)) {
                                try {
                                    byte[] data = new byte[(int) entity.getContentLength()];
                                    entity.getContent().read(data);
                                    String content = new String(data, contentType.substring(contentType.lastIndexOf("=") + 1));
                                    //TEST.log("HTTP POST Content : " + content);
                                } catch (IllegalStateException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            }
                        }else{
                            byte[] data = new byte[(int) entity.getContentLength()];
                            try {
                                entity.getContent().read(data);
                                String content = new String(data, HTTP.UTF_8);
                                //TEST.log("HTTP POST Content : " + content);
                            } catch (IllegalStateException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                });
         */
    }
}