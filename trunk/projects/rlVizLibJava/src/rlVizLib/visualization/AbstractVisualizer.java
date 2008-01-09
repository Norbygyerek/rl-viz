/* RL-VizLib, a library for C++ and Java for adding advanced visualization and dynamic capabilities to RL-Glue.
* Copyright (C) 2007, Brian Tanner brian@tannerpages.com (http://brian.tannerpages.com/)
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */
package rlVizLib.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public abstract class AbstractVisualizer implements ImageAggregator {

    private BufferedImage productionEnvImage = null;
    private BufferedImage bufferEnvImage = null;
    private VisualizerPanelInterface parentPanel = null;
    private Vector<ThreadRenderObject> threadRunners = new Vector<ThreadRenderObject>();
    private Vector<Thread> theThreads = new Vector<Thread>();
    private Vector<Point2D> positions = new Vector<Point2D>();
    private Vector<Point2D> sizes = new Vector<Point2D>();
    volatile boolean currentlyRunning = false;

    public void setParentPanel(VisualizerPanelInterface parentPanel) {
        this.parentPanel = parentPanel;
    }

    public void notifyPanelSizeChange() {
        resizeImages();
    }

    private synchronized void resizeImages() {
        Dimension currentVisualizerPanelSize = parentPanel.getSize();

        productionEnvImage = new BufferedImage((int) currentVisualizerPanelSize.getWidth(), (int) currentVisualizerPanelSize.getHeight(), BufferedImage.TYPE_INT_ARGB);
        bufferEnvImage = new BufferedImage((int) currentVisualizerPanelSize.getWidth(), (int) currentVisualizerPanelSize.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < threadRunners.size(); i++) {
            threadRunners.get(i).receiveSizeChange(makeSizeForVizComponent(i));
        }

    }

    public AbstractVisualizer() {
    }

    public BufferedImage getLatestImage() {
        return productionEnvImage;
    }

    private synchronized void redrawCurrentImage() {
        synchronized (redrawLock) {
            scheduled = false;
            drawing = true;
        }

        Graphics2D G = bufferEnvImage.createGraphics();
        Color myClearColor = new Color(0.0f, 0.0f, 0.0f, 0.0f);
        G.setColor(myClearColor);
        G.setBackground(myClearColor);
        G.clearRect(0, 0, bufferEnvImage.getWidth(), bufferEnvImage.getHeight());

        for (int i = 0; i < threadRunners.size(); i++) {
            ThreadRenderObject thisRunner = threadRunners.get(i);

            Dimension position = makeLocationForVizComponent(i);

            G.drawImage(thisRunner.getProductionImage(), position.width, position.height, null);
        }

        BufferedImage tmpImage = productionEnvImage;
        productionEnvImage = bufferEnvImage;
        bufferEnvImage = tmpImage;

        synchronized (redrawLock) {
            lastDrawTime = System.currentTimeMillis();
            drawing = false;
        }


        parentPanel.receiveNotificationVizChanged();


    }
    long lastDrawTime = 0;
    volatile boolean scheduled = false;
    boolean drawing = false;
    Object redrawLock = new Object();
    volatile Timer redrawTimer = new Timer();

    public void receiveNotification() {
        final int timeBetweenDraw = 43;
        //Either draw now or schedule a drawing

        synchronized (redrawLock) {
            long now = System.currentTimeMillis();
            //If we're planning on drawing but haven't started, relax
            if (scheduled) {
                return;
            }
            //If we're not scheduled but currently drawing, schedule at the interval
            if (drawing) {
                scheduled = true;
                redrawTimer.schedule(new TimerTask() {

                            public void run() {
                                redrawCurrentImage();
                            }
                        }, timeBetweenDraw);
                return;
            }
            //Ok so in this case, we're !scheduled && !drawing
                //Schedule a drawing in the shorter term
            long timeSinceDraw = now - lastDrawTime;
            long timeTillDraw = timeBetweenDraw - timeSinceDraw;
            if (timeTillDraw < 0) {
                timeTillDraw = 0;
            }

            redrawTimer.schedule(new TimerTask() {

                        public void run() {
                            redrawCurrentImage();
                        }
                    }, timeTillDraw);
        }
    //
//            if(now>lastDrawTime+43){
//		//One of the guys I draw has updates
//		redrawCurrentImage();
//            }else{
//                if(redrawTimer==null){
//		Timer redrawTimer= new Timer();
//		redrawTimer.schedule(new TimerTask() {
//		            public void run() {
//		            	redrawCurrentImage();
//		            }
//		        }, now-lastDrawTime);		
//            }
//                        
//}   
    }

    public void startVisualizing() {
        synchronized (startStopSynchObject) {
        if (currentlyRunning||currentlyStarting) {
            return;
        }
        currentlyStarting = true;
        }
        for (ThreadRenderObject thisRunner : threadRunners) {
            Thread theThread = new Thread(thisRunner);
            theThreads.add(theThread);
            theThread.start();
        }
        
        synchronized (startStopSynchObject) {
            currentlyRunning=true;
            currentlyStarting=false;
        }

    }
    volatile boolean currentlyStopping = false;
    volatile boolean currentlyStarting = false;
    Object startStopSynchObject = new Object();

    public void stopVisualizing() {

        synchronized (startStopSynchObject) {
            if (!currentlyRunning || currentlyStopping) {
                return;
            }
            currentlyStopping = true;
            
            if(currentlyStarting){
                System.err.println("Got through all of the stopping conditions, we're going to stop... but its currently starting....ahhh");
            }
        }

        // tell them all to die
        for (ThreadRenderObject thisRunner : threadRunners) {
            thisRunner.kill();
        }

        // wait for them all to be done
        for (Thread thisThread : theThreads) {
            try {
                thisThread.join();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        theThreads.removeAllElements();

        synchronized (startStopSynchObject) {
            currentlyStopping = false;
            currentlyRunning = false;
        }
    }

    private Dimension makeSizeForVizComponent(int i) {
        Dimension currentVisualizerPanelSize = parentPanel.getSize();



        double width = currentVisualizerPanelSize.getWidth() - 10;
        double height = currentVisualizerPanelSize.getHeight() - 20;

        double scaledWidth = width * sizes.get(i).getX();
        double scaledHeight = height * sizes.get(i).getY();

        Dimension d = new Dimension();
        d.setSize(scaledWidth, scaledHeight);

        return d;
    }

    private Dimension makeLocationForVizComponent(int i) {
        Dimension currentVisualizerPanelSize = parentPanel.getSize();

        double width = currentVisualizerPanelSize.getWidth() - 10;
        double height = currentVisualizerPanelSize.getHeight() - 20;

        double startX = 5 + width * positions.get(i).getX();
        double startY = 16 + height * positions.get(i).getY();

        Dimension d = new Dimension();
        d.setSize(startX, startY);

        return d;
    }

    //All of these should be between 0 and 1

    public void addVizComponentAtPositionWithSize(VizComponent newComponent, double xPos, double yPos, double width, double height) {
        threadRunners.add(new ThreadRenderObject(new Dimension(200, 200), newComponent, this));
        positions.add(new Point2D.Double(xPos, yPos));
        sizes.add(new Point2D.Double(width, height));

    }

    public boolean isCurrentlyRunning() {
        return currentlyRunning;
    }

    public String getName() {
        return "Name not implemented";
    }
}