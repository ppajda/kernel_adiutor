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
package com.grarak.kerneladiutor.fragments.kernel;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DAdapter;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.elements.cards.SeekBarCardView;
import com.grarak.kerneladiutor.elements.cards.SwitchCardView;
import com.grarak.kerneladiutor.fragments.PathReaderFragment;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.fragments.ViewPagerFragment;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.kernel.CPU;
import com.grarak.kerneladiutor.utils.root.Control;
import com.kerneladiutor.library.root.RootFile;

import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by willi on 07.04.15.
 */
public class CPUFragment extends ViewPagerFragment implements Constants {

    private static WeakReference < CPUFragment > cpuFragment;
    private CPUPart cpuPart;
    private GovernorPart governorPart;
    private int core;

    @Override
    public void preInit(Bundle savedInstanceState) {
        super.preInit(savedInstanceState);
        showTabs(false);
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);
        cpuFragment = new WeakReference < CPUFragment > (this);

        allowSwipe(false);
        addFragment(new ViewPagerItem(cpuPart == null ? cpuPart = new CPUPart() : cpuPart, null));
        addFragment(new ViewPagerItem(governorPart == null ? governorPart = new GovernorPart() : governorPart, null));
    }

    @Override
    public void onSwipe(int page) {
        super.onSwipe(page);
        allowSwipe(page == 1);
    }

    @Override
    public boolean onBackPressed() {
        if (getCurrentPage() == 1) {
            setCurrentItem(0);
            return true;
        }
        return false;
    }

    public static class CPUPart extends RecyclerViewFragment implements View.OnClickListener,
        PopupCardView.DPopupCard.OnDPopupCardListener, CardViewItem.DCardView.OnDCardListener,
        SeekBarCardView.DSeekBarCard.OnDSeekBarCardListener,
        SwitchCardView.DSwitchCard.OnDSwitchCardListener {

            private CardViewItem.DCardView mTempCard;

            private AppCompatCheckBox[] mCoreCheckBox;
            private ProgressBar[] mCoreProgressBar;
            private AppCompatTextView[] mCoreUsageText;
            private AppCompatTextView[] mCoreFreqText;

            private PopupCardView.DPopupCard mMaxFreqCard, mMinFreqCard, mMaxScreenOffFreqCard;

            private PopupCardView.DPopupCard mGovernorCard;
            private CardViewItem.DCardView mGovernorTunableCard;

            private AppCompatCheckBox[] mCoreCheckBoxLITTLE;
            private ProgressBar[] mCoreProgressBarLITTLE;
            private AppCompatTextView[] mCoreUsageTextLITTLE;
            private AppCompatTextView[] mCoreFreqTextLITTLE;

            private PopupCardView.DPopupCard mMaxFreqLITTLECard, mMinFreqLITTLECard, mMaxScreenOffFreqLITTLECard;

            private PopupCardView.DPopupCard mGovernorLITTLECard;
            private CardViewItem.DCardView mGovernorTunableLITTLECard;

            private PopupCardView.DPopupCard mMcPowerSavingCard;

            private SwitchCardView.DSwitchCard mPowerSavingWqCard;

            private PopupCardView.DPopupCard mCFSSchedulerCard;

            private SwitchCardView.DSwitchCard mCpuQuietEnableCard;
            private PopupCardView.DPopupCard mCpuQuietGovernorCard;

            private SwitchCardView.DSwitchCard mCpuBoostEnableCard;
            private SwitchCardView.DSwitchCard mCpuBoostDebugMaskCard;
            private SeekBarCardView.DSeekBarCard mCpuBoostMsCard;
            private SeekBarCardView.DSeekBarCard mStateDeferCard;
            private PopupCardView.DPopupCard mCpuBoostSyncThresholdCard;
            private SeekBarCardView.DSeekBarCard mCpuBoostInputMsCard;
            private PopupCardView.DPopupCard[] mCpuBoostInputFreqCard;
            private SwitchCardView.DSwitchCard mCpuBoostWakeupCard;
            private SwitchCardView.DSwitchCard mCpuBoostHotplugCard;

            private SwitchCardView.DSwitchCard mCpuTouchBoostCard;
            private SwitchCardView.DSwitchCard mStateNotifierStateCard, mStateDebugCard;

            @Override
            public String getClassName() {
                return CPUFragment.class.getSimpleName();
            }

            @Override
            public void init(Bundle savedInstanceState) {
                super.init(savedInstanceState);

                if (CPU.hasTemp()) tempInit();
                if (CPU.getFreqs() != null) {
                    if (CPU.isBigLITTLE()) {
                        DDivider bigDivider = new DDivider();
                        bigDivider.setText(getString(R.string.big).toLowerCase(Locale.getDefault()));
                        addView(bigDivider);
                    }
                    coreInit();
                    freqInit();
                }
                if (CPU.getAvailableGovernors() != null) governorInit();
                DDivider othersDivider = null;
                if (CPU.isBigLITTLE()) {
                    DDivider LITTLEDivider = new DDivider();
                    LITTLEDivider.setText(getString(R.string.little).toUpperCase(Locale.getDefault()));
                    addView(LITTLEDivider);

                    if (CPU.getFreqs(CPU.getLITTLEcore()) != null) {
                        coreLITTLEInit();
                        freqLITTLEInit();
                    }
                    if (CPU.getAvailableGovernors(CPU.getLITTLEcore()) != null) governorLITTLEInit();

                    othersDivider = new DDivider();
                    othersDivider.setText(getString(R.string.other));
                    addView(othersDivider);
                }
                int count = getCount();
                if (CPU.hasStateNotifier()) statenotifierInit();
                if (CPU.hasMcPowerSaving()) mcPowerSavingInit();
                if (CPU.hasPowerSavingWq() || CPU.hasCFSScheduler()) ExtraFlagDividerInit();
                if (CPU.hasPowerSavingWq()) powerSavingWqInit();
                if (CPU.hasCFSScheduler()) cfsSchedulerInit();
                if (CPU.hasCpuQuiet()) cpuQuietInit();
                if (CPU.hasCpuBoost()) cpuBoostInit();
                if (CPU.hasCpuTouchBoost()) cpuTouchBoostInit();
                if (othersDivider != null && (count == getCount() || getView(count) instanceof DDivider))
                    removeView(othersDivider);
                Update();
            }

            private void tempInit() {
                mTempCard = new CardViewItem.DCardView();
                mTempCard.setTitle(getString(R.string.cpu_temp));
                mTempCard.setDescription(CPU.getTemp());

                addView(mTempCard);
            }

            private void coreInit() {
                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);

                mCoreCheckBox = new AppCompatCheckBox[CPU.getBigCoreRange().size()];
                mCoreProgressBar = new ProgressBar[mCoreCheckBox.length];
                mCoreUsageText = new AppCompatTextView[mCoreCheckBox.length];
                mCoreFreqText = new AppCompatTextView[mCoreCheckBox.length];
                for (int i = 0; i < mCoreCheckBox.length; i++) {
                    View view = inflater.inflate(R.layout.coreview, container, false);

                    mCoreCheckBox[i] = (AppCompatCheckBox) view.findViewById(R.id.core_checkbox);
                    mCoreCheckBox[i].setText(getString(R.string.core, i + 1));
                    mCoreCheckBox[i].setOnClickListener(this);

                    mCoreProgressBar[i] = (ProgressBar) view.findViewById(R.id.progressbar);
                    mCoreProgressBar[i].setMax(CPU.getFreqs().size());

                    mCoreUsageText[i] = (AppCompatTextView) view.findViewById(R.id.usage);

                    mCoreFreqText[i] = (AppCompatTextView) view.findViewById(R.id.freq);

                    layout.addView(view);
                }

                CardViewItem.DCardView coreCard = new CardViewItem.DCardView();
                coreCard.setTitle(getString(R.string.current_freq));
                coreCard.setView(layout);

                addView(coreCard);
            }

            private void freqInit() {

                List < String > freqs = new ArrayList < > ();
                for (int freq: CPU.getFreqs())
                    freqs.add(freq / 1000 + getString(R.string.mhz));

                    mMaxFreqCard = new PopupCardView.DPopupCard(freqs);
                    mMaxFreqCard.setTitle(getString(R.string.cpu_max_freq));
                    mMaxFreqCard.setDescription(getString(R.string.cpu_max_freq_summary));
                    mMaxFreqCard.setItem(CPU.getMaxFreq(true) / 1000 + getString(R.string.mhz));
                    mMaxFreqCard.setOnDPopupCardListener(this);

                    mMinFreqCard = new PopupCardView.DPopupCard(freqs);
                    mMinFreqCard.setTitle(getString(R.string.cpu_min_freq));
                    mMinFreqCard.setDescription(getString(R.string.cpu_min_freq_summary));
                    mMinFreqCard.setItem(CPU.getMinFreq(true) / 1000 + getString(R.string.mhz));
                    mMinFreqCard.setOnDPopupCardListener(this);

                    addView(mMaxFreqCard);
                    addView(mMinFreqCard);

                    if (CPU.hasMaxScreenOffFreq()) {
                        mMaxScreenOffFreqCard = new PopupCardView.DPopupCard(freqs);
                        mMaxScreenOffFreqCard.setTitle(getString(R.string.cpu_max_screen_off_freq));
                        mMaxScreenOffFreqCard.setDescription(getString(R.string.cpu_max_screen_off_freq_summary));
                        mMaxScreenOffFreqCard.setItem(CPU.getMaxScreenOffFreq(true) / 1000 + getString(R.string.mhz));
                        mMaxScreenOffFreqCard.setOnDPopupCardListener(this);

                        addView(mMaxScreenOffFreqCard);
                    }
            }

            private void governorInit() {

                    mGovernorCard = new PopupCardView.DPopupCard(CPU.getAvailableGovernors());
                    mGovernorCard.setTitle(getString(R.string.cpu_governor));
                    mGovernorCard.setDescription(getString(R.string.cpu_governor_summary));
                    mGovernorCard.setItem(CPU.getCurGovernor(true));
                    mGovernorCard.setOnDPopupCardListener(this);

                    mGovernorTunableCard = new CardViewItem.DCardView();
                    mGovernorTunableCard.setTitle(getString(R.string.cpu_governor_tunables));
                    mGovernorTunableCard.setDescription(getString(R.string.cpu_governor_tunables_summary));
                    mGovernorTunableCard.setOnDCardListener(this);

                    addView(mGovernorCard);
                    addView(mGovernorTunableCard);
            }

            private void coreLITTLEInit() {
                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.VERTICAL);

                mCoreCheckBoxLITTLE = new AppCompatCheckBox[CPU.getLITTLECoreRange().size()];
                mCoreProgressBarLITTLE = new ProgressBar[mCoreCheckBoxLITTLE.length];
                mCoreUsageTextLITTLE = new AppCompatTextView[mCoreCheckBoxLITTLE.length];
                mCoreFreqTextLITTLE = new AppCompatTextView[mCoreCheckBoxLITTLE.length];
                for (int i = 0; i < mCoreCheckBoxLITTLE.length; i++) {
                    View view = inflater.inflate(R.layout.coreview, container, false);

                    mCoreCheckBoxLITTLE[i] = (AppCompatCheckBox) view.findViewById(R.id.core_checkbox);
                    mCoreCheckBoxLITTLE[i].setText(getString(R.string.core, i + 1));
                    mCoreCheckBoxLITTLE[i].setOnClickListener(this);

                    mCoreProgressBarLITTLE[i] = (ProgressBar) view.findViewById(R.id.progressbar);
                    mCoreProgressBarLITTLE[i].setMax(CPU.getFreqs(CPU.getLITTLEcore()).size());

                    mCoreUsageTextLITTLE[i] = (AppCompatTextView) view.findViewById(R.id.usage);

                    mCoreFreqTextLITTLE[i] = (AppCompatTextView) view.findViewById(R.id.freq);

                    layout.addView(view);
                }

                CardViewItem.DCardView coreCard = new CardViewItem.DCardView();
                coreCard.setTitle(getString(R.string.current_freq));
                coreCard.setView(layout);

                addView(coreCard);
            }

            private void freqLITTLEInit() {
                List < String > freqs = new ArrayList < > ();
                for (int freq: CPU.getFreqs(CPU.getLITTLEcore()))
                    freqs.add(freq / 1000 + getString(R.string.mhz));

                mMaxFreqLITTLECard = new PopupCardView.DPopupCard(freqs);
                mMaxFreqLITTLECard.setDescription(getString(R.string.cpu_max_freq));
                mMaxFreqLITTLECard.setItem(CPU.getMaxFreq(CPU.getLITTLEcore(), true) / 1000 + getString(R.string.mhz));
                mMaxFreqLITTLECard.setOnDPopupCardListener(this);

                mMinFreqLITTLECard = new PopupCardView.DPopupCard(freqs);
                mMinFreqLITTLECard.setDescription(getString(R.string.cpu_min_freq));
                mMinFreqLITTLECard.setItem(CPU.getMinFreq(CPU.getLITTLEcore(), true) / 1000 + getString(R.string.mhz));
                mMinFreqLITTLECard.setOnDPopupCardListener(this);

                addView(mMaxFreqLITTLECard);
                addView(mMinFreqLITTLECard);

                if (CPU.hasMaxScreenOffFreq()) {
                    mMaxScreenOffFreqLITTLECard = new PopupCardView.DPopupCard(freqs);
                    mMaxScreenOffFreqLITTLECard.setDescription(getString(R.string.cpu_max_screen_off_freq));
                    mMaxScreenOffFreqLITTLECard.setItem(CPU.getMaxScreenOffFreq(CPU.getLITTLEcore(), true) / 1000 +
                        getString(R.string.mhz));
                    mMaxScreenOffFreqLITTLECard.setOnDPopupCardListener(this);

                    addView(mMaxScreenOffFreqLITTLECard);
                }
            }

            private void governorLITTLEInit() {
                mGovernorLITTLECard = new PopupCardView.DPopupCard(CPU.getAvailableGovernors(CPU.getLITTLEcore()));
                mGovernorLITTLECard.setDescription(getString(R.string.cpu_governor));
                mGovernorLITTLECard.setItem(CPU.getCurGovernor(CPU.getLITTLEcore(), true));
                mGovernorLITTLECard.setOnDPopupCardListener(this);

                mGovernorTunableLITTLECard = new CardViewItem.DCardView();
                mGovernorTunableLITTLECard.setDescription(getString(R.string.cpu_governor_tunables));
                mGovernorTunableLITTLECard.setOnDCardListener(this);

                addView(mGovernorLITTLECard);
                addView(mGovernorTunableLITTLECard);
            }

            private void ExtraFlagDividerInit() {
                DDivider mExtraFlagDividerDividerCard = new DDivider();
                mExtraFlagDividerDividerCard.setText(getString(R.string.cpu_extra));
                addView(mExtraFlagDividerDividerCard);
            }

            private void mcPowerSavingInit() {
                mMcPowerSavingCard = new PopupCardView.DPopupCard(new ArrayList<>(Arrays.asList(
                    CPU.getMcPowerSavingItems(getActivity()))));
                mMcPowerSavingCard.setTitle(getString(R.string.mc_power_saving));
                mMcPowerSavingCard.setDescription(getString(R.string.mc_power_saving_summary));
                mMcPowerSavingCard.setItem(CPU.getCurMcPowerSaving());
                mMcPowerSavingCard.setOnDPopupCardListener(this);

                addView(mMcPowerSavingCard);
            }

            private void powerSavingWqInit() {
                mPowerSavingWqCard = new SwitchCardView.DSwitchCard();
                mPowerSavingWqCard.setTitle(getString(R.string.power_saving_wq));
                mPowerSavingWqCard.setDescription(getString(R.string.power_saving_wq_summary));
                mPowerSavingWqCard.setChecked(CPU.isPowerSavingWqActive());
                mPowerSavingWqCard.setOnDSwitchCardListener(this);

                addView(mPowerSavingWqCard);
            }

            private void cfsSchedulerInit() {
                mCFSSchedulerCard = new PopupCardView.DPopupCard(CPU.getAvailableCFSSchedulers());
                mCFSSchedulerCard.setTitle(getString(R.string.cfs_scheduler_policy));
                mCFSSchedulerCard.setDescription(getString(R.string.cfs_scheduler_policy_summary));
                mCFSSchedulerCard.setItem(CPU.getCurrentCFSScheduler());
                mCFSSchedulerCard.setOnDPopupCardListener(this);

                addView(mCFSSchedulerCard);
            }

            private void statenotifierInit() {

                DDivider mNotifierStateDividerCard = new DDivider();
                mNotifierStateDividerCard.setText(getString(R.string.state_notifier));
                mNotifierStateDividerCard.setDescription(getString(R.string.state_notifier_summary));
                addView(mNotifierStateDividerCard);

                mStateNotifierStateCard = new SwitchCardView.DSwitchCard();
                mStateNotifierStateCard.setTitle(getString(R.string.state_notifier_mode));
                mStateNotifierStateCard.setDescription(getString(CPU.isStateNotifierStateActive() ? R.string.state_notifier_mode_summary_enabled : R.string.state_notifier_mode_summary_disabled));
                mStateNotifierStateCard.setChecked(CPU.isStateNotifierStateActive());
                mStateNotifierStateCard.setOnDSwitchCardListener(this);

                addView(mStateNotifierStateCard);

                if (CPU.isStateNotifierStateActive()) {
                    if (CPU.hasStateDefer()) {
                        List < String > list = new ArrayList < > ();
                        for (int i = 0; i < 51; i += 1)
                            list.add(i + getString(R.string.sec));

                        mStateDeferCard = new SeekBarCardView.DSeekBarCard(list);
                        mStateDeferCard.setTitle(getString(R.string.state_defer));
                        mStateDeferCard.setDescription(getString(R.string.state_defer_summary));
                        mStateDeferCard.setProgress(CPU.getStateDefer());
                        mStateDeferCard.setOnDSeekBarCardListener(this);

                        addView(mStateDeferCard);
                    }
                }
            }

            private void cpuQuietInit() {
                if (CPU.hasCpuQuietEnable()) {
                    mCpuQuietEnableCard = new SwitchCardView.DSwitchCard();
                    mCpuQuietEnableCard.setTitle(getString(R.string.cpu_quiet));
                    mCpuQuietEnableCard.setDescription(getString(R.string.cpu_quiet_summary));
                    mCpuQuietEnableCard.setChecked(CPU.isCpuQuietActive());
                    mCpuQuietEnableCard.setOnDSwitchCardListener(this);

                    addView(mCpuQuietEnableCard);
                }

                if (CPU.hasCpuQuietGovernors()) {
                    mCpuQuietGovernorCard = new PopupCardView.DPopupCard(CPU.getCpuQuietAvailableGovernors());
                    mCpuQuietGovernorCard.setDescription(getString(R.string.cpu_quiet_governor));
                    mCpuQuietGovernorCard.setItem(CPU.getCpuQuietCurGovernor());
                    mCpuQuietGovernorCard.setOnDPopupCardListener(this);

                    addView(mCpuQuietGovernorCard);
                }
            }

            private void cpuBoostInit() {
                List < DAdapter.DView > views = new ArrayList < > ();

                if (CPU.hasCpuBoostEnable()) {
                    mCpuBoostEnableCard = new SwitchCardView.DSwitchCard();
                    mCpuBoostEnableCard.setDescription(getString(R.string.cpu_boost));
                    mCpuBoostEnableCard.setChecked(CPU.isCpuBoostActive());
                    mCpuBoostEnableCard.setOnDSwitchCardListener(this);

                    views.add(mCpuBoostEnableCard);
                }

                if (CPU.hasCpuBoostDebugMask()) {
                    mCpuBoostDebugMaskCard = new SwitchCardView.DSwitchCard();
                    mCpuBoostDebugMaskCard.setTitle(getString(R.string.debug_mask));
                    mCpuBoostDebugMaskCard.setDescription(getString(R.string.debug_mask_summary));
                    mCpuBoostDebugMaskCard.setChecked(CPU.isCpuBoostDebugMaskActive());
                    mCpuBoostDebugMaskCard.setOnDSwitchCardListener(this);

                    views.add(mCpuBoostDebugMaskCard);
                }

                if (CPU.hasCpuBoostMs()) {
                    List < String > list = new ArrayList < > ();
                    for (int i = 0; i < 5001; i += 10)
                        list.add(i + getString(R.string.ms));

                    mCpuBoostMsCard = new SeekBarCardView.DSeekBarCard(list);
                    mCpuBoostMsCard.setTitle(getString(R.string.interval));
                    mCpuBoostMsCard.setDescription(getString(R.string.interval_summary));
                    mCpuBoostMsCard.setProgress(CPU.getCpuBootMs() / 10);
                    mCpuBoostMsCard.setOnDSeekBarCardListener(this);

                    views.add(mCpuBoostMsCard);
                }

                if (CPU.hasCpuBoostSyncThreshold() && CPU.getFreqs() != null) {
                    List < String > list = new ArrayList < > ();
                    list.add(getString(R.string.disabled));
                    for (int freq: CPU.getFreqs())
                        list.add((freq / 1000) + getString(R.string.mhz));

                    mCpuBoostSyncThresholdCard = new PopupCardView.DPopupCard(list);
                    mCpuBoostSyncThresholdCard.setTitle(getString(R.string.sync_threshold));
                    mCpuBoostSyncThresholdCard.setDescription(getString(R.string.sync_threshold_summary));
                    mCpuBoostSyncThresholdCard.setItem(CPU.getCpuBootSyncThreshold());
                    mCpuBoostSyncThresholdCard.setOnDPopupCardListener(this);

                    views.add(mCpuBoostSyncThresholdCard);
                }

                if (CPU.hasCpuBoostInputMs()) {
                    List < String > list = new ArrayList < > ();
                    for (int i = 0; i < 5001; i += 10)
                        list.add(i + getString(R.string.ms));

                    mCpuBoostInputMsCard = new SeekBarCardView.DSeekBarCard(list);
                    mCpuBoostInputMsCard.setTitle(getString(R.string.input_interval));
                    mCpuBoostInputMsCard.setDescription(getString(R.string.input_interval_summary));
                    mCpuBoostInputMsCard.setProgress(CPU.getCpuBootInputMs() / 10);
                    mCpuBoostInputMsCard.setOnDSeekBarCardListener(this);

                    views.add(mCpuBoostInputMsCard);
                }

                if (CPU.hasCpuBoostInputFreq() && CPU.getFreqs() != null) {
                    List < String > list = new ArrayList < > ();
                    list.add(getString(R.string.disabled));
                    for (int freq: CPU.getFreqs())
                        list.add((freq / 1000) + getString(R.string.mhz));

                    List < Integer > freqs = CPU.getCpuBootInputFreq();
                    mCpuBoostInputFreqCard = new PopupCardView.DPopupCard[freqs.size()];

                    for (int i = 0; i < freqs.size(); i++) {
                        mCpuBoostInputFreqCard[i] = new PopupCardView.DPopupCard(list);
                        if (i == 0) {
                            if (freqs.size() > 1)
                                mCpuBoostInputFreqCard[i].setTitle(getString(R.string.input_boost_freq_core, i + 1));
                            else
                                mCpuBoostInputFreqCard[i].setTitle(getString(R.string.input_boost_freq));
                            mCpuBoostInputFreqCard[i].setDescription(getString(R.string.input_boost_freq_summary));
                        } else {
                            mCpuBoostInputFreqCard[i].setDescription(getString(R.string.input_boost_freq_core, i + 1));
                        }
                        mCpuBoostInputFreqCard[i].setItem(freqs.get(i));
                        mCpuBoostInputFreqCard[i].setOnDPopupCardListener(this);

                        views.add(mCpuBoostInputFreqCard[i]);
                    }
                }

                if (CPU.hasCpuBoostWakeup()) {
                    mCpuBoostWakeupCard = new SwitchCardView.DSwitchCard();
                    mCpuBoostWakeupCard.setTitle(getString(R.string.wakeup_boost));
                    mCpuBoostWakeupCard.setDescription(getString(R.string.wakeup_boost_summary));
                    mCpuBoostWakeupCard.setChecked(CPU.isCpuBoostWakeupActive());
                    mCpuBoostWakeupCard.setOnDSwitchCardListener(this);

                    views.add(mCpuBoostWakeupCard);
                }

                if (CPU.hasCpuBoostHotplug()) {
                    mCpuBoostHotplugCard = new SwitchCardView.DSwitchCard();
                    mCpuBoostHotplugCard.setTitle(getString(R.string.hotplug_boost));
                    mCpuBoostHotplugCard.setDescription(getString(R.string.hotplug_boost_summary));
                    mCpuBoostHotplugCard.setChecked(CPU.isCpuBoostHotplugActive());
                    mCpuBoostHotplugCard.setOnDSwitchCardListener(this);

                    views.add(mCpuBoostHotplugCard);
                }

                if (views.size() > 0) {
                    DDivider mCpuBoostDividerCard = new DDivider();
                    mCpuBoostDividerCard.setText(getString(R.string.cpu_boost));
                    addView(mCpuBoostDividerCard);

                    addAllViews(views);
                }

            }

            private void cpuTouchBoostInit() {
                mCpuTouchBoostCard = new SwitchCardView.DSwitchCard();
                mCpuTouchBoostCard.setTitle(getString(R.string.touch_boost));
                mCpuTouchBoostCard.setDescription(getString(R.string.touch_boost_summary));
                mCpuTouchBoostCard.setChecked(CPU.isCpuTouchBoostEnabled());
                mCpuTouchBoostCard.setOnDSwitchCardListener(this);
                addView(mCpuTouchBoostCard);
            }

            @Override
            public void onClick(View v) {
                for (int i = 0; i < mCoreCheckBox.length; i++)
                    if (v == mCoreCheckBox[i]) {
                        List < Integer > range = CPU.getBigCoreRange();
                        if (range.get(i) == 0) {
                            mCoreCheckBox[i].setChecked(true);
                            return;
                        }
                        CPU.activateCore(range.get(i), ((CheckBox) v).isChecked(), getActivity());
                        return;
                    }
                if (mCoreCheckBoxLITTLE != null)
                    for (int i = 0; i < mCoreCheckBoxLITTLE.length; i++)
                        if (v == mCoreCheckBoxLITTLE[i]) {
                            List < Integer > range = CPU.getLITTLECoreRange();
                            if (range.get(i) == 0) {
                                mCoreCheckBoxLITTLE[i].setChecked(true);
                                return;
                            }
                            CPU.activateCore(range.get(i), ((CheckBox) v).isChecked(), getActivity());
                            return;
                        }
            }

            @Override
            public void onItemSelected(PopupCardView.DPopupCard dPopupCard, int position) {
                if (dPopupCard == mMaxFreqCard)
                    SetFreq(CPU.getFreqs().get(position), 10, false, getActivity());
                else if (dPopupCard == mMinFreqCard)
                    SetFreq(CPU.getFreqs().get(position), 10, true, getActivity());
                else if (dPopupCard == mMaxScreenOffFreqCard)
                    CPU.setMaxScreenOffFreq(CPU.getFreqs().get(position), getActivity());
                else if (dPopupCard == mGovernorCard) {
                    String cores = null;
                    for (int i = 0; i < CPU.getCoreCount(); i++) {
                        if (!CPU.setGovernorPC(CPU.getAvailableGovernors().get(position), i, getActivity())) {
                            if (cores == null) cores = "" + i;
                            else cores += "|" + i;
                        }
                    }
                    if (cores != null)
                        Utils.toast(String.format(getString(R.string.cpu_set_governor_erro), cores), getActivity(), Toast.LENGTH_LONG);
                } else if (dPopupCard == mMaxFreqLITTLECard)
                    CPU.setMaxFreq(Control.CommandType.CPU_LITTLE, CPU.getFreqs(CPU.getLITTLEcore()).get(position), getActivity());
                else if (dPopupCard == mMinFreqLITTLECard)
                    CPU.setMinFreq(Control.CommandType.CPU_LITTLE, CPU.getFreqs(CPU.getLITTLEcore()).get(position), getActivity());
                else if (dPopupCard == mMaxScreenOffFreqLITTLECard)
                    CPU.setMaxScreenOffFreq(Control.CommandType.CPU_LITTLE, CPU.getFreqs(CPU.getLITTLEcore()).get(position),
                        getActivity());
                else if (dPopupCard == mGovernorLITTLECard)
                    CPU.setGovernorBig(Control.CommandType.CPU_LITTLE, CPU.getAvailableGovernors(CPU.getLITTLEcore()).get(position),
                        getActivity());
                else if (dPopupCard == mMcPowerSavingCard)
                    CPU.setMcPowerSaving(position, getActivity());
                else if (dPopupCard == mCFSSchedulerCard)
                    CPU.setCFSScheduler(CPU.getAvailableCFSSchedulers().get(position), getActivity());
                else if (dPopupCard == mCpuQuietGovernorCard)
                    CPU.setCpuQuietGovernor(CPU.getCpuQuietAvailableGovernors().get(position), getActivity());
                else if (dPopupCard == mCpuBoostSyncThresholdCard)
                    CPU.setCpuBoostSyncThreshold(position == 0 ? 0 : CPU.getFreqs().get(position - 1), getActivity());
                else {
                    if (mCpuBoostInputFreqCard != null)
                        for (int i = 0; i < mCpuBoostInputFreqCard.length; i++)
                            if (dPopupCard == mCpuBoostInputFreqCard[i]) {
                                CPU.setCpuBoostInputFreq(position == 0 ? 0 : CPU.getFreqs().get(position - 1), i, getActivity());
                                return;
                            }
                }
            }

            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                    if (dCardView == mGovernorTunableCard) {
                        cpuFragment.get().core = CPU.getBigCore();
                        cpuFragment.get().governorPart.reload();
                        cpuFragment.get().setCurrentItem(1);
                    } else if (dCardView == mGovernorTunableLITTLECard) {
                        cpuFragment.get().core = CPU.getLITTLEcore();
                        cpuFragment.get().governorPart.reload();
                        cpuFragment.get().setCurrentItem(1);
                    }

            }

            @Override
            public void onChanged(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {}

            @Override
            public void onStop(SeekBarCardView.DSeekBarCard dSeekBarCard, int position) {
                if (dSeekBarCard == mCpuBoostMsCard)
                    CPU.setCpuBoostMs(position * 10, getActivity());
                else if (dSeekBarCard == mCpuBoostInputMsCard)
                    CPU.setCpuBoostInputMs(position * 10, getActivity());
                else if (dSeekBarCard == mStateDeferCard)
                    CPU.setStateDefer(position, getActivity());
            }

            @Override
            public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                if (dSwitchCard == mCpuQuietEnableCard)
                    CPU.activateCpuQuiet(checked, getActivity());
                else if (dSwitchCard == mCpuBoostEnableCard)
                    CPU.activateCpuBoost(checked, getActivity());
                else if (dSwitchCard == mCpuBoostDebugMaskCard)
                    CPU.activateCpuBoostDebugMask(checked, getActivity());
                else if (dSwitchCard == mPowerSavingWqCard)
                    CPU.activatePowerSavingWq(checked, getActivity());
                else if (dSwitchCard == mStateNotifierStateCard) {
                    CPU.activateStateNotifier(checked, getActivity());
                    mStateNotifierStateCard.setDescription(getString(checked ? R.string.state_notifier_mode_summary_enabled : R.string.state_notifier_mode_summary_disabled));
                    ForceRefresh();
                } else if (dSwitchCard == mCpuBoostWakeupCard)
                    CPU.activateCpuBoostWakeup(checked, getActivity());
                else if (dSwitchCard == mCpuBoostHotplugCard)
                    CPU.activateCpuBoostHotplug(checked, getActivity());
                else if (dSwitchCard == mCpuTouchBoostCard)
                    CPU.activateCpuTouchBoost(checked, getActivity());
	    }
            private void ForceRefresh() {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                CPUFragment.cpuFragment.get().cpuPart.view.invalidate();
                getActivity().getSupportFragmentManager().beginTransaction().detach(CPUFragment.cpuFragment.get()).attach(CPUFragment.cpuFragment.get()).commit();
            }

            private void SetFreq(int freq, int core, boolean min, Context context) {
                // sleep for input boots duration to avoid false negative of the error toast
                if (CPU.hasCpuBoostInputMs()) {
                    try {
                        Thread.sleep(CPU.getCpuBootInputMs());
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (min) {
                    if (core == 10) {
                        String cores = null;
                        for (int i = 0; i < CPU.getCoreCount(); i++) {
                            if (freq >= CPU.getMaxFreq(i, true))
                                Utils.toast(String.format(getString(R.string.cpu_set_minfreq_erro_min_max), i), context, Toast.LENGTH_LONG);
                            else if (!CPU.setPCMinFreq(freq, i, context)) {
                                if (cores == null) cores = "" + i;
                                else cores += "|" + i;
                            }
                        }
                        if (cores != null)
                            Utils.toast(String.format(getString(R.string.cpu_set_minfreq_erro), cores), getActivity(), Toast.LENGTH_LONG);
                    } else if (freq >= CPU.getMaxFreq(core, true))
                        Utils.toast(String.format(getString(R.string.cpu_set_minfreq_erro_min_max), core), context, Toast.LENGTH_LONG);
                    else if (!CPU.setPCMinFreq(freq, core, context))
                        Utils.toast(String.format(getString(R.string.cpu_set_minfreq_erro), String.valueOf(core)), context, Toast.LENGTH_LONG);
                } else {
                    if (core == 10) {
                        String cores = null;
                        for (int i = 0; i < CPU.getCoreCount(); i++) {
                            if (freq <= CPU.getMinFreq(i, true))
                                Utils.toast(String.format(getString(R.string.cpu_set_maxfreq_erro_max_min), i), context, Toast.LENGTH_LONG);
                            else if (!CPU.setPCMaxFreq(freq, i, context)) {
                                if (cores == null) cores = "" + i;
                                else cores += "|" + i;
                            }
                        }
                        if (cores != null)
                            Utils.toast(String.format(getString(R.string.cpu_set_maxfreq_erro), cores), getActivity(), Toast.LENGTH_LONG);
                    } else if (freq <= CPU.getMinFreq(core, true))
                        Utils.toast(String.format(getString(R.string.cpu_set_maxfreq_erro_max_min), core), context, Toast.LENGTH_LONG);
                    else if (!CPU.setPCMaxFreq(freq, core, context))
                        Utils.toast(String.format(getString(R.string.cpu_set_maxfreq_erro), String.valueOf(core)), context, Toast.LENGTH_LONG);
                }
            }

            @Override
            public boolean onRefresh() {
                Update();
                return true;
            }
            public void Update() {

                if (mTempCard != null) mTempCard.setDescription(CPU.getTemp());

                if (mCoreCheckBox != null && mCoreProgressBar != null && mCoreFreqText != null) {
                    List < Integer > range = CPU.getBigCoreRange();
                    for (int i = 0; i < mCoreCheckBox.length; i++) {
                        int cur = CPU.getCurFreq(range.get(i));
                        if (mCoreCheckBox[i] != null) mCoreCheckBox[i].setChecked(cur != 0);
                        if (mCoreProgressBar[i] != null)
                            mCoreProgressBar[i].setProgress(CPU.getFreqs().indexOf(cur) + 1);
                        if (mCoreFreqText[i] != null)
                            mCoreFreqText[i].setText(cur == 0 ? getString(R.string.offline) : cur / 1000 +
                                getString(R.string.mhz));
                    }
                }

                    if (mMaxFreqCard != null) {
                        int maxFreq = CPU.getMaxFreq(false);
                        if (maxFreq != 0) mMaxFreqCard.setItem(maxFreq / 1000 + getString(R.string.mhz));
                    }
                    if (mMinFreqCard != null) {
                        int minFreq = CPU.getMinFreq(false);
                        if (minFreq != 0) mMinFreqCard.setItem(minFreq / 1000 + getString(R.string.mhz));
                    }

                    if (mGovernorCard != null) {
                        String governor = CPU.getCurGovernor(false);
                        if (!governor.isEmpty()) mGovernorCard.setItem(governor);
                    }

                if (mCoreCheckBoxLITTLE != null && mCoreProgressBarLITTLE != null && mCoreFreqTextLITTLE != null) {
                    List < Integer > range = CPU.getLITTLECoreRange();
                    for (int i = 0; i < mCoreCheckBoxLITTLE.length; i++) {
                        int cur = CPU.getCurFreq(range.get(i));
                        if (mCoreCheckBoxLITTLE[i] != null) mCoreCheckBoxLITTLE[i].setChecked(cur != 0);
                        if (mCoreProgressBarLITTLE[i] != null)
                            mCoreProgressBarLITTLE[i].setProgress(CPU.getFreqs(CPU.getLITTLEcore()).indexOf(cur) + 1);
                        if (mCoreFreqTextLITTLE[i] != null)
                            mCoreFreqTextLITTLE[i].setText(cur == 0 ? getString(R.string.offline) : cur / 1000 +
                                getString(R.string.mhz));
                    }
                }

                if (mMaxFreqLITTLECard != null) {
                    int maxFreq = CPU.getMaxFreq(CPU.getLITTLEcore(), false);
                    if (maxFreq != 0)
                        mMaxFreqLITTLECard.setItem((maxFreq / 1000) + getString(R.string.mhz));
                }
                if (mMinFreqLITTLECard != null) {
                    int minFreq = CPU.getMinFreq(CPU.getLITTLEcore(), false);
                    if (minFreq != 0)
                        mMinFreqLITTLECard.setItem(minFreq / 1000 + getString(R.string.mhz));
                }
                if (mGovernorLITTLECard != null) {
                    String governor = CPU.getCurGovernor(CPU.getLITTLEcore(), false);
                    if (!governor.isEmpty()) mGovernorLITTLECard.setItem(governor);
                }
            }

        }

    public static class GovernorPart extends PathReaderFragment {

        @Override
        public String getName() {
            return CPU.getCurGovernor(cpuFragment.get().core, true);
        }

        @Override
        public String getPath() {
            return getPath(CPU.isBigLITTLE() ? String.format(Locale.US, CPU_GOVERNOR_TUNABLES_CORE, cpuFragment.get().core) :
                CPU_GOVERNOR_TUNABLES, CPU.getCurGovernor(cpuFragment.get().core, true));
        }

        private String getPath(String path, String governor) {
            if (Utils.existFile(path + "/" + governor)) return path + "/" + governor;
            else
                for (String file: new RootFile(path).list())
                    if (governor.contains(file))
                        return path + "/" + file;
            return null;
        }

        @Override
        public PATH_TYPE getType() {
            return PATH_TYPE.GOVERNOR;
        }

        @Override
        public String getError(Context context) {
            return context.getString(R.string.not_tunable, CPU.getCurGovernor(cpuFragment.get().core, true));
        }
    }

}
