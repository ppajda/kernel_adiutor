/*
 * Copyright (C) 2015 Willi Ye
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.grarak.kerneladiutor.utils.kernel;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;
import com.kerneladiutor.library.root.RootUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by willi on 02.01.15.
 */
public class Misc implements Constants {

    private static String[] mAvailableTCPCongestions;

    private static String VIBRATION_PATH;
    private static Integer VIBRATION_MAX;
    private static Integer VIBRATION_MIN;

    private static String LOGGER_FILE;

    private static String CRC_FILE;

    public static void setHostname(String value, Context context) {
        Control.setProp(HOSTNAME_KEY, value, context);
    }

    public static String getHostname() {
        return Utils.getProp(HOSTNAME_KEY);
    }

    public static void setTcpCongestion(String tcpCongestion, Context context) {
        Control.runCommand("sysctl -w net.ipv4.tcp_congestion_control=" + tcpCongestion,
            TCP_AVAILABLE_CONGESTIONS, Control.CommandType.CUSTOM, context);
    }

    public static String getCurTcpCongestion() {
        return getTcpAvailableCongestions(false).get(0);
    }

    public static List < String > getTcpAvailableCongestions(boolean sort) {
        if (mAvailableTCPCongestions == null) mAvailableTCPCongestions = new String[0];
        String value = Utils.readFile(TCP_AVAILABLE_CONGESTIONS);
        if (value != null) {
            mAvailableTCPCongestions = value.split(" ");
            if (sort) {
                Collections.sort(Arrays.asList(mAvailableTCPCongestions), String.CASE_INSENSITIVE_ORDER);
            }
        }
        return new ArrayList < > (Arrays.asList(mAvailableTCPCongestions));
    }

    public static String getIpAddr(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        String ipString = String.format(Locale.US,
            "%d.%d.%d.%d",
            (ip & 0xff),
            (ip >> 8 & 0xff),
            (ip >> 16 & 0xff),
            (ip >> 24 & 0xff));

        return ipString;
    }

    public static void activateADBOverWifi(boolean active, Context context) {
        Control.setProp(ADB_OVER_WIFI, active ? "5555" : "-1", context);
    }

    public static boolean isADBOverWifiActive() {
        if (Utils.getProp(ADB_OVER_WIFI).equals("5555")) {
            return true;
        }
        return false;
    }

    public static void setNewPowerSuspend(int value, Context context) {
        Control.runCommand(String.valueOf(value), POWER_SUSPEND_STATE, Control.CommandType.GENERIC, context);
    }

    public static int getNewPowerSuspendState() {
        return Utils.stringToInt(Utils.readFile(POWER_SUSPEND_STATE));
    }

    public static boolean hasNewPowerSuspendState() {
        if (Utils.existFile(POWER_SUSPEND_STATE) && Utils.existFile(POWER_SUSPEND_VERSION)) {
            String version = Utils.readFile(POWER_SUSPEND_VERSION);
            if (version.contains("1.3") || version.contains("1.5")) return true;
        }
        return false;
    }

    public static void activateOldPowerSuspend(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", POWER_SUSPEND_STATE, Control.CommandType.GENERIC, context);
    }

    public static boolean isOldPowerSuspendStateActive() {
        return Utils.readFile(POWER_SUSPEND_STATE).equals("1");
    }

    public static boolean hasOldPowerSuspendState() {
        if (Utils.existFile(POWER_SUSPEND_STATE) && Utils.existFile(POWER_SUSPEND_VERSION))
            if (Utils.readFile(POWER_SUSPEND_VERSION).contains("1.2")) return true;
        return false;
    }

    public static void setPowerSuspendMode(int value, Context context) {
        Control.runCommand(String.valueOf(value), POWER_SUSPEND_MODE, Control.CommandType.GENERIC, context);
    }

    public static int getPowerSuspendMode() {
        return Utils.stringToInt(Utils.readFile(POWER_SUSPEND_MODE));
    }

    public static boolean hasPowerSuspendMode() {
        return Utils.existFile(POWER_SUSPEND_MODE);
    }

    public static boolean hasPowerSuspend() {
        return Utils.existFile(POWER_SUSPEND);
    }

    public static void activateGentleFairSleepers(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", GENTLE_FAIR_SLEEPERS, Control.CommandType.GENERIC, context);
    }

    public static boolean isGentleFairSleepersActive() {
        return Utils.readFile(GENTLE_FAIR_SLEEPERS).equals("1");
    }

    public static boolean hasGentleFairSleepers() {
        return Utils.existFile(GENTLE_FAIR_SLEEPERS);
    }

    public static void activateArchPower(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", ARCH_POWER, Control.CommandType.GENERIC, context);
    }

    public static boolean isArchPowerActive() {
        return Utils.readFile(ARCH_POWER).equals("1");
    }

    public static boolean hasArchPower() {
        return Utils.existFile(ARCH_POWER);
    }

    public static void activateDynamicFsync(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", DYNAMIC_FSYNC, Control.CommandType.GENERIC, context);
    }

    public static boolean isDynamicFsyncActive() {
        return Utils.readFile(DYNAMIC_FSYNC).equals("1");
    }

    public static boolean hasDynamicFsync() {
        return Utils.existFile(DYNAMIC_FSYNC);
    }

    public static void activateFsync(boolean active, Context context) {
        if (Utils.isLetter(Utils.readFile(Utils.getsysfspath(FSYNC_ARRAY)))) {
            Control.runCommand(active ? "Y" : "N", Utils.getsysfspath(FSYNC_ARRAY), Control.CommandType.GENERIC, context);
        } else {
            Control.runCommand(active ? "1" : "0", Utils.getsysfspath(FSYNC_ARRAY), Control.CommandType.GENERIC, context);
        }
    }

    public static boolean isFsyncActive() {
        String path = Utils.getsysfspath(FSYNC_ARRAY);
        return Utils.readFile(path).equals(Utils.isLetter(Utils.readFile(path)) ? "Y" : "1");
    }

    public static boolean hasFsync() {
        return Utils.existFile(Utils.getsysfspath(FSYNC_ARRAY));
    }

    public static void activateCrc(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", CRC_FILE, Control.CommandType.GENERIC, context);
    }

    public static boolean isCrcActive() {
        return Utils.readFile(CRC_FILE).equals("1");
    }

    public static boolean hasCrc() {
        if (CRC_FILE == null)
            for (String file: CRC_ARRAY)
                if (Utils.existFile(file)) {
                    CRC_FILE = file;
                    return true;
                }
        return CRC_FILE != null;
    }

    public static void activateLogger(boolean active, Context context) {
        if (!LOGGER_FILE.equals(LOGD)) {
            Control.runCommand(active ? "1" : "0", LOGGER_FILE, Control.CommandType.GENERIC, context);
        }
        if (LOGGER_FILE.equals(LOGD)) {
            // This is needed because the path changes from "start" to "stop" so it breaks the commandsaver function
            Control.deletespecificcommand(context, active ? "stop" : "start", null);

            Control.runCommand("logd", active ? "start" : "stop", Control.CommandType.SHELL, context);
        }
    }

    public static boolean isLoggerActive() {
        // copy this from mpdecision
        try {
            String result = RootUtils.runCommand("getprop | grep logd]").split("]: ")[1];
            if (result.equals("[running]") || result.equals("[restarting]")) {
                return true;
            }
        } catch (Exception ignored) {
            return false;
        }
        return false;
    }

    public static boolean hasLoggerEnable() {
        if (LOGGER_FILE == null)
            for (String file: LOGGER_ARRAY)
                if (Utils.existFile(file)) {
                    LOGGER_FILE = file;
                    return true;
                }
        return LOGGER_FILE != null;
    }

    public static void setVibration(int value, Context context) {
        String enablePath = VIB_ENABLE;
        boolean enable = Utils.existFile(enablePath);
        if (enable) Control.runCommand("1", enablePath, Control.CommandType.GENERIC, context);
        Control.runCommand(String.valueOf(value), VIBRATION_PATH, Control.CommandType.GENERIC, context);
        if (Utils.existFile(VIB_LIGHT))
            Control.runCommand(String.valueOf(value - 300 < 116 ? 116 : value - 300), VIB_LIGHT,
                Control.CommandType.GENERIC, context);
        if (enable) Control.runCommand("0", enablePath, Control.CommandType.GENERIC, context);
    }

    public static int getVibrationMin() {
        if (VIBRATION_MIN == null) {
            if (VIBRATION_PATH.equals("/sys/class/timed_output/vibrator/vtg_level") &&
                Utils.existFile("/sys/class/timed_output/vibrator/vtg_min")) {
                VIBRATION_MIN = Utils.stringToInt(Utils.readFile("/sys/class/timed_output/vibrator/vtg_min"));
                return VIBRATION_MIN;
            }

            if (VIBRATION_PATH.equals("/sys/class/timed_output/vibrator/pwm_value") &&
                Utils.existFile("/sys/class/timed_output/vibrator/pwm_min")) {
                VIBRATION_MIN = Utils.stringToInt(Utils.readFile("/sys/class/timed_output/vibrator/pwm_min"));
                return VIBRATION_MIN;
            }

            for (int i = 0; i < VIBRATION_ARRAY.length; i++)
                if (VIBRATION_PATH.equals(VIBRATION_ARRAY[i]))
                    VIBRATION_MIN = VIBRATION_MAX_MIN_ARRAY[i][1];
        }
        return VIBRATION_MIN != null ? VIBRATION_MIN : 0;
    }

    public static int getVibrationMax() {
        if (VIBRATION_MAX == null) {
            if (VIBRATION_PATH.equals("/sys/class/timed_output/vibrator/vtg_level") &&
                Utils.existFile("/sys/class/timed_output/vibrator/vtg_max")) {
                VIBRATION_MAX = Utils.stringToInt(Utils.readFile("/sys/class/timed_output/vibrator/vtg_max"));
                return VIBRATION_MAX;
            }

            if (VIBRATION_PATH.equals("/sys/class/timed_output/vibrator/pwm_value") &&
                Utils.existFile("/sys/class/timed_output/vibrator/pwm_max")) {
                VIBRATION_MAX = Utils.stringToInt(Utils.readFile("/sys/class/timed_output/vibrator/pwm_max"));
                return VIBRATION_MAX;
            }

            for (int i = 0; i < VIBRATION_ARRAY.length; i++)
                if (VIBRATION_PATH.equals(VIBRATION_ARRAY[i]))
                    VIBRATION_MAX = VIBRATION_MAX_MIN_ARRAY[i][0];
        }
        return VIBRATION_MAX != null ? VIBRATION_MAX : 0;
    }

    public static int getCurVibration() {
        return Utils.stringToInt(Utils.readFile(VIBRATION_PATH).replaceAll("%", ""));
    }

    public static boolean hasVibration() {
        for (String vibration: VIBRATION_ARRAY)
            if (Utils.existFile(vibration)) {
                VIBRATION_PATH = vibration;
                break;
            }
        return VIBRATION_PATH != null;
    }

    public static boolean isSELinuxActive() {
        String result = RootUtils.runCommand(GETENFORCE);
        if (result.equals("Enforcing")) return true;
        return false;
    }

    public static void activateSELinux(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", SETENFORCE, Control.CommandType.SHELL, context);
    }

    public static String getSELinuxStatus() {
        String result = RootUtils.runCommand(GETENFORCE);
        if (result != null) {
            if (result.equals("Enforcing")) return "Enforcing";
            else if (result.equals("Permissive")) return "Permissive";
        }
        return "Unknown Status";
    }

    public static boolean hasMotoTouchx() {
        return Utils.existFile(MOTO_TOUCHX);
    }

    public static void activateMotoTouchx(boolean active, Context context) {
        Control.runCommand(active ? "1" : "0", MOTO_TOUCHX, Control.CommandType.GENERIC, context);
    }

    public static boolean isMotoTouchxActive() {
        return Utils.readFile(MOTO_TOUCHX).equals("1");
    }
}
