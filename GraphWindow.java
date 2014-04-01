import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

public class GraphWindow extends JFrame {
	private static final long serialVersionUID = 1817162240395467260L;
	private DCMainWindow parent;
	private ArrayList<RDCSet> sets;
	private ArrayList<RDCSeries> allSeries, activeSeries;
	int curSet;
	
	private float [][] data;
	JPanel graphPane, controlsPane;
	JLabel typeName;
	JButton prevType, nextType;
	ChartPanel chartPane;
	JCheckBox cbUseAll;

	public GraphWindow(DCMainWindow parent) {
		super("Graphs");
		
		this.parent = parent;
		curSet = 0;
		
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		// Position the window to the right of the parent window
		Rectangle parentBounds = parent.getBounds();		
		setBounds(parentBounds.x + parentBounds.width + 40, parentBounds.y - 50, 400, 400);
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		controlsPane = new JPanel();		
		controlsPane.setLayout(new FlowLayout());
		
		cbUseAll = new JCheckBox("Show all RDC sets");
		cbUseAll.setSelected(true);
		cbUseAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(cbUseAll.isSelected()) {
					prevType.setEnabled(false);
					nextType.setEnabled(false);
				} else {
					prevType.setEnabled(true);
					nextType.setEnabled(true);
				}
				
				redrawChart();
			}
		});
		controlsPane.add(cbUseAll);

		// Try to load arrow images to use on next/previous buttons
		try {
			BufferedImage leftArrowImage = ImageIO.read(new File("dcgui_left_arrow.png"));
			ImageIcon leftArrowIcon = new ImageIcon(leftArrowImage);
			BufferedImage rightArrowImage = ImageIO.read(new File("dcgui_right_arrow.png"));
			ImageIcon rightArrowIcon = new ImageIcon(rightArrowImage);
			
			prevType = new JButton(leftArrowIcon);
			nextType = new JButton(rightArrowIcon);

		} catch(IOException ex) {
			// If load fails then just make the buttons show + and -
			prevType = new JButton("-");
			nextType = new JButton("+");
		}

		Dimension buttonDim = new Dimension(30, 30);
		prevType.setPreferredSize(buttonDim);
		prevType.setEnabled(false);
		prevType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(curSet > 0)
					curSet--;
				else
					curSet = sets.size() - 1;
				
				typeName.setText(sets.get(curSet).getTypeString());
				redrawChart();
			}
		});
		controlsPane.add(prevType);
		
		typeName = new JLabel("NH");
		typeName.setPreferredSize(new Dimension(50, 30));
		typeName.setHorizontalAlignment(SwingConstants.CENTER);
		controlsPane.add(typeName);

		nextType.setPreferredSize(buttonDim);
		nextType.setEnabled(false);
		nextType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(curSet < sets.size() - 1)
					curSet++;
				else
					curSet = 0;
				
				typeName.setText(sets.get(curSet).getTypeString());
				redrawChart();
			}
		});
		controlsPane.add(nextType);
		
		JPanel contentPane = (JPanel)getContentPane();
	//	graphPane = new JPanel();
		//pane.setBounds(0, 0, 300, 50);
	//	graphPane.setMaximumSize(new Dimension(200, 200));
	//	graphPane.setBackground(Color.WHITE);
		
		contentPane.add(controlsPane);
		contentPane.add(Box.createRigidArea(new Dimension(20, 0)));
		
	//	contentPane.add(graphPane);
	}
	
	public void setRDCSets(ArrayList<RDCSet> sets) {
		this.sets = sets;
	}
	
	// Called to prepare all the XYSeries data to be later used in plotting
	private void createSeriesList() {
		allSeries = new ArrayList<RDCSeries>();
		
		// Fill the data array
		for(int i=0; i<sets.size(); i++) {
			RDCSet set = sets.get(i);
		    RDCSeries series = new RDCSeries(set.getTypeString());

			for(int j=0; j<set.getCount(); j++) {
				RDC rdc = set.get(j);
				if(rdc.isUsed && rdc.wasPredicted) {
					String label = String.format("%s res %d: delta %.3f Hz", set.getTypeString(), rdc.resNum, rdc.predValue-rdc.value);
					series.add(rdc.predValue, rdc.value, label);
				}
			}

			allSeries.add(series);
		}

	}
	
	public void refresh() {
		curSet = 0;
		typeName.setText(sets.get(0).getTypeString());
		
 		createSeriesList();
		redrawChart();
	}
	
	private void redrawChart() {
		JPanel contentPane = (JPanel)getContentPane();
	//	if(chartPane != null)
	//		contentPane.remove(chartPane);
		
		if(cbUseAll.isSelected()) {
 			activeSeries = allSeries;
 		} else {
 			activeSeries = new ArrayList<RDCSeries>();
 			activeSeries.add(allSeries.get(curSet));
 		}
		
		if(chartPane != null)
			chartPane.setChart(drawCorrelation());
		else {
			chartPane = new ChartPanel(drawCorrelation());
			contentPane.add(chartPane);
		}
	}

 	private JFreeChart drawCorrelation() {
        XYSeriesCollection dataCollection = new XYSeriesCollection();
        
 		// Fill the data array
 		for(int i=0; i<activeSeries.size(); i++) {
 			XYSeries series = activeSeries.get(i);
 			dataCollection.addSeries(series);
 		}

        JFreeChart jfreechart = ChartFactory.createScatterPlot(
                null, "Predicted", "Observed", dataCollection,
                PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
      //  xyPlot.setDomainCrosshairVisible(true);
      //  xyPlot.setRangeCrosshairVisible(true);
        xyPlot.setRangeZeroBaselineVisible(true);
        xyPlot.setDomainZeroBaselineVisible(true);
        
        XYItemRenderer renderer = xyPlot.getRenderer();
        
        XYToolTipGenerator toolTipGen = new XYToolTipGenerator() {
            @Override
            public String generateToolTip(XYDataset dataSet, int serIndex, int index) {
            	if(serIndex >= activeSeries.size()) return null; // The case of diagonal line            	
            	RDCSeries series = activeSeries.get(serIndex);
            	return series.getLabel(dataSet.getXValue(serIndex, index), dataSet.getYValue(serIndex, index));
            }
         };

         renderer.setBaseToolTipGenerator(toolTipGen);
         
        //    Shape cross = new Ellipse2D.Double(0,0,5,5); //ShapeUtilities.createDiagonalCross(3, 1);
        if(activeSeries.size() == 1) {
 			Shape shape = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[curSet];
 			Paint paint = DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[curSet];
 			
 			renderer.setSeriesShape(0, shape);
 			renderer.setSeriesPaint(0, paint);			
        } else {
	 		for(int i=0; i<activeSeries.size(); i++) {
	 			Shape shape = DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE[i];
	 			Paint paint = DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[i];
	 			
	 			renderer.setSeriesShape(i, shape);
	 			renderer.setSeriesPaint(i, paint);
	 		}
        }

        // Set up the y=x line
        double min = Math.min(dataCollection.getRangeLowerBound(true), dataCollection.getDomainLowerBound(true));
        double max = Math.max(dataCollection.getRangeUpperBound(true), dataCollection.getDomainUpperBound(true));
        XYSeries diagonal = new XYSeries("diagonal");
        diagonal.add(min, min);
        diagonal.add(max, max);

        int diagonalIndex = activeSeries.size();
        XYItemRenderer diagRenderer = new XYLineAndShapeRenderer(true, false);   // Lines only
        diagRenderer.setBasePaint(Color.black);
        diagRenderer.setSeriesPaint(0, Color.black);
        xyPlot.setDataset(diagonalIndex, new XYSeriesCollection(diagonal));
        xyPlot.setRenderer(diagonalIndex, diagRenderer);

        return jfreechart;
	}


	class RDCSeries extends XYSeries
	{
		private static final long serialVersionUID = 2827998698044419507L;
		private ArrayList<String> labels;
	    private HashMap<String, String> labelsMap;
	    
		public RDCSeries(Comparable<?> key) {
			super(key);
			labels = new ArrayList<String>();
			labelsMap = new HashMap<String, String>();
		}
		
		public void add(double x, double y, String label) {
			super.add(x, y);
			labels.add(label);
			labelsMap.put(String.format("%.3f, %.3f", x, y), label);
		}
		
	    public String getLabel (int index) {
	        return labels.get(index);
	    }
	    
	    public String getLabel (double x, double y) {
	    	String result = labelsMap.get(String.format("%.3f, %.3f", x, y));
	        return result;
	    }

	    public ArrayList<String> getLabels() {
	    	return labels;
	    }
	}
}
