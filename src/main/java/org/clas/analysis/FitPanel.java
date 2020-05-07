/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JFrame;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.freehep.math.minuit.MnUserParameters;


public class FitPanel {
    
    
    private Map<Integer, ArrayList<Double>> pars     = new HashMap<Integer, ArrayList<Double>>();
    private double[]          range    = new double[2];
    private JFrame            frame    = new JFrame();
    private CustomPanel2      panel    = null;
    
    public FitPanel() {
        //init pars container
        for(int j = 0; j<6; j++) {
            pars.put(j, new ArrayList<Double>());
        }
    }
    
    public void openFitPanel(String title, Map<Coordinate, MnUserParameters> TvstrkdocasFitPars){
        
        panel = new CustomPanel2(TvstrkdocasFitPars);
        frame.setSize(350, 300); 
        frame.setTitle(title);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            
    }
    
    public boolean fitted = false;
    public void refit(Map<Coordinate, MnUserParameters> TvstrkdocasFitPars){
        boolean[][] fixedPars = new boolean[10][6];
        for(int j = 0; j<6; j++) {
            pars.get(j).clear();
        }
        int npar = 10;
        for(int j = 0; j<6; j++) {
            for(int i=0; i<npar; i++){   
                if(panel.params[i][j].getText().isEmpty()){
                    this.pars.get(j).add(TvstrkdocasFitPars.get(new Coordinate(j)).value(i));
                }
                else { 
                    this.pars.get(j).add(Double.parseDouble(panel.params[i][j].getText()));
                }
            }
        }
        if(!panel.minRange.getText().isEmpty())this.range[0] = Double.parseDouble(panel.minRange.getText());
        else this.range[0] = 0.0;
        if(!panel.maxRange.getText().isEmpty())this.range[1] = Double.parseDouble(panel.maxRange.getText());
        else this.range[1] = 2.0;
        for(int j = 0; j<6; j++) {    
            for(int i=0; i<npar; i++){
                TvstrkdocasFitPars.get(new Coordinate(j)).setValue(i,this.pars.get(j).get(i));
            }
        }
        
        for(int j = 0; j<6; j++) {
            for(int i=0; i<npar; i++){ 
                System.out.println("j "+j+" par "+this.pars.get(j).get(i));
                if(panel.fixFit[i][j].isSelected()==true)
                    fixedPars[i][j] = true;
            }
            
            for(int p = 0; p<10; p++) {
                panel.pars[p][j] = TvstrkdocasFitPars.get(new Coordinate(j)).value(p);
                if(p!=3) {
                    panel.params[p][j].setText(String.format("%.5f",TvstrkdocasFitPars.get(new Coordinate(j)).value(p)));
                } else {
                    panel.params[p][j].setText(String.format("%.3f",TvstrkdocasFitPars.get(new Coordinate(j)).value(p)));
                }
            }
        }
        fitted = true;
    }
    
    
    private final class CustomPanel2 extends JPanel {
        JLabel label;
        JPanel panel;
    	JTextField minRange = new JTextField(5);
	JTextField maxRange = new JTextField(5);
	JTextField[][] params = new JTextField[10][6];
        JCheckBox[][]    fixFit ;
       
        String[] parNames = new String[] {"p0", "p1", "p2", "p3", "p4"};
        double[][] pars = new double[5][6];
        
        public CustomPanel2(Map<Coordinate, MnUserParameters> TvstrkdocasFitPars) {        
            super(new BorderLayout());
            for(int i = 0; i < 6; i++) {
                for(int p = 0; p<10; p++) {
                    pars[p][i] = TvstrkdocasFitPars.get(new Coordinate(i)).value(p);
                }
            }
            int npar = 10;
            panel = new JPanel(new GridLayout(npar+1, 6));            
            fixFit = new JCheckBox[10][6];
            for (int i = 0; i < npar; i++) {  
                JLabel l = new JLabel("      "+parNames[i], JLabel.LEADING);
                panel.add(l);
                for (int j = 0; j < 6; j++) {
                    fixFit[i][j] = new JCheckBox("Fix");
                    if(i==2 || i>4) {
                        fixFit[i][j].setSelected(true);
                    } else {
                        fixFit[i][j].setSelected(false);
                    }
                    
                    params[i][j] = new JTextField(3);
                    if(i!=3) {
                        params[i][j].setText(String.format("%.5f", pars[i][j]));
                    } else {
                        params[i][j].setText(String.format("%.3f", pars[i][j]));
                    }
                    panel.add(params[i][j]);
                    panel.add(fixFit[i][j]);
                }
            }
            panel.add(new JLabel("    Fit range min"));
            minRange.setText(Double.toString(0));
            panel.add(minRange);
            panel.add(new JLabel("    Fit range max"));
            maxRange.setText(Double.toString(2.0));
            panel.add(maxRange);
            
            this.add(panel, BorderLayout.CENTER);
            
            label = new JLabel("Click the \"Show it!\" button"
                           + " to bring up the selected dialog.",
                           JLabel.CENTER);
        }
        private Font bBold = new Font("Arial", Font.BOLD, 16);
        void setLabel(String newText) {
            label.setText(newText);
        }

    }
}