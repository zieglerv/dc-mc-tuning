/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis.docasmear;

import org.freehep.math.minuit.MnUserParameters;
import org.jlab.groot.math.Func1D;
/**
 *
 * @author ziegler
 */
public class FitLine extends Func1D{
    public int i;
    public int j;
    private FitFunction fc ;
    public FitLine() {
        super("fcn", 0.0, 2.0);
        fc = new FitFunction();
    }
    public static final int nPars = 5;
    private double[] par = new double[nPars];
    public FitLine(String name, int i, int j, MnUserParameters pars) {
        super(name, 0.0, 2.0);
        this.i = i;
        this.j = j;
        fc = new FitFunction();
        this.initParameters(pars);
    }

    private void initParameters(MnUserParameters pars) {
        for(int p = 0; p< nPars; p++) {
            par[p] = pars.value(p);
        }
    }
    @Override
    public double evaluate(double x) { 
       
        return par[0]*(par[1]/Math.pow(x*x + par[2], 2) + par[3]/Math.pow( (1-x) + par[4], 2));
 
    }

    
}
