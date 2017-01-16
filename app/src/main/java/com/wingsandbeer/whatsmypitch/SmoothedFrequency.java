package com.wingsandbeer.whatsmypitch;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Get the latest recording, and based on the history decide on which frequency we are reading.
 *
 * Created by gunhan on 1/16/2017.
 */

class SmoothedFrequency {
    private Vector<Integer> last_n_freq;
    private int buffer_size;
    private int max_recording_size;

    public static final int RECORDER_SAMPLERATE = 16000;

    SmoothedFrequency(int buf_size, int maxRecSize) {
        buffer_size = buf_size;
        max_recording_size = maxRecSize;
        last_n_freq = new Vector<Integer>();
    }

    private static int maxLoc(Complex[] cmp) {
        double maxVal = 0;
        int maxInd = -1;
        double magnitude;
        for (int ktr = 0; ktr < cmp.length; ktr++) {
            magnitude = cmp[ktr].abs();
            if(magnitude > maxVal) {
                maxVal = magnitude;
                maxInd = ktr;
            }
        }
        return maxInd;
    }

    private void checkAndBuffer(int new_pitch) {
        while(last_n_freq.size() >= buffer_size) {
            last_n_freq.remove(0);
        }
        last_n_freq.add(new_pitch);
    }

    private int getBufferMod() {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int e : last_n_freq) {
            Integer count = map.get(e);
            map.put(e, count == null ? 1 : count + 1);
        }

        Integer popular = Collections.max(map.entrySet(),
                new Comparator<Map.Entry<Integer, Integer>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                        return o1.getValue().compareTo(o2.getValue());
                    }
                }).getKey();

        return popular;
    }

    public int evaluate(short sData[]) {
        int pitchLoc;
        int pitchHz;

        double pianoKeyLocation;
        int lastValidPitch = 0;

        Complex[] input_complex = new Complex[max_recording_size];
        for(int i = 0; i<max_recording_size; i++){
            input_complex[i] = new Complex(sData[i]);
        }

        Complex[] output = FFT.fft(input_complex);

        pitchLoc = maxLoc(output);
        pitchHz = pitchLoc*RECORDER_SAMPLERATE/max_recording_size;

        checkAndBuffer(pitchHz);
        return getBufferMod();
    }
}
