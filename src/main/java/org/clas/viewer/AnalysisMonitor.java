
package org.clas.viewer;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.jlab.detector.calib.tasks.CalibrationEngine;
import org.jlab.detector.calib.utils.CalibrationConstants;
import org.jlab.detector.calib.utils.CalibrationConstantsListener;
import org.jlab.detector.calib.utils.CalibrationConstantsView;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.IDataEventListener;
import org.jlab.utils.groups.IndexedList;

public class AnalysisMonitor extends CalibrationEngine implements IDataEventListener, 
        CalibrationConstantsListener{    
    
    private final String           analysisName;
    public ArrayList<String>      analysisTabNames  = new ArrayList();
    private IndexedList<DataGroup> analysisData    = new IndexedList<DataGroup>(3);
    private DataGroup              analysisSummary = null;
    private JPanel                 analysisPanel   = null;
    private EmbeddedCanvasTabbed   analysisCanvas  = null;
    private DetectorPane2D         analysisView    = null;
    public ConstantsManager        ccdb            = null;
    private int                    numberOfEvents;
    private CalibrationConstants   calib           = null;
    private CalibrationConstants   prevcalib       = null;
    
    CalibrationConstantsView ccview                = null;    

    public CalibrationConstantsView getCcview() {
        return ccview;
    }
    
    
    public CalibrationConstants getCalib() {
        return calib;
    }

    public CalibrationConstants getPrevcalib() {
        return prevcalib;
    }
    
    public AnalysisMonitor(String name, ConstantsManager ccdb){
        this.setStyle(0);
                               
        this.analysisName = name;
        this.analysisPanel  = new JPanel();
        this.analysisCanvas = new EmbeddedCanvasTabbed();
        this.analysisView   = new DetectorPane2D();
        this.ccdb           = ccdb;
        this.numberOfEvents = 0;
    }

    
    public void analyze() {
        // analyze detector data at the end of data processing
    }

    public void createHistos() {
        // initialize canvas and create histograms
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
        
        this.setNumberOfEvents(this.getNumberOfEvents()+1);
        if (event.getType() == DataEventType.EVENT_START) {
//            resetEventListener();
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_SINGLE) {
            processEvent(event);
            plotEvent(event);
	} else if (event.getType() == DataEventType.EVENT_ACCUMULATE) {
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_STOP) {
            analyze();
	}
    }

    public void drawDetector() {
    
    }
    
    public EmbeddedCanvasTabbed getAnalysisCanvas() {
        return analysisCanvas;
    }
    
    public ArrayList<String> getAnalysisTabNames() {
        return analysisTabNames;
    }
    
    public IndexedList<DataGroup>  getDataGroup(){
        return analysisData;
    }

    public String getAnalysisName() {
        return analysisName;
    }
    
    public JPanel getAnalysisPanel() {
        return analysisPanel;
    }
    
    public DataGroup getAnalysisSummary() {
        return analysisSummary;
    }
    
    public DetectorPane2D getAnalysisView() {
        return analysisView;
    }

    public ConstantsManager getCcdb() {
        return ccdb;
    }
    
    public int getNumberOfEvents() {
        return numberOfEvents;
    }

    public void init(boolean flagDetectorView, String constants) {
        // initialize monitoring application
        // detector view is shown if flag is true
        getAnalysisPanel().setLayout(new BorderLayout());
        drawDetector();
        JSplitPane   splitPane = new JSplitPane();
        splitPane.setLeftComponent(getAnalysisView());
        splitPane.setRightComponent(getAnalysisCanvas());
        if(flagDetectorView) {
            getAnalysisPanel().add(splitPane,BorderLayout.CENTER);  
        }
        else {
            getAnalysisPanel().add(getAnalysisCanvas(),BorderLayout.CENTER);  
        }
        calib = new CalibrationConstants(3,constants);
        prevcalib = new CalibrationConstants(3,constants);
        ccview = new CalibrationConstantsView();
        
        ccview.addConstants(this.getCalib(), this);
        createHistos();
        plotHistos();
        
    }
    
    public void processEvent(DataEvent event) {
        // process event
    }
    
    public void plotEvent(DataEvent event) {
        // process event
    }
    
    public void plotHistos() {

    }
    
    public void printCanvas(String dir) {
        // print canvas to files
        for(int tab=0; tab<this.analysisTabNames.size(); tab++) {
            String fileName = dir + "/" + this.analysisName + "_canvas" + tab + ".png";
            System.out.println(fileName);
            this.analysisCanvas.getCanvas(this.analysisTabNames.get(tab)).save(fileName);
        }
    }
    
    @Override
    public void resetEventListener() {
        System.out.println("Resetting " + this.getAnalysisName() + " histogram");
        this.createHistos();
        this.plotHistos();
    }
    
    public void setAnalysisCanvas(EmbeddedCanvasTabbed canvas) {
        this.analysisCanvas = canvas;
    }
    
    public void setAnalysisSummary(DataGroup group) {
        this.analysisSummary = group;
    }
    
    public void setAnalysisTabNames(String... names) {
        for(String name : names) {
            this.analysisTabNames.add(name);
        }
        EmbeddedCanvasTabbed canvas = new EmbeddedCanvasTabbed(names);
        this.setAnalysisCanvas(canvas);
    }

    public void setCanvasUpdate(int time) {
        for(int tab=0; tab<this.analysisTabNames.size(); tab++) {
            this.analysisCanvas.getCanvas(this.analysisTabNames.get(tab)).initTimer(time);
        }
    }

    public void setCcdb(ConstantsManager ccdb) {
        this.ccdb = ccdb;
    }
    
    public void setNumberOfEvents(int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    public void setStyle(int mode) {
        if(mode==1) {
            GStyle.getAxisAttributesX().setTitleFontSize(32);
            GStyle.getAxisAttributesX().setLabelFontSize(24);
            GStyle.getAxisAttributesY().setTitleFontSize(32);
            GStyle.getAxisAttributesY().setLabelFontSize(24);
            GStyle.getAxisAttributesZ().setLabelFontSize(14);
            GStyle.getH1FAttributes().setLineWidth(2);
            GStyle.getAxisAttributesX().setLabelFontName("Arial");
            GStyle.getAxisAttributesY().setLabelFontName("Arial");
            GStyle.getAxisAttributesZ().setLabelFontName("Arial");
            GStyle.getAxisAttributesX().setTitleFontName("Arial");
            GStyle.getAxisAttributesY().setTitleFontName("Arial");
            GStyle.getAxisAttributesZ().setTitleFontName("Arial");           
        }
        else {
            GStyle.getAxisAttributesX().setTitleFontSize(24);
            GStyle.getAxisAttributesX().setLabelFontSize(18);
            GStyle.getAxisAttributesY().setTitleFontSize(24);
            GStyle.getAxisAttributesY().setLabelFontSize(18);
            GStyle.getAxisAttributesZ().setLabelFontSize(14);
            GStyle.getAxisAttributesX().setLabelFontName("Avenir");
            GStyle.getAxisAttributesY().setLabelFontName("Avenir");
            GStyle.getAxisAttributesZ().setLabelFontName("Avenir");
            GStyle.getAxisAttributesX().setTitleFontName("Avenir");
            GStyle.getAxisAttributesY().setTitleFontName("Avenir");
            GStyle.getAxisAttributesZ().setTitleFontName("Avenir");
            GStyle.setGraphicsFrameLineWidth(1);
            GStyle.getH1FAttributes().setLineWidth(1); 
            GStyle.getH1FAttributes().setOptStat("1111");
        }
    }

    @Override
    public void timerUpdate() {
        
    }
    public void analysis() {
        
    }
    public void readDataGroup(TDirectory dir) {
        String folder = this.getAnalysisName() + "/";
        System.out.println("Reading from: " + folder);
        DataGroup sum = this.getAnalysisSummary();
        int nrows = sum.getRows();
        int ncols = sum.getColumns();
        int nds   = nrows*ncols;
        DataGroup newSum = new DataGroup(ncols,nrows);
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = sum.getData(i);
            for(IDataSet ds : dsList){
                System.out.println("\t --> " + ds.getName());
                newSum.addDataSet(dir.getObject(folder, ds.getName()),i);
            }
        }            
        this.setAnalysisSummary(newSum);
        Map<Long, DataGroup> map = this.getDataGroup().getMap();
        for( Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            Long key = entry.getKey();
            DataGroup group = entry.getValue();
            nrows = group.getRows();
            ncols = group.getColumns();
            nds   = nrows*ncols;
            DataGroup newGroup = new DataGroup(ncols,nrows);
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = group.getData(i);
                for(IDataSet ds : dsList){
                    System.out.println("\t --> " + ds.getName());
                    newGroup.addDataSet(dir.getObject(folder, ds.getName()),i);
                }
            }
            map.replace(key, newGroup);
        }
        this.plotHistos();
    }
    
    public void writeDataGroup(TDirectory dir) {
        String folder = "/" + this.getAnalysisName();
        dir.mkdir(folder);
        dir.cd(folder);
        DataGroup sum = this.getAnalysisSummary();
        int nrows = sum.getRows();
        int ncols = sum.getColumns();
        int nds   = nrows*ncols;
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = sum.getData(i);
            for(IDataSet ds : dsList){
                System.out.println("\t --> " + ds.getName());
                dir.addDataSet(ds);
            }
        }            
        Map<Long, DataGroup> map = this.getDataGroup().getMap();
        for( Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            DataGroup group = entry.getValue();
            nrows = group.getRows();
            ncols = group.getColumns();
            nds   = nrows*ncols;
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = group.getData(i);
                for(IDataSet ds : dsList){
                    System.out.println("\t --> " + ds.getName());
                    dir.addDataSet(ds);
                }
            }
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
           this.getAnalysisCanvas().getCanvas().draw(group.getItem(sector, layer, component));
       } else {
           System.out.println(" ERROR: can not find the data group");
       }
       
   
    }

    
} 
