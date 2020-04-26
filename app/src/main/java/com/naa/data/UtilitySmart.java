package com.naa.data;



import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UtilitySmart {
    public static List<String> splitList(String original, String separator) {
        List<String> nodes = new ArrayList<String>();
        int index = original.indexOf(separator);
        while (index >= 0) {
            nodes.add(original.substring(0, index));
            original = original.substring(index + separator.length());
            index = original.indexOf(separator);
        }
        nodes.add(original);
        return nodes;
    }
    public static String Now() {
        Calendar calendar = Calendar.getInstance();
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());
    }
    public static final String MD5(final String s) {
        if (!s.equals("")) { 
            try { 
                MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
                digest.update(s.getBytes());
                byte messageDigest[] = digest.digest();

                // Create Hex String
                StringBuffer hexString = new StringBuffer();
                for (int i = 0; i < messageDigest.length; i++) {
                        String h = Integer.toHexString(0xFF & messageDigest[i]);
                        while (h.length() < 2)
                                h = "0" + h;
                        hexString.append(h);
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) { }

        }
        return "";
    }
    public static final String SHA1(final String s) {
        if (!s.equals("")) {
            try {
                MessageDigest digest = java.security.MessageDigest.getInstance("SHA1");
                digest.update(s.getBytes());
                byte messageDigest[] = digest.digest();

                StringBuffer hexString = new StringBuffer();
                for (int i = 0; i < messageDigest.length; i++) {
                    hexString.append(Integer.toString((messageDigest[i] & 0xff) + 0x100, 16).substring(1));
                }
                return hexString.toString();
            } catch (NoSuchAlgorithmException e) { }
        }            
        return "";
    }

    public static List<String> getKeys(Map maps){
        List<String> list = new ArrayList<String>();
        Iterator iterator = maps.keySet().iterator() ;
        while (iterator.hasNext()) {                 
              list.add(String.valueOf(iterator.next()));
        }
        return list;
    }
    public static String[] getKeysAsString(Map maps){
        List<String> list = getKeys(maps);
        String[] strings = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            strings[i] = list.get(i);            
        }
        return strings;
    }
    public boolean classExists (String className){
        try {
            Class.forName (className);
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }
    
    
    
    
    
    
    public static int getInt(String s) {
        return getNumber(s).intValue();
    }
    public static long getLong(String s) {
        return getNumber(s).longValue();
    }	 
    public static double getDouble(Object n) {
        return getNumber(n).doubleValue();
    }
    public static float getFloat(String s) {
        return getNumber(s).floatValue();				
    }
    public static int getNumberAsInt(String s) {
        return getNumber(s).intValue();
    }
    public static long getNumberAsLong(String s) {
        return getNumber(s).longValue();
    }	 
    public static double getNumberAsDouble(Object n) {
        return getNumber(n).doubleValue();
    }
    public static float getNumberAsFloat(String s) {
        return getNumber(s).floatValue();				
    }
    public static Number getNumber(Object n) {
        if (n instanceof Number) {
            return ((Number)n);
        }else if (isDecimalNumber(String.valueOf(n))){
            return Double.valueOf(String.valueOf(n));
        }
        return 0;
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
    private static boolean isDecimalNumber(String str) {
        return str.matches("^[-+]?[0-9]*.?[0-9]+([eE][-+]?[0-9]+)?$");
    }
    public static boolean isLongIntegerNumber(String str) {
        return str.matches("-?\\d+");  
    }


}
