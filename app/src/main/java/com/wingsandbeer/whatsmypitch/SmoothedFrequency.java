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

    public static final int RECORDER_SAMPLERATE = 40000;
    private static final String[] pitchClasses = new String[] {"A ","A#","B ","C ","C#",
            "D ","D#","E ","F ","F#","G ","G#","A "};

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

    public String pianoKeyLocation(int pitchHz){

        double pianoKeyNumber = 12*Math.log10(pitchHz/440.0)/Math.log10(2.0)+49;
        double pianoKeyPitchClassIdx = (pianoKeyNumber + 11) % 12;

        int upperPitchClassIdx = ((int) Math.ceil(pianoKeyNumber) + 11)%12;
        double pianoKeysToUpper = upperPitchClassIdx - pianoKeyPitchClassIdx;
        pianoKeysToUpper = Math.round(pianoKeysToUpper*100.0) / 100.0;

        int lowerPitchClassIdx = ((int) Math.floor(pianoKeyNumber) + 11)%12;
        double pianoKeysToLower = 1 - pianoKeysToUpper;
        pianoKeysToLower = Math.round(pianoKeysToLower*100.0) / 100.0;

        String evaluation = pitchHz + " Hz \n";
        evaluation += fineTuningVisual(pianoKeyPitchClassIdx);
        //evaluation += tuningVisual(pianoKeyPitchClassIdx);

        return evaluation;
    }

    private String tuningVisual(double pianoKeyPitchClassIdx){

        int roundedPianoKeyPitchClass = (int) Math.round(pianoKeyPitchClassIdx * 5.0);
        int startIdx = (int) (Math.floor(pianoKeyPitchClassIdx) + 10) % 12;
        int endIdx = startIdx + 5; // to have 6 notes in the visual

        String visual = "";
        System.out.println(roundedPianoKeyPitchClass);

        for (int i = startIdx*5; i <= endIdx*5; i++){
            if(i%5 == 0) {
                visual += pitchClasses[(i/5) % 12];
            } else if (i%60 == roundedPianoKeyPitchClass){
                visual += "|";
            } else {
                visual += ".";
            }
        }
        if(roundedPianoKeyPitchClass%5 == 0){
            visual += "\n You're in tune with " + pitchClasses[(roundedPianoKeyPitchClass/5) % 12];
        }
        visual += "\n";
        return visual;
    }

    private String fineTuningVisual(double pianoKeyPitchClassIdx){

        int roundedPianoKeyPitchClass = (int) Math.round(pianoKeyPitchClassIdx * 10.0);
        int startIdx = (int) (Math.floor(pianoKeyPitchClassIdx) + 11) % 12;
        int endIdx = startIdx + 3; // to have 6 notes in the visual

        String visual = "";
        System.out.println(roundedPianoKeyPitchClass);

        for (int i = startIdx*10; i <= endIdx*10; i++){
            if(i%10 == 0) {
                visual += pitchClasses[(i/10) % 12];
            } else if (i%120 == roundedPianoKeyPitchClass){
                visual += "|";
            } else {
                visual += ".";
            }
        }
        if(roundedPianoKeyPitchClass%10 == 0){
            visual += "\n You're in tune with " + pitchClasses[(roundedPianoKeyPitchClass/10) % 12];
        }
        visual += "\n";

        return visual;
    }

}
