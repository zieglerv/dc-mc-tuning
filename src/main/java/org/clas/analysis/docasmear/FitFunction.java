/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis.docasmear;

import java.util.Map;
import org.clas.analysis.Coordinate;
import org.freehep.math.minuit.FCNBase;
import org.jlab.groot.data.GraphErrors;

/**
 *
 * @author ziegler
 */
public class FitFunction implements FCNBase{

    private int i;
    private int j;
    private Map<Coordinate, GraphErrors> _timeResvstrkdocasProf;
    
    public FitFunction() {
        
    }
    public FitFunction(int i, int j, Map<Coordinate, GraphErrors> timeResvstrkdocasProf) {
        _timeResvstrkdocasProf = timeResvstrkdocasProf;
        this.i = i;
        this.j = j;
    }
         
    public double eval(double x, double[] par) {
        double value = par[0]*(par[1]/Math.pow(x*x + par[2], 2) + par[3]/Math.pow( (1-x) + par[4], 2));
        return value;
    }

    @Override
    public double valueOf(double[] par) {
        double chisq = 0;
        double delta = 0;
        if(_timeResvstrkdocasProf.get(new Coordinate(this.i, j)).getVectorX().size()>0){ 
            GraphErrors gr = _timeResvstrkdocasProf.get(new Coordinate(this.i, j));
            for (int ix =0; ix< gr.getDataSize(0); ix++) {
                double x = gr.getDataX(ix);
                double res = gr.getDataY(ix);
                double err = gr.getDataEY(ix);
                if(err>0) {
                    double smear = this.eval(x, par);
                    delta = (res - smear) / err; 
                    chisq += delta * delta;
                }
            }
        }
        return chisq;
    }
}
