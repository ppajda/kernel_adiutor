/*
 * Copyright (C) 2017 Felipe Leon
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;
import com.kerneladiutor.library.root.RootUtils;

/**
 * Created by bhb27 on 09.14.17.
 */
public class Ram implements Constants {

    private static String[] mAvail_Freq;

    public static boolean hasRamControl() {
        for (String file: RAM_ARRAY)
            if (Utils.existFile(file)) return true;
        return false;
    }

    public static boolean hasRamMaxFreq() {
        if (Utils.existFile(RAM_FREQ_MAX)) return true;
        return false;
    }

    public static String getRamMaxFreq() {
        return Utils.readFile(RAM_FREQ_MAX);
    }

    public static void setRamMaxFreq(String value, Context context) {
        Control.runCommand(value, RAM_FREQ_MAX, Control.CommandType.ASTERISK, context);
    }

    public static boolean hasRamMinFreq() {
        if (Utils.existFile(RAM_FREQ_MIN)) return true;
        return false;
    }

    public static String getRamMinFreq() {
        return Utils.readFile(RAM_FREQ_MIN);
    }

    public static void setRamMinFreq(String value, Context context) {
        Control.runCommand(value, RAM_FREQ_MIN, Control.CommandType.ASTERISK, context);
    }

    public static boolean hasRamPoll() {
        if (Utils.existFile(RAM_POLL)) return true;
        return false;
    }

    public static int getRamPoll() {
        return Utils.stringToInt(Utils.readFile(RAM_POLL));
    }

    public static void setRamPoll(int value, Context context) {
        Control.runCommand(String.valueOf(value), RAM_POLL, Control.CommandType.ASTERISK, context);
    }

    public static boolean hasRamCurFreq() {
        if (Utils.existFile(RAM_CUR_FREQ)) return true;
        return false;
    }

    public static String getRamCurFreq() {
        return Utils.readFile(RAM_CUR_FREQ);
    }

    public static boolean hasRamAvaFreq() {
        if (Utils.existFile(RAM_AVA_FREQ)) return true;
        return false;
    }

    public static List < String > getFreqs() {
        if (mAvail_Freq == null) mAvail_Freq = new String[0];
        String value = Utils.readFile(RAM_AVA_FREQ);
        if (value != null) {
            mAvail_Freq = value.split(" ");
        }
        return new ArrayList < > (Arrays.asList(mAvail_Freq));
    }

    public static List < String > devFreqs() {
        return new ArrayList < > (Arrays.asList(RAM_G3_AVA_FREQ));
    }

    public static boolean device(String device, String device2, String device3, String device4, String device5, String device6, String device7) {
        return ((RootUtils.runCommand("getprop ro.product.device").contains(device)) ||
            (RootUtils.runCommand("getprop ro.product.device").contains(device2)) ||
            (RootUtils.runCommand("getprop ro.product.device").contains(device3)) ||
            (RootUtils.runCommand("getprop ro.product.device").contains(device4)) ||
            (RootUtils.runCommand("getprop ro.product.device").contains(device5)) ||
            (RootUtils.runCommand("getprop ro.product.device").contains(device6)) ||
            (RootUtils.runCommand("getprop ro.product.device").contains(device7)));
    }
}
