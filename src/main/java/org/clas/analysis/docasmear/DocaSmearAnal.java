/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.analysis.docasmear;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap; 
import java.util.Map;
import org.clas.viewer.AnalysisMonitor;
import org.freehep.math.minuit.FCNBase;
import org.freehep.math.minuit.FunctionMinimum;
import org.freehep.math.minuit.MnMigrad;
import org.freehep.math.minuit.MnScan;
import org.freehep.math.minuit.MnUserParameters;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent; 
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.groot.data.GraphErrors;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.rec.dc.Constants;
import org.clas.analysis.Coordinate;
import org.jlab.utils.groups.IndexedList;
import org.jlab.utils.system.ClasUtilsFile;
import org.clas.analysis.FitPanel;
/**
 *
 * @author ziegler
 */
public class DocaSmearAnal extends AnalysisMonitor{
    private SchemaFactory schemaFactory = new SchemaFactory();
    PrintWriter pw = null;
    File outfile = null;
    private int runNumber;
    private FitPanel fp;
    
    public DocaSmearAnal(String name, ConstantsManager ccdb) throws FileNotFoundException {
        super(name, ccdb);
        this.setAnalysisTabNames("Time Resi vs TrackDoca","Time Resi vs TrackDoca Graphs", "Beta Dependence");
        this.init(false, "p0:p1:p2:p3:p4");
        outfile = new File("Files/ccdbConstants.txt");
        pw = new PrintWriter(outfile);
        pw.printf("#& sector superlayer component v0 deltanm tmax distbeta delta_bfield_coefficient b1 b2 b3 b4 delta_T0 c1 c2 c3\n");
        
        String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
        schemaFactory.initFromDirectory(dir);
       
        if(schemaFactory.hasSchema("TimeBasedTrkg::TBHits")) {
            System.out.println(" BANK FOUND........");
        } else {
            System.out.println(" BANK NOT FOUND........");
        }
        
        
    }
    private Map<Coordinate, H2F> timeResVsTrkDoca                      = new HashMap<Coordinate, H2F>();
    private Map<Coordinate, GraphErrors> timeResVsTrkDocaProf          = new HashMap<Coordinate, GraphErrors>();
    private Map<Coordinate, FitFunction> timResVsTrkDocaFit            = new HashMap<Coordinate, FitFunction>();
    private Map<Coordinate, MnUserParameters> timeResVsTrkDocaFitPars  = new HashMap<Coordinate, MnUserParameters>();
    public  Map<Coordinate, FitLine> timeResVsTrkDocaFits              = new HashMap<Coordinate, FitLine>();
    private Map<Coordinate, H1F> parsVsBeta                            = new HashMap<Coordinate, H1F>();
    
    int nsl = 6;

    public static double[] betaValues = new double[]{0.89, 0.91, 0.93, 0.95, 0.97, 0.99};
    double betaBinHalfWidth = (betaValues[1]-betaValues[0])*0.5;
    public static int betaBins = betaValues.length;
    
    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        
        DataGroup tr = new DataGroup(6,6);
        
        for (int i = 0; i < nsl; i++) {
            for (int j = 0; j < betaBins; j++) {
                DataGroup trkdvst = new DataGroup(1,1);
                DataGroup dvst = new DataGroup(1,1);
                timeResVsTrkDocaFitPars.put(new Coordinate(i,j), new MnUserParameters());
                timeResVsTrkDoca.put(new Coordinate(i,j), new H2F("timeResVsTrkDoca" + (i + 1)*1000+(j+1)+26, "superlayer" + (i + 1)
                            + ", beta ("+(betaValues[j]-betaBinHalfWidth)+", "+(betaValues[j]+betaBinHalfWidth)+")"
                             , 40, 0, 1.0, 40, -0.5, 0.5));
                    
                timeResVsTrkDocaProf.put(new Coordinate(i,j), new GraphErrors());
                timeResVsTrkDocaProf.get(new Coordinate(i,j)).setMarkerColor(1);
                tr.addDataSet(timeResVsTrkDocaProf.get(new Coordinate(i,j)), 0);
                timeResVsTrkDocaFits.put(new Coordinate(i,j), new FitLine());
               
                this.getDataGroup().add(tr, 0, i+1, j+1);

            }
        }
        
        this.getDataGroup().add(tr, 0,0,0);
        for (int i = 0; i < nsl; i++) {
            for(int k = 0; k<5; k++) { //pars
                parsVsBeta.put(new Coordinate(i,k), new H1F("par "+k*1000+i, "superlayer" + (i + 1), 
                        betaValues.length, betaValues[0]-(betaValues[1]-betaValues[0])/2, betaValues[betaValues.length-1]-(betaValues[1]-betaValues[0])/2));
            }
        }
        
        
        for (int i = 0; i < nsl; i++) {
            for (int j = 0; j < betaBins; j++) {
                this.getCalib().addEntry(0,i+1,j+1);
                //blank out
                this.getCalib().setDoubleValue((double)999, "p0", 0, i+1, j+1);
                this.getCalib().setDoubleValue((double)999, "p1", 0, i+1, j+1);
                this.getCalib().setDoubleValue((double)999, "p2", 0, i+1, j+1);
                this.getCalib().setDoubleValue((double)999, "p3", 0, i+1, j+1);
                this.getCalib().setDoubleValue((double)999, "p4", 0, i+1, j+1);
                this.getCalib().setDoubleValue((double)999, "p5", 0, i+1, j+1);
            }
        }
        
        this.getCalib().fireTableDataChanged();
    }
    private void updateTable(int i, int j) {
        this.getCalib().setDoubleValue(timeResVsTrkDocaFitPars.get(new Coordinate(i,j)).value(0), "p0", 0, i+1, j+1);
        this.getCalib().setDoubleValue(timeResVsTrkDocaFitPars.get(new Coordinate(i,j)).value(1), "p1", 0, i+1, j+1);
        this.getCalib().setDoubleValue(timeResVsTrkDocaFitPars.get(new Coordinate(i,j)).value(2), "p2", 0, i+1, j+1);
        this.getCalib().setDoubleValue(timeResVsTrkDocaFitPars.get(new Coordinate(i,j)).value(3), "p3", 0, i+1, j+1);
        this.getCalib().setDoubleValue(timeResVsTrkDocaFitPars.get(new Coordinate(i,j)).value(4), "p4", 0, i+1, j+1);
    }    
    @Override
    public void plotHistos() {
        String[] Names = {"Time Resi vs TrackDoca","Time Resi vs TrackDoca Graphs", "Beta Dependence"};
        
        this.getAnalysisCanvas().getCanvas(Names[2]).divide(6, 5);
        for(int s = 0; s<Names.length; s++) {
            this.getAnalysisCanvas().getCanvas(Names[s]).setGridX(false);
            this.getAnalysisCanvas().getCanvas(Names[s]).setGridY(false);
        
            int NumPads = 
            this.getAnalysisCanvas().getCanvas(Names[s]).getCanvasPads().size();
            for (int n = 0; n < NumPads; n++) {
                this.getAnalysisCanvas().getCanvas(Names[s]).getPad(n).getAxisZ().setLog(true);
            }
        }
        
        
        
        this.getAnalysisCanvas().getCanvas("Time Resi vs TrackDoca").update();
        this.getAnalysisCanvas().getCanvas("Time Resi vs TrackDoca Graphs").update();
        this.getAnalysisCanvas().getCanvas("Beta Dependence").update();
    }
    
    @Override
    public void timerUpdate() {
    }
    
    @Override
    public void analysis() {
        loadFitPars();
        
        for (int i = 0; i < this.nsl; i++) {
            for (int j = 0; j < this.betaBins; j++) {
                this.filltrkDocavsTGraphs(i,j);
                this.runFit(i, j);
            }
        }
        plotFits() ;
        for (int i = 0; i < this.nsl; i++) {
            for (int j = 0; j < this.betaBins; j++) {
                this.Plot(i,j);
            }
        }
        int ik =0;
        for (int i = 0; i < this.nsl; i++) {
            for (int k = 0; k < 5; k++) {
                this.getAnalysisCanvas().getCanvas("Beta Dependence").cd(ik);
                this.getAnalysisCanvas().getCanvas("Beta Dependence").draw(
                        parsVsBeta.get(new Coordinate(i,k)));
                ik++;
            }
        }
    }
    public void plotFits() {
        pw.close();
        File file2 = new File("");
        file2 = outfile;
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
        String fileName = "Files/ccdb_run" + this.runNumber + "time_" 
                + df.format(new Date())+ ".txt";
        file2.renameTo(new File(fileName));
        int ij =0;
        int ip =0;
        for (int i = 0; i < this.nsl; i++) {

            for (int j = 0; j < this.betaBins; j++) {

                if(timeResVsTrkDocaProf.get(new Coordinate(i, j)).getVectorX().size()>0) {
                    this.updateTable(i,j);
                    timeResVsTrkDocaFits.put(new Coordinate(i,j), new FitLine("f"+""+i+""+j+"0", i, j, 
                    timeResVsTrkDocaFitPars.get(new Coordinate(i,j))));
                    timeResVsTrkDocaFits.get(new Coordinate(i, j)).setLineStyle(4);
                    timeResVsTrkDocaFits.get(new Coordinate(i, j)).setLineWidth(5);
                    timeResVsTrkDocaFits.get(new Coordinate(i, j)).setLineColor(8);
                }

                ij++;
            }
            this.getCalib().fireTableDataChanged();  
        }
    }
    private int maxIter = 2;
    double[][] fixPars = new double[6][10];
    
    private MnScan  scanner = null;
    private MnMigrad migrad = null;
    
    public int NbRunFit = 0;
    public void runFit(int i, int j) {
        // i = superlayer - 1;
        System.out.println(" **************** ");
        System.out.println(" RUNNING THE FITS ");
        System.out.println(" **************** ");
        timResVsTrkDocaFit.put(new Coordinate(i, j), 
                new FitFunction(i, j, (Map<Coordinate, GraphErrors>) timeResVsTrkDocaProf));
        
        scanner = new MnScan((FCNBase) timResVsTrkDocaFit.get(new Coordinate(i,j)), 
                timeResVsTrkDocaFitPars.get(new Coordinate(i,j)),2);
	
         System.out.println(" Ready to minimize..... ");
        FunctionMinimum scanmin = scanner.minimize();
        for(int pi = 0; pi<5; pi++) 
                System.out.println("scan par["+pi+"]="+timeResVsTrkDocaFitPars.get(new Coordinate(i,j)).value(pi));
           
        //if(scanmin.isValid())
            timeResVsTrkDocaFitPars.put(new Coordinate(i,j),scanmin.userParameters());
        
        migrad = new MnMigrad((FCNBase) timResVsTrkDocaFit.get(new Coordinate(i,j)), 
                timeResVsTrkDocaFitPars.get(new Coordinate(i,j)),1);
        migrad.setCheckAnalyticalDerivatives(true);
        
        FunctionMinimum min ;
        
        
        for(int it = 0; it<maxIter; it++) {
            
            min = migrad.minimize();
            System.err.println("****************************************************");
            System.err.println("*   FIT RESULTS  FOR SUPERLAYER  "+(i+1)+" at iteration "+(it+1)+"  *");
            System.err.println("****************************************************");  
            for(int pi = 0; pi<5; pi++) 
                System.out.println("par["+pi+"]="+(timeResVsTrkDocaFitPars.get(new Coordinate(i,j)).value(pi)-min.userParameters().value(pi)));
            
            //if(min.isValid()) {
                timeResVsTrkDocaFitPars.put(new Coordinate(i,j),min.userParameters());  
            //}
            System.err.println(min);
            for(int k = 0; k<5; k++) {
                parsVsBeta.get(new Coordinate(i,k)).setBinContent(k, timeResVsTrkDocaFitPars.get(new Coordinate(i,j)).value(k));
                parsVsBeta.get(new Coordinate(i,k)).setBinError(k, timeResVsTrkDocaFitPars.get(new Coordinate(i,j)).error(k));
            }       
                    
        }
        
//        for(int isec = 0; isec < 6; isec++) {
//           
//            pw.printf("%d\t %d\t %d\t %.6f\t %d\t %.6f\t %.6f\t %.6f\t %.6f\t %.6f\t %.6f\t %.6f\t %d\t %.6f\t %.6f\t %d\n",
//                (isec+1), (i+1), 
//                timeResVsTrkDocaFitPars.get(new Coordinate(i)).value(0),
//                timeResVsTrkDocaFitPars.get(new Coordinate(i)).value(1),
//                timeResVsTrkDocaFitPars.get(new Coordinate(i)).value(2),
//                timeResVsTrkDocaFitPars.get(new Coordinate(i)).value(3),
//                timeResVsTrkDocaFitPars.get(new Coordinate(i)).value(4),
//                0);
//        }
        
        
    }
    
    int counter = 0;
    private int iterationNum = 1;
   
    private int getBetaBin(double b) {
        
        int v = 0;
        for(int i = 0; i<betaValues.length; i++) {
            if(Math.abs(b-betaValues[i])<this.betaBinHalfWidth)
                v = i;
        }      
        
        return v;
    }

    
    private int MINENTRIES = 10;
    F1D f1 = new F1D("f1","[amp]*gaus(x,[mean],[sigma])+[p0]", -0.5, 0.5);
    
    private void filltrkDocavsTGraphs(int i, int j) {
        
        if(timeResVsTrkDocaProf.get(new Coordinate(i, j))!=null) {
            
            timeResVsTrkDocaProf.get(new Coordinate(i, j)).reset();
            H2F h2 = timeResVsTrkDoca.get(new Coordinate(i, j));
            ArrayList<H1F> hslice = h2.getSlicesX();
            
            for(int si=0; si<hslice.size(); si++) {
                double amp   = hslice.get(si).getBinContent(hslice.get(si).getMaximumBin());
                
                if(amp<this.MINENTRIES) {
                    
                } else {
                    double x = h2.getXAxis().getBinCenter(si);
                    double y = hslice.get(si).getMean();
                    double sigma = hslice.get(si).getRMS();
                    
                    f1.setParameter(0, amp);
                    f1.setParameter(1, y);
                    f1.setParameter(2, sigma);
                    f1.setParameter(3, 0);
                    DataFitter.fit(f1, hslice.get(si), "Q"); //No options uses error for sigma 
                    if(f1.parameter(1).error()>0 && Math.abs(f1.parameter(2).error())<0.1) {
                        timeResVsTrkDocaProf.get(new Coordinate(i, j)).
                                addPoint(x, Math.abs(f1.getParameter(2)), 0, Math.abs(f1.parameter(2).error()));//f1.parameter(1).error()
                        
                    } 
                }
            }
        }
    }

    int count = 0;
    public static int polarity =-1;
    @Override
    public void processEvent(DataEvent event) {
        
        if (!event.hasBank("RUN::config")) {
            return ;
        }
        
        DataBank bank = event.getBank("RUN::config");
        int newRun = bank.getInt("run", 0);
        if (newRun == 0) {
           return ;
        } else {
           count++;
        }
       
        if(count==1) {
            Constants.Load();
            polarity = (int)Math.signum(event.getBank("RUN::config").getFloat("torus",0));
            runNumber = newRun;
        }
        if(!event.hasBank("TimeBasedTrkg::TBHits")) {
            return;
        } 
        // get segment property
        
        DataBank bnkHits = event.getBank("TimeBasedTrkg::TBHits");
        
        for (int i = 0; i < bnkHits.rows(); i++) {
                
            int superlayer = bnkHits.getInt("superlayer", i);
            double beta = bnkHits.getFloat("beta", i);    
            if (bnkHits.getByte("trkID", i) >0 
                    && beta > 0.5
                    && bnkHits.getFloat("TFlight", i)>0 
                    && Math.abs(bnkHits.getFloat("fitResidual", i))<0.075)
            {
                int betaBin = this.getBetaBin(beta);
                 
                timeResVsTrkDoca.get(new Coordinate(bnkHits.getInt("superlayer", i) - 1, betaBin))
                                .fill(bnkHits.getFloat("trkDoca", i)/(2.0*Constants.wpdist[bnkHits.getByte("superlayer", i)-1]), bnkHits.getFloat("timeResidual", i));
                
            }
        }
    }
    
    private String[] parNames = {"p0", "p1", "p2", "p3", "p4"};
    private double[] errs = {0.001,0.001,0.01,0.01,0.01};
    
    public void loadFitPars() {
        for (int i = 0; i < this.nsl; i++) {
            for (int j = 0; j < betaValues.length; j++) {
                double[] pars = new double[parNames.length];
                pars[0] = 2.902506e-02;
                pars[1] = 2.884514e-02;
                pars[2] = 1.298221e-01;
                pars[3] = 1.981027e-01;
                pars[4] = 0.0;
                timeResVsTrkDocaFitPars.put(new Coordinate(i,j), new MnUserParameters());
                for(int p = 0; p < parNames.length; p++) {
                    timeResVsTrkDocaFitPars.get(new Coordinate(i,j)).add(parNames[p], pars[p], errs[p]);
                }
                for(int pi = 0; pi<5; pi++) 
                    System.out.println("loaded par["+pi+"]="+(timeResVsTrkDocaFitPars.get(new Coordinate(i,j)).value(pi)));
           
            }   
        }
        // Fit panel
        //fp = new FitPanel();
        //fp.openFitPanel("fit panel", timeResVsTrkDocaFitPars);
    }
    
    public void Plot(int i , int j) {
        
        this.getAnalysisCanvas().getCanvas("Time Resi vs TrackDoca").cd(0);
        this.getAnalysisCanvas().getCanvas("Time Resi vs TrackDoca").draw(timeResVsTrkDoca.get(new Coordinate(i, j)));
        this.getAnalysisCanvas().getCanvas("Time Resi vs TrackDoca Graphs").cd(0);
        //this.getAnalysisCanvas().getCanvas("Time Resi vs TrackDoca Graphs").draw(timeResVsTrkDoca.get(new Coordinate(i, j)));
        if(timeResVsTrkDocaProf.get(new Coordinate(i, j)).getVectorX().size()>0){
                    this.getAnalysisCanvas().getCanvas("Time Resi vs TrackDoca Graphs").
                            draw(timeResVsTrkDocaProf.get(new Coordinate(i, j)), "E");
                    this.getAnalysisCanvas().getCanvas("Time Resi vs TrackDoca Graphs").
                            draw(timeResVsTrkDocaFits.get(new Coordinate(i, j)), "same");
        }
    }
    @Override
    public void constantsEvent(CalibrationConstants cc, int col, int row) {
        String str_sector    = (String) cc.getValueAt(row, 0);
        String str_layer     = (String) cc.getValueAt(row, 1);
        String str_component = (String) cc.getValueAt(row, 2);
        System.out.println(str_sector + " " + str_layer + " " + str_component);
        IndexedList<DataGroup> group = this.getDataGroup();

       int sector    = Integer.parseInt(str_sector);
       int layer     = Integer.parseInt(str_layer);
       int component = Integer.parseInt(str_component);

       if(group.hasItem(sector,layer,component)==true){
           this.Plot(layer-1, component-1);
       } else {
           System.out.println(" ERROR: can not find the data group");
       }
       
   
    }
    
}

