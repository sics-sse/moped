package com.sun.squawk.builder.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SuiteMetadata implements Serializable {

    protected List<String> targetsIncluded;
    
    public SuiteMetadata() {
        targetsIncluded = new ArrayList<String>(0);
    }
    
    public boolean includesTarget(String name) {
        int index = targetsIncluded.indexOf(name);
        return index != -1;
    }
    
    public void addTargetIncluded(String included) {
        targetsIncluded.add(included);
    }
    
    public void addTargetsIncluded(List<String> included) {
        targetsIncluded.addAll(included);
    }
    
    public void addTargetsIncluded(String[] included) {
        Collections.addAll(targetsIncluded, included);
    }
    
    public List<String> getTargetsIncluded() {
        return targetsIncluded;
    }
    
}
