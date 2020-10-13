/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.naa.data;

import android.os.Bundle;

import com.naa.data.google.gson.JsonReader;
import com.naa.data.google.gson.JsonToken;
import com.naa.data.google.gson.JsonWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author rkrzmail
 */
public class Nson implements Serializable {


    public OnVariableListener getVariableListener() {
        return variable;
    }

    private Object internalObject;

    public Nson(Object object) {
        this.internalObject = object;
    }

    private OnVariableListener variable;

    private Nson(String nson, OnVariableListener variable) {
        this.variable = variable;
        this.internalObject = parseNson(nson, this.variable);
    }

    public Nson() {
        this.internalObject = null;
    }

    public Nson(String json) {
        this.internalObject = parseJson(json);
    }

    public Nson(Map map) {
        this.internalObject = map;
    }

    public Nson(List list) {
        this.internalObject = list;
    }

    public Nson(InputStream stream) {
        this.internalObject = parseJson(stream);
    }

    public Nson(Nson nson) {
        if (nson != null) {
            this.internalObject = nson.getInternalObject();
        }
    }

    public static Nson readJson(String json) {
        return new Nson(json);
    }

    public static Nson readNson(String nson) {
        return new Nson(nson, null);
    }

    public static Nson readNson(String nson, OnVariableListener variable) {
        return new Nson(nson, variable);
    }

    public static Nson newArray() {
        return new Nson(new ArrayList());
    }

    public static Nson newObject() {
        return new Nson(new TreeMap(String.CASE_INSENSITIVE_ORDER));
    }

    public static Nson newNull() {
        return new Nson();
    }

    public String toJson() {
        StringWriter writer = new StringWriter();
        toJson(writer);
        return writer.toString();
    }

    public void toJson(OutputStream outputStream) {
        toJson(new OutputStreamWriter(outputStream));
    }

    public void toJson(Writer writer) {
        JsonWriter jsonWriter = new JsonWriter(writer);
        Object dataObject = internalObject;
        try {
            if (dataObject == null) {
                //nothing                
            } else if (dataObject instanceof List) {
                OutJsonArray(jsonWriter, (List) dataObject);
            } else if (dataObject instanceof Map) {
                OutJsonObject(jsonWriter, (Map) dataObject);
            }
            jsonWriter.flush();
        } catch (IOException e) {
        }
    }

    private static void OutJsonArray(JsonWriter jsonWriter, List list) throws IOException {
        jsonWriter.beginArray();
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            OutJsonValue(jsonWriter, iterator.next());
        }
        jsonWriter.endArray();
    }

    private static void OutJsonObject(JsonWriter jsonWriter, Map object) throws IOException {
        jsonWriter.beginObject();
        Iterator iterator = object.keySet().iterator();

        while (iterator.hasNext()) {
            String key = String.valueOf(iterator.next());
            jsonWriter.name(key);
            OutJsonValue(jsonWriter, object.get(key));
        }
        jsonWriter.endObject();
    }

    private static void OutJsonValue(JsonWriter jsonWriter, Object object) throws IOException {
        if (object == null || object instanceof NullPointerException) {
            String nil = null;
            jsonWriter.value(nil);
            //}else if (object instanceof Smartset) { //new nextcompatible ?cant restore
            //    OutJsonValue(jsonWriter, ( (Smartset)object).toNson().getInternalObject());
        } else if (object instanceof Nson) {
            OutJsonValue(jsonWriter, ((Nson) object).getInternalObject());
        } else if (object instanceof Map) {
            OutJsonObject(jsonWriter, (Map) object);
        } else if (object instanceof List) {
            OutJsonArray(jsonWriter, (List) object);
        } else if (object instanceof String) {
            jsonWriter.value((String) object);
        } else if (object instanceof Number) {
            jsonWriter.value((Number) object);
        } else if (object instanceof Boolean) {
            jsonWriter.value(((Boolean) object).booleanValue());
        } else {
            jsonWriter.value(String.valueOf(object));
        }
    }

    private Object parseNson(String json, OnVariableListener listener) {
        Object dataObject;
        error = null;
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(json.getBytes())));
            reader.setLenient(true);
            switch (reader.peek()) {
                case BEGIN_ARRAY:
                    dataObject = JsonArray(reader, listener);
                    break;
                case BEGIN_OBJECT:
                    dataObject = JsonObject(reader, listener);
                    break;
                default:
                    dataObject = "";
                    break;
            }
            return dataObject;
        } catch (IOException ex) {
            error = ex.getMessage();
            ex.printStackTrace();
        }
        return null;
    }

    private final Object parseJson(InputStream is) {
        Object dataObject;
        error = null;
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(is));
            switch (reader.peek()) {
                case BEGIN_ARRAY:
                    dataObject = JsonArray(reader, null);
                    break;
                case BEGIN_OBJECT:
                    dataObject = JsonObject(reader, null);
                    break;
                default:
                    dataObject = "";
                    break;
            }
            return dataObject;
        } catch (Exception ex) {
            error = ex.getMessage();
            ex.printStackTrace();
        }
        return null;
    }

    private Object parseJson(String json) {
        Object dataObject;
        error = null;
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(json.getBytes())));
            switch (reader.peek()) {
                case BEGIN_ARRAY:
                    dataObject = JsonArray(reader, null);
                    break;
                case BEGIN_OBJECT:
                    dataObject = JsonObject(reader, null);
                    break;
                default:
                    dataObject = "";
                    break;
            }
            return dataObject;
        } catch (Exception ex) {
            error = ex.getMessage();
            ex.printStackTrace();
        }
        return null;
    }

    private static Object JsonArray(JsonReader reader, OnVariableListener listener) throws IOException {
        List vector = new ArrayList();
        reader.beginArray();
        while (reader.hasNext()) {
            vector.add(JsonValue(reader, listener));
        }
        reader.endArray();
        return vector;
    }

    private static Object JsonObject(JsonReader reader, OnVariableListener listener) throws IOException {
        Map hashtable = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        reader.beginObject();
        while (reader.hasNext()) {
            String name = JsonMember(reader, listener);
            hashtable.put(name, JsonValue(reader, listener));
        }
        reader.endObject();
        return hashtable;
    }

    private static String JsonMember(JsonReader reader, OnVariableListener listener) throws IOException {
        if (reader.peek().equals(JsonToken.NAME)) {
            if (listener != null && reader.peekUnQuote()) {
                return String.valueOf(listener.get(reader.nextName(), false, false));
            } else if (listener != null && reader.peekSingleQuote()) {
                return String.valueOf(listener.get(reader.nextName(), true, false));
            } else if (listener != null && reader.peekDoubleQuote()) {
                return String.valueOf(listener.get(reader.nextName(), false, true));
            }
            return reader.nextName();
        }
        return "";//must error
    }

    public interface OnVariableListener {

        Object get(String name, boolean singlequote, boolean doublequote);
    }

    private static Object JsonValue(JsonReader reader, OnVariableListener listener) throws IOException {
        switch (reader.peek()) {
            case BEGIN_ARRAY:
                return JsonArray(reader, listener);
            case BEGIN_OBJECT:
                return JsonObject(reader, listener);
            case STRING:
                if (listener != null && reader.peekUnQuote()) {
                    return listener.get(reader.nextString(), false, false);
                } else if (listener != null && reader.peekSingleQuote()) {
                    return listener.get(reader.nextString(), true, false);
                } else if (listener != null && reader.peekDoubleQuote()) {
                    return listener.get(reader.nextString(), false, true);
                }
                return reader.nextString();
            case NUMBER:
                String s = reader.nextString();
                if (isLongIntegerNumber(s)) {
                    return getLong(s);
                } else {
                    return getDouble(s);
                }
            case BOOLEAN:
                return reader.nextBoolean();
            case NULL:
                reader.nextNull();
                return Nson.newNull();
            default:
                return reader.nextString();
        }
    }

    /*
    new Methode 14/4/2018
     */

    public Nson add(Serializable value) {
        return addInternal(value);
    }

    public Nson add(Nson value) {
        return addInternal(value.internalObject);
    }

    public Nson add(String value) {
        return addInternal(value);
    }

    public Nson add(Boolean value) {
        return addInternal(value);
    }

    public Nson add(Number value) {
        return addInternal(value);
    }

    public Nson add(Bundle value) {
        return addInternal(value);
    }

    private Nson addInternal(Object value) {
        if (value == null) {
            value = Nson.newNull();
        }
        if (internalObject instanceof List) {
            List list = (List) internalObject;
            list.add(value);
        }
        return this;
    }

    /*
    new Methode 14/4/2018
     */
    public Nson set(String key, Nson value) {
        return setInternal(key, value.getInternalObject());
    }

    public Nson set(String key, Serializable value) {
        return setInternal(key, value);
    }

    public Nson set(String key, String value) {
        return setInternal(key, value);
    }

    public Nson set(String key, Boolean value) {
        return setInternal(key, value);
    }

    public Nson set(String key, Number value) {
        return setInternal(key, value);
    }

    private Nson setInternal(String key, Object value) {
        if (value == null) {
            value = Nson.newNull();
        }
        if (internalObject instanceof Map) {
            Map map = (Map) internalObject;
            map.put(key, value);
        }
        return this;
    }

    //Save [update|replace or insert array]    
    private Nson saveInternal(int index, Object value, boolean insert) {
        if (value == null) {
            value = Nson.newNull();
        }
        if (internalObject instanceof List) {
            List list = (List) internalObject;
            try {
                if (list.size() > index && index >= 0) {
                    if (insert) {
                        list.add(index, value);
                    } else {
                        list.set(index, value);
                    }
                } else {
                    if (insert) {
                        list.add(value);//always insert
                    } else {
                        list.add(index, value);
                    }
                }
            } catch (Exception e) {
            }
        }
        return this;
    }

    public Nson insertData(int index) {
        return saveInternal(index, null, true);
    }

    public Nson set(int index, Nson value) {
        return saveInternal(index, value.internalObject, false);
    }

    public Nson set(int index, String value) {
        return saveInternal(index, value, false);
    }

    public Nson set(int index, Boolean value) {
        return saveInternal(index, value, false);
    }

    public Nson set(int index, Number value) {
        return saveInternal(index, value, false);
    }

    //remove array/object    
    public void remove(String key) {
        if (internalObject instanceof Map) {
            Map map = (Map) internalObject;
            if (map.containsKey(key)) {
                map.remove(key);
            }
        }
    }

    public void remove(Nson nListArray) {
        if (internalObject instanceof List) {
            List list = (List) internalObject;
            if (list.contains(nListArray)) {
                list.remove(nListArray);
            }
        }
    }

    public void remove(int index) {
        if (internalObject instanceof List) {
            List list = (List) internalObject;
            if (list.size() > index && index >= 0) {
                list.remove(index);
            }
        }
    }

    public int size() {
        if (isNsonArray()) {
            return ((List) internalObject).size();
        } else if (isNsonObject()) {
            return ((Map) internalObject).size();
        }
        return 0;
    }

    public Nson getObjectKeys() {
        Nson nson = Nson.newArray();
        if (isNsonObject()) {
            Map map = (Map) getInternalObject();
            Iterator iterator = map.keySet().iterator();

            while (iterator.hasNext()) {
                String key = String.valueOf(iterator.next());
                nson.addInternal(key);
            }
        }
        return nson;
    }

    public Nson getObjectValues(Nson keys) {
        Nson nson = Nson.newArray();
        for (int i = 0; i < keys.size(); i++) {
            nson.addInternal(getData(keys.getData(i).asString()));
        }
        return nson;
    }

    public Object getInternalObject() {
        return internalObject;//maybe Null !!!
    }

    private Nson getData(String key) {
        if (internalObject instanceof Map) {
            Map map = (Map) internalObject;
            if (map.containsKey(key)) {
                return new Nson(map.get(key));
            }
        }
        return new Nson();
    }

    private Nson getData(int index) {
        if (internalObject instanceof List) {
            List list = (List) internalObject;
            if (list.size() > index && index >= 0) {
                return new Nson(list.get(index));
            }
        }
        return new Nson();
    }

    /*
    new Methode 14/2/2019
     */
    public Nson get(int index) {
        return getData(index);
    }

    public Nson get(String key) {
        return getData(key);
    }

    public Nson get(Nson stream) {
        Nson out = this;
        for (int i = 0; i < stream.size(); i++) {
            if (stream.getData(i).isNumber()) {
                out = out.getData(stream.getData(i).asInteger());
            } else {
                out = out.getData(stream.getData(i).asString());
            }
        }
        return out;
    }

    protected Nson streamAddData(String stream, Object data) {
        return streamAddData(Nson.readNson(stream), data);
    }

    protected Nson streamAddData(Nson stream, Object data) {
        Nson out = this;
        for (int i = 0; i < stream.size(); i++) {
            if (stream.getData(i).isNumber()) {
                out = out.getData(stream.getData(i).asInteger());
            } else {
                out = out.getData(stream.getData(i).asString());
            }
        }
        out.addInternal(data);
        return out;
    }

    protected Nson streamSetData(String stream, Object data) {
        return streamSetData(Nson.readNson(stream), data);
    }

    protected Nson streamSetData(Nson stream, Object data) {
        Nson out = this;
        for (int i = 0; i < stream.size() - 1; i++) {
            if (stream.getData(i).isNumber()) {
                out = out.getData(stream.getData(i).asInteger());
            } else {
                out = out.getData(stream.getData(i).asString());
            }
        }
        for (int i = stream.size() - 1; i < stream.size(); i++) {
            if (stream.getData(i).isNumber()) {
                out.saveInternal(stream.getData(i).asInteger(), data, true);
            } else {
                out.setInternal(stream.getData(i).asString(), data);
            }
        }
        return out;
    }

    public boolean containsKey(String key) {
        if (isNsonObject()) {
            Map map = (Map) getInternalObject();
            return map.containsKey(key);
        }
        return false;
    }

    public boolean containsValue(String value) {
        if (isNsonObject()) {
            Nson keys = getObjectKeys();
            for (int i = 0; i < keys.size(); i++) {
                if (getData(keys.getData(i).asString()).asString().equals(value)) {
                    return true;
                }
            }
        } else if (isNsonArray()) {
            for (int i = 0; i < size(); i++) {
                if (getData(i).asString().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsValue(Object value) {
        if (isNsonObject()) {
            Nson keys = getObjectKeys();
            for (int i = 0; i < keys.size(); i++) {
                if (getData(keys.getData(i).asString()).asString().equals(value)) {
                    return true;
                }
            }
        } else if (isNsonArray()) {
            for (int i = 0; i < size(); i++) {
                if (getData(i).asString().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNsonArray() {
        return internalObject instanceof List;
    }

    public boolean isNsonObject() {
        return internalObject instanceof Map;
    }

    public boolean isNull() {
        if (internalObject == null) {
            return true;
        } else if (internalObject instanceof Nson) {
            return ((Nson) internalObject).getInternalObject() == null;
        }
        return false;
    }

    public boolean isNson() {
        return isNsonArray() || isNsonObject();
    }

    public boolean isNumber() {
        return internalObject instanceof Number;
    }

    public boolean isBoolean() {
        return internalObject instanceof Boolean;
    }

    public boolean isString() {
        return internalObject instanceof String;
    }

    public boolean isPrimitive() {
        return isNumber() || isBoolean();
    }

    public List asArray() {
        if (internalObject instanceof List) {
            return (List) internalObject;
        }
        return new ArrayList();
    }

    public Map asObject() {
        if (internalObject instanceof Map) {
            return (Map) internalObject;
        }
        return new HashMap();
    }

    public boolean asBoolean() {
        if (internalObject instanceof Boolean) {
            return (Boolean) internalObject;
        }
        return Boolean.getBoolean(String.valueOf(internalObject));
    }

    public int asInteger() {
        if (internalObject instanceof Integer) {
            return (Integer) internalObject;
        }
        return getNumber(String.valueOf(internalObject)).intValue();
    }

    public Number asNumber() {
        if (internalObject instanceof Number) {
            return (Number) internalObject;
        }
        return getNumber(String.valueOf(internalObject));
    }

    public long asLong() {
        if (internalObject instanceof Long) {
            return (Long) internalObject;
        }
        return getNumber(String.valueOf(internalObject)).longValue();
    }

    public double asDouble() {
        if (internalObject instanceof Double) {
            return (Double) internalObject;
        }
        return getNumber(String.valueOf(internalObject)).doubleValue();
    }

    public String asDecimalString() {
        if (internalObject instanceof Double) {
            return BigDecimal.valueOf((Double) internalObject).toPlainString();
        } else if (internalObject instanceof Integer) {
            return BigDecimal.valueOf((Integer) internalObject).toPlainString();
        } else if (internalObject instanceof Long) {
            return BigDecimal.valueOf((Long) internalObject).toPlainString();
        }
        return BigDecimal.valueOf(asNumber().doubleValue()).toPlainString();
    }

    public String asString() {
        if (internalObject == null) {
            return "";
        }
        if (internalObject instanceof Nson) {
            if (((Nson) internalObject).getInternalObject() == null) {
                return ((Nson) internalObject).asString();
            }
        }

        return String.valueOf(internalObject);
    }

    public String[] asStringArray() {
        Nson nson = getObjectKeys();
        if (nson.getInternalObject() instanceof List) {
            String[] strs = new String[nson.size()];
            for (int i = 0; i < nson.size(); i++) {
                strs[i] = nson.getData(i).asString();
            }
            return strs;
        }
        return new String[0];
    }

    public Nson asJson() {
        if (internalObject instanceof Nson) {
            return ((Nson) internalObject);
            //}else if (internalObject instanceof Smartset) {
            //    return (((Smartset)internalObject).toNson());
        } else {
            return Nson.readJson(String.valueOf(internalObject));
        }
    }

    public String asType() {
        return type(internalObject);
    }

    public static String type(Object internalObject) {
        if (internalObject instanceof List) {
            return "array";
        } else if (internalObject instanceof Map) {
            return "object";
        } else if (internalObject instanceof String) {
            return "string";
        } else if (internalObject instanceof Boolean) {
            return "boolean";
        } else if (internalObject instanceof Number) {
            if (internalObject instanceof Double) {
                return "double";
            } else if (internalObject instanceof Long) {
                return "long";
            } else if (internalObject instanceof Integer) {
                return "int";
            } else if (internalObject instanceof Float) {
                return "float";
            }
            return "number";
        } else if (internalObject instanceof Nson) {
            if (((Nson) internalObject).isNull()) {
                return "null";
            }
            return "nson";
        } else if (internalObject == null) {
            return "null";
        }
        return internalObject.getClass().getSimpleName().toLowerCase();
    }

    public Nson copy() {
        return Nson.readJson(toJson());
    }

    @Override
    public String toString() {
        if (internalObject == null) {
            return "";
        }
        if (isNson()) {
            return toJson();
        }
        return String.valueOf(internalObject);
    }

    private String error;

    public String getError() {
        return error != null ? error : "";
    }

    private static int getInt(String s) {
        return getNumber(s).intValue();
    }

    private static long getLong(String s) {
        return getNumber(s).longValue();
    }

    private static double getDouble(Object n) {
        return getNumber(n).doubleValue();
    }

    private static float getFloat(String s) {
        return getNumber(s).floatValue();
    }

    private static Number getNumber(Object n) {
        if (n instanceof Number) {
            return ((Number) n);
        } else if (isDecimalNumber(String.valueOf(n))) {
            return Double.valueOf(String.valueOf(n));
        }
        return 0;
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private static boolean isDecimalNumber(String str) {
        return str.matches("^[-+]?[0-9]*.?[0-9]+([eE][-+]?[0-9]+)?$");
    }

    private static boolean isLongIntegerNumber(String str) {
        return str.matches("-?\\d+");
    }
}
