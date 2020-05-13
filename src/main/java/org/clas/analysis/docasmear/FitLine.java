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
        super("fcn", 0.0, 1.0);
        fc = new FitFunction();
    }
    public static final int nPars = 5;
    private double[] par = new double[nPars];
    public FitLine(String name, int i, int j, MnUserParameters pars) {
        super(name, 0.0, 1.0);
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
        double beta = DocaSmearAnal.betaValues[this.j];
        double scale_factor = par[0];
        double a1           = par[1];
        double a2           = par[2];
        double a3           = par[3];
        double a4           = par[4];
        
        double doca_smearing  = 0.001*scale_factor * ( ( Math.sqrt (x*x + a1 * beta*beta) - x ) 
                + a2 * Math.sqrt(x) + a3 * beta*beta  / (1 - x +a4) ) / DocaSmearAnal.v0[0][this.i];
        
        return doca_smearing;
 
    }

    
}
