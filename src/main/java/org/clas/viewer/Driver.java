/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.metal.MetalButtonUI;

/**
 *
 * @author ziegler
 */
public class Driver {
    
      
    public static void main(String[] args) throws FileNotFoundException {
        JFrame    frame    = new JFrame();
        JButton   IneffButton = null;
        JButton   DocaSmear = null;
        JPanel panel = new JPanel(new GridLayout(4, 1)); 
        frame.setSize(1400, 800); 
        frame.setTitle("DC MC TUNING");
        ImageIcon imageIcon = new ImageIcon("DC.png");
        imageIcon.getImage().getScaledInstance(800, 400, java.awt.Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(imageIcon);
        frame.add(imgLabel, BorderLayout.PAGE_START);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        
        IneffButton = new JButton("Wire Inefficiency Analysis");
        IneffButton.setUI(new MetalButtonUI());
        IneffButton.setBackground(Color.YELLOW);
        IneffButton.setContentAreaFilled(false);
        IneffButton.setOpaque(true);
        IneffButton.setFont(new Font("Arial", Font.BOLD, 18));
        IneffButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("DC Calibration");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                WireIneffAnalViewer viewer = null;
                try {
                    viewer = new WireIneffAnalViewer();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, null, ex);
                }
                frame.add(viewer.mainPanel);
                frame.setJMenuBar(viewer.menuBar);
                frame.setSize(300, 300);
                frame.setVisible(true);
                viewer.configFrame.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                //viewer.configure();
                return;
            }
        });
        panel.add(IneffButton);
        
        DocaSmear = new JButton("Doca Smearing Analysis");
        DocaSmear.setUI(new MetalButtonUI());
        DocaSmear.setBackground(Color.ORANGE);
        DocaSmear.setContentAreaFilled(false);
        DocaSmear.setOpaque(true);
        DocaSmear.setFont(new Font("Arial", Font.BOLD, 18));
        DocaSmear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFrame frame = new JFrame("DC MC Tuning");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                DocaSmearAnalViewer viewer = null;
                try {
                    viewer = new DocaSmearAnalViewer();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, null, ex);
                }
                frame.add(viewer.mainPanel);
                frame.setJMenuBar(viewer.menuBar);
                frame.setSize(300, 300);
                frame.setVisible(true);
                viewer.configFrame.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                viewer.configure();
                return;
            }
        });
        panel.add(DocaSmear);
        
        
        frame.add(panel, BorderLayout.PAGE_END);
        //frame.add(IneffButton, BorderLayout.PAGE_END);
        

    }
}
