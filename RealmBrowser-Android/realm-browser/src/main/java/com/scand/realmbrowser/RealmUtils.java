package com.scand.realmbrowser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by Slabodeniuk on 6/16/15.
 */
class RealmUtils {
    static final int DEFAULT_ROW_COUNT = 50;

    static boolean isFieldRealmList(@NonNull Field field) {
        return RealmList.class.isAssignableFrom(field.getType());
    }

    static boolean isFieldRealmObject(@NonNull Field field) {
        return RealmObject.class.isAssignableFrom(field.getType());
    }

    static void setNotParamFieldValue(@NonNull RealmObject obj, @NonNull Field field,
                                      Object newValue) {
        String methodName = createSetterName(field);

        try {
            Class<?> type = field.getType();
            Method method = obj.getClass().getMethod(methodName, field.getType());
            method.invoke(obj, newValue);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    static Object getNotParamFieldValue(@NonNull RealmObject obj, @NonNull Field field) {
        String methodName = createGetterName(field);

        try {
            Method method = obj.getClass().getMethod(methodName);
            return method.invoke(obj);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    static String getFieldDisplayedName(@NonNull RealmObject obj, @NonNull Field field) {
        if (isFieldRealmList(field)) {
            return getRealmListFieldDisplayingName(obj, field);
        } else if (isFieldRealmObject(field)) {
            Object value = getRealmObjectFieldValue(obj, field);
            return value == null ? "null" : getRealmObjectFieldDisplayingName(field);
        } else {
            Object result = getNotParamFieldValue(obj, field);
            if (result != null) {
                if (field.getType() == byte[].class || field.getType() == Byte[].class) {
                    byte[] array = (byte[]) result;

                    StringBuilder builder = new StringBuilder();
                    for (byte b : array) {
                        builder.append(String.format("0x%02X", b));
                        builder.append(" ");
                    }

                    return builder.toString();
                }
            }
            return result == null ? "null" : result.toString();
        }
    }

    @Nullable
    static RealmList<RealmObject> getRealmListFieldValue(@NonNull RealmObject obj, @NonNull Field field) {
        String methodName = createGetterName(field);
        RealmList<RealmObject> result = null;

        try {
            Method method = obj.getClass().getMethod(methodName);
            Object resultObj = method.invoke(obj);
            if (resultObj != null) {
                //noinspection unchecked
                result = (RealmList<RealmObject>) resultObj;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return result;
    }

    static RealmObject getRealmObjectFieldValue(@NonNull RealmObject obj,
                                                @NonNull Field field) {
        String methodName = createGetterName(field);
        RealmObject result = null;

        try {
            Method method = obj.getClass().getMethod(methodName);
            Object resultObj = method.invoke(obj);
            if (resultObj != null) {
                result = (RealmObject) resultObj;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return result;
    }

    static List<Field> getFields(Class<? extends RealmObject> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        List<Field> result = new ArrayList<>();
        for (Field f : fields) {
            int mods = f.getModifiers();
            // remove constants
            if (!(Modifier.isStatic(mods) || Modifier.isFinal(mods))) {
                result.add(f);
            }
        }
        return result;
    }

    private static String getRealmListFieldDisplayingName(@NonNull RealmObject obj,
                                                          @NonNull Field field) {
        ParameterizedType pType = (ParameterizedType) field.getGenericType();
        String rawType = pType.getRawType().toString();
        int rawTypeIndex = rawType.lastIndexOf(".");
        if (rawTypeIndex > 0) {
            rawType = rawType.substring(rawTypeIndex + 1);
        }

        String argument = pType.getActualTypeArguments()[0].toString();
        int argumentIndex = argument.lastIndexOf(".");
        if (argumentIndex > 0) {
            argument = argument.substring(argumentIndex + 1);
        }

        int objNumber = RealmUtils.getRealmListFieldValue(obj, field).size();

        return String.format("%s<%s> (%d)", rawType, argument, objNumber);
    }

    private static String getRealmObjectFieldDisplayingName(@NonNull Field field) {
        return field.getType().getSimpleName();
    }

    private static String createGetterName(Field field) {
        String methodName;
        if (field.getType().equals(boolean.class)) {
            if (field.getName().startsWith("is")) {
                methodName = field.getName();
            } else {
                methodName = "is" + capitalize(field.getName());
            }
        } else {
            methodName = "get" + capitalize(field.getName());
        }

        return methodName;
    }

    private static String createSetterName(Field field) {
        String methodName = "set" + capitalize(field.getName());
        return methodName;
    }

    static void clearClassData(Realm realm, Class<? extends RealmObject> clazz) {
        realm.beginTransaction();
        realm.where(clazz).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    static RealmList<RealmObject> generateData(Realm realm, Class<? extends RealmObject> clazz, int count) {
        RealmList<RealmObject> resultList = new RealmList<>();
        RealmObject obj;
        String fieldName;
        Method setter;
        List<Field> fields = getFields(clazz);
        Object fieldValue;

        if (fields.size() <= 0)
            return resultList;

        RealmResults<? extends RealmObject> existing = realm.where(clazz).findAll();

        int from = existing.size();

        try {
            for (int i = 0; i < count; i++) {
                if (i < from) {
                    obj = existing.get(i);
                } else {
                    obj = clazz.newInstance();

                    for (Field field : fields) {
                        fieldValue = generateFieldValue(realm, field, i);
                        setNotParamFieldValue(obj, field, fieldValue);
                    }
                }
                resultList.add(obj);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        realm.beginTransaction();
        realm.copyToRealm(resultList);
        realm.commitTransaction();

        return resultList;
    }

    private static Object generateFieldValue(Realm realm, Field field, int counter) {
        Class<?> type = field.getType();
        Class<? extends RealmObject> clazz;
        Object value = null;
        if (type == String.class) {
            value = field.getName() + " " + counter;
        } else if (type == Boolean.class || type == boolean.class) {
            value = (counter % 2 == 0);
        } else if (type == Short.class || type == short.class) {
            value = Integer.valueOf(counter).shortValue();
        } else if (type == Integer.class || type == int.class) {
            value = counter;
        } else if (type == Long.class || type == long.class) {
            value = Integer.valueOf(counter).longValue();
        } else if (type == Float.class || type == float.class) {
            value = Integer.valueOf(counter).floatValue();
        } else if (type == Double.class || type == double.class) {
            value = Integer.valueOf(counter).doubleValue();
        } else if (type == Date.class) {
            value = new Date(counter * 1000);
        } else if (type == Byte.class || type == byte.class) {
            value = Integer.valueOf(counter).byteValue();
        } else if (type == Byte[].class || type == byte[].class) {
            value = Integer.toString(counter).getBytes();
        } else if (RealmObject.class.isAssignableFrom(type)) {
            RealmResults<?> existing = realm.where((Class<? extends RealmObject>) type).findAll();
            int count = existing.size();
            if (count > 0) {
                value = existing.get(0);
            } else {
                RealmList<RealmObject> generated = generateData(realm, (Class<? extends RealmObject>) type, 1);
                value = (generated.size() > 0 ? generated.get(0) : null);
            }
        } else if (RealmList.class.isAssignableFrom(type)) {
            ParameterizedType pType = (ParameterizedType) field.getGenericType();
            clazz = (Class<? extends RealmObject>) pType.getActualTypeArguments()[0];

            RealmResults<? extends RealmObject> existing = realm.where(clazz).findAll();

            int count = existing.size();
            if (count < DEFAULT_ROW_COUNT) {
                value = generateData(realm, clazz, DEFAULT_ROW_COUNT);
            } else {
                RealmObject item;
                RealmList<RealmObject> list = new RealmList<>();
                list.addAll(existing.subList(0, DEFAULT_ROW_COUNT));
                value = list;
            }
        } else {
            Log.w("GENERATE", "unknown field type");
            value = null;
        }

        return value;
    }

    private static String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
