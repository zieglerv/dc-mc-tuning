/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis.wireineff;

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
    private Map<Coordinate, GraphErrors> _ineffsvstrkdocasProf;
    
    public FitFunction() {
        
    }
    public FitFunction(int i, Map<Coordinate, GraphErrors> ineffsvstrkdocasProf) {
        _ineffsvstrkdocasProf = ineffsvstrkdocasProf;
        this.i = i;
    }
         
    public double eval(double x, double[] par) {
        double value = par[0]*(par[1]/Math.pow(x*x + par[2], 2) + par[3]/Math.pow( (1-x) + par[4], 2));
        return value;
    }

    @Override
    public double valueOf(double[] par) {
        double chisq = 0;
        double delta = 0;
        if(_ineffsvstrkdocasProf.get(new Coordinate(this.i)).getVectorX().size()>0){ 
            GraphErrors gr = _ineffsvstrkdocasProf.get(new Coordinate(this.i));
            for (int ix =0; ix< gr.getDataSize(0); ix++) {
                double x = gr.getDataX(ix);
                double y = gr.getDataY(ix);
                double err = gr.getDataEY(ix);
                if(err>0) {
                    double f = this.eval(x, par);
                    delta = (y - f) / err; 
                    chisq += delta * delta;
                }
            }
        }
        return chisq;
    }
}
