package com.dmsys.airdiskpro.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 判断网络的的工具类
 * @author liuzongyao
 *
 */
public abstract class NetHelper
{
    
    //public static String mNetType = null;
    
    public static boolean isWifiNet(Context c)
    {
        boolean bRet = false;
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo && wifiInfo.isConnectedOrConnecting())
        {
            bRet = true;
        }
        return bRet;
    }
    
    public static boolean isMobileNet(final Context c)
    {
        boolean ret = false;
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != cm)
        {
            NetworkInfo mobileInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (null != mobileInfo && mobileInfo.isConnectedOrConnecting())
            {
                ret = true;
            }
        }
        return ret;
    }
    
    public static boolean isNetworkAvailable(Context context)
    {
        Context ct = context.getApplicationContext();
        ConnectivityManager cm = (ConnectivityManager) ct.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == cm)
        {
            return false;
        }
        else
        {
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (null != info)
            {
                for (int i = 0; i < info.length; i++)
                {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static String getCurrentSsid(Context context)
    {
        String ssid = null;
        
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (null != wifiInfo)
        {
            ssid = wifiInfo.getSSID();
        }
        
        return ssid;
    }
    
    // 以网络编码格式返回，如 3gnet/3gwap/uninet/uniwap/cmnet/cmwap/ctnet/ctwap
    @SuppressLint("DefaultLocale")
    public static String getNetTypeName(Context context)
    {
        String typeName = "null";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        
        if (null == info)
        {
            typeName = "null";
        }
        else if (info.getTypeName() != null)
        {
            typeName = info.getTypeName().toLowerCase(); // WIFI/MOBILE    
            
            if (!typeName.equals("wifi"))
            {
                if (info.getExtraInfo() != null)
                {
                    typeName = info.getExtraInfo().toLowerCase(); //3gnet/3gwap/uninet/uniwap/cmnet/cmwap/ctnet/ctwap
                }
                
                if (typeName.equals("#777") && info.getSubtypeName() != null)
                    typeName = info.getSubtypeName();
            }
        }
        
        return typeName;
    }
    
    // wuwenhua add: 以wifi、2g、3g、4g作为返回格式
	public static String getNetTypeNameEx(Context context) {
		
		String type = "null";
		ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo mobileInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		if(null != mobileInfo && mobileInfo.isConnected()) {
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			int netType = tm.getNetworkType();
			 switch (netType) {
	            case TelephonyManager.NETWORK_TYPE_GPRS:
	            case TelephonyManager.NETWORK_TYPE_EDGE:
	            case TelephonyManager.NETWORK_TYPE_CDMA:
	            case TelephonyManager.NETWORK_TYPE_1xRTT:
	            case TelephonyManager.NETWORK_TYPE_IDEN:
	            	type = "2g";
	            	break;
	            case TelephonyManager.NETWORK_TYPE_UMTS:
	            case TelephonyManager.NETWORK_TYPE_EVDO_0:
	            case TelephonyManager.NETWORK_TYPE_EVDO_A:
	            case TelephonyManager.NETWORK_TYPE_HSDPA:
	            case TelephonyManager.NETWORK_TYPE_HSUPA:
	            case TelephonyManager.NETWORK_TYPE_HSPA:
	            case TelephonyManager.NETWORK_TYPE_EVDO_B:
	            case TelephonyManager.NETWORK_TYPE_EHRPD:
	            case TelephonyManager.NETWORK_TYPE_HSPAP:
	            	type = "3g";
	            	break;
	            case TelephonyManager.NETWORK_TYPE_LTE:
	            	type = "4g";
	            	break;
	            default:
	            	type = "null";
	            	break;
	        }
			
		}
		if(null != wifiInfo && wifiInfo.isConnected()) {
			type = "wifi";
		}
		return type;
	}
	
    
    public static int getNetType(Context context)
    {
        int type = -1;
        
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        NetworkInfo mobileInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        
        if (null != mobileInfo && mobileInfo.isConnected())
        {
            type = ConnectivityManager.TYPE_MOBILE;
        }
        if (null != wifiInfo && wifiInfo.isConnected())
        {
            type = ConnectivityManager.TYPE_WIFI;
        }
        
        return type;
    }
    
    public static int getCurrentNetType(Context ctx)
    {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni == null ? -1 : ni.getType();
    }
    
//    public static void setNetworkType(NetworkInfo ni)
//    {
//        if (null != ni)
//        {
//            if (ni.getType() == ConnectivityManager.TYPE_WIFI)
//            {
//                mNetType = "wifi";
//            }
//            else
//            {
//                mNetType = getNetworkSubType(ni.getSubtype());
//            }
//        }
//        
//    }
    
    public static String getNetworkSubType(int subType)
    {
        String type = null;
        switch (subType)
        {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
                // 2G网络
                type = "2g";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
                // 3G网络
                type = "3g";
                break;
            default:
                type = "other";
                break;
        }
        
        return type;
    }
    
    /**
     * 获得ip地址【Old版本，在MX351手机上获取的ip地址有错】，修改by GeXianglin
     * @param context
     * @return
     * @throws SocketException
     */
	public static String getIPAddress(Context context) throws SocketException {
		String ret = null;
		if ((ret = getIPAddressPrivate(context)) != null) {
			return ret;
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
					en.hasMoreElements();) {
				
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
						enumIpAddr.hasMoreElements();) {
					
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} else {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
					en.hasMoreElements();) {
				
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
						enumIpAddr.hasMoreElements();) {
					
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		}
		return null;
	}
	
	/*
	 * 获取Ip地址，原来的ip地址获取，在魅族MX351手机上获取ip地址有错，增加这个在老方法里头。
	 * @param context
	 * @return
	 * @author GeXianglin
	 */
	private static String getIPAddressPrivate(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifiManager.getDhcpInfo();
		if (dhcp != null) {
			int ipAddress = dhcp.ipAddress;
			return (ipAddress & 0xff) + "." + ((ipAddress >> 8) & 0xff) + "." + 
					((ipAddress >> 16) & 0xff) + "." + ((ipAddress >> 24) & 0xff);
		}
		
		return null;
	}
    
    static private AlertDialog.Builder mSetNetDialog = null;
    
    public static void showNotWifiNotic(final Context context)
    {
        if (null == mSetNetDialog)
        {
            mSetNetDialog = new AlertDialog.Builder(context);
            mSetNetDialog.setTitle("提示");
            mSetNetDialog.setMessage("你正在使用非WIFI网络，建议在wifi下使用");
            mSetNetDialog.setPositiveButton("设置",
                    new DialogInterface.OnClickListener()
                    {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            try
                            {
                                dialog.dismiss();
                                context.startActivity(new Intent(
                                        android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            finally
                            {
                                mSetNetDialog = null;
                            }
                        }
                    });
            
            mSetNetDialog.setNegativeButton("取消",
                    new DialogInterface.OnClickListener()
                    {
                        
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            try
                            {
                                dialog.dismiss();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            finally
                            {
                                mSetNetDialog = null;
                            }
                        }
                    });
            mSetNetDialog.create();
        }
        mSetNetDialog.show();
    }
    
    /**
     * 获取当前wifi的ssid
     * 
     */
    public static String getSsid(Context ctx)
    {
        WifiManager mWifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.getConnectionInfo() != null)
        {
            return mWifiManager.getConnectionInfo().getSSID();
        }
        return null;
    }
    
    /**
     * 
     * 判断当前是否处于wifi网络
     */
    public static boolean isWifi(Context ctx)
    {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.getType() == ConnectivityManager.TYPE_WIFI)
        {
            return true;
        }
        return false;
    }
    
    /**
     * 判断当前是否处于移动网络
     */
    public static boolean isMobileNetwork(Context ctx)
    {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.getType() == ConnectivityManager.TYPE_MOBILE)
        {
            return true;
        }
        return false;
    }
    
    /**
     * 判断任务的SSID是否和当前网络的SSID相同(和taskInfo解耦)
     * 
     * @param context
     * @param ssid
     * @return
     */
    public static boolean isSSIDSame(Context context, String ssid)
    {
        boolean ret = false;
        if (context != null && ssid != null)
        {
            if (ssid != null && !ssid.equals(""))
            {
                ret = ssid.equals(getSsid(context));
            }
            else
            {
                ret = true;
            }
        }
        else
        {
            if (ssid == null || ssid.equals(""))
            {
                ret = true;
            }
        }
        return ret;
    }
    
    /**
     * 判断传入的IP地址与手机的wifi是否在同一局域网中。
     * 
     * @param context
     * @param PCIP
     * @return
     */
    public static boolean isNetSame(Context context, String PCIP)
    {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = null;
        if (null != mWifiManager && null != (dhcp = mWifiManager.getDhcpInfo()))
        {
            int networkIP = dhcp.ipAddress;
            int networkMask = dhcp.netmask;
            int networkId = networkIP & networkMask;
            int pcaddress = ConvertUtil.ipAddrToInt(PCIP);
            if (0 != pcaddress)
            {
                int pcNetWorkId = pcaddress & networkMask;
                if (pcNetWorkId == networkId)
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    @SuppressLint("DefaultLocale")
    public static String getLocalIpAddress()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        }
        catch (SocketException ex)
        {
        }
        return null;
    }
    
}
