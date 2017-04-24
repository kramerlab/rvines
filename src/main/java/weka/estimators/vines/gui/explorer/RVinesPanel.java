package weka.estimators.vines.gui.explorer;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.estimators.vines.RegularVine;
import weka.gui.LogPanel;
import weka.gui.Logger;
import weka.gui.WekaTaskMonitor;
import weka.gui.explorer.Explorer;
import weka.gui.explorer.Explorer.ExplorerPanel;
import weka.gui.explorer.Explorer.LogHandler;

/**
 * GUI class for regular vines.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class RVinesPanel extends JPanel implements ExplorerPanel, LogHandler {
	private static final long serialVersionUID = 3202605931532786777L;

	/** RVine object */
	protected RegularVine rvine;

	/** Logging object */
	protected Logger m_log;
	
	/** Panel objects */
	JLabel top;
	
	/**
	 * Constructor
	 */
	public RVinesPanel() {
		setLayout(new BorderLayout());
		rvine = new RegularVine();
		
		top = new JLabel();
		top.setHorizontalAlignment(JLabel.CENTER);
		
		add(top, BorderLayout.PAGE_START);
	}

	/**
	 * Unused
	 */
	@Override
	public void setExplorer(Explorer parent) {
	}

	/**
	 * Unused - just returns null
	 * 
	 * @return null
	 */
	@Override
	public Explorer getExplorer() {
		return null;
	}

	/**
	 * Set the working instances for this panel. Passes the instances on to the
	 * wrapped ForecastingPanel
	 * 
	 * @param inst
	 *            the instances to use
	 */
	@Override
	public void setInstances(Instances inst) {
		double[][] data = RegularVine.transform(inst);
		if(RegularVine.testData(data)){
			top.setForeground(new Color(0, 255, 0));
			top.setText("Data is ok!");
		}else{
			top.setForeground(new Color(255, 0, 0));
			top.setText("Data does not fit the [0, 1] interval! Please use preprocessing first!");
		}
	}

	/**
	 * Get the title for this tab
	 * 
	 * @return the title for this tab
	 */
	@Override
	public String getTabTitle() {
		return "RVines";
	}

	/**
	 * Get the tool tip for this tab
	 * 
	 * @return the tool tip for this tab
	 */
	@Override
	public String getTabTitleToolTip() {
		return "Build and evaluate regular vine models.";
	}

	/**
	 * Set the logging object to use
	 * 
	 * @param newLog
	 *            the log to use
	 */
	@Override
	public void setLog(Logger newLog) {
		this.m_log = newLog;
	}

	/**
	 * Main method for testing this class. Expects the path to an ARFF file as
	 * an argument
	 * 
	 * @param args an array of command line arguments
	 */
	public static void main(String[] args) {
		try {
			DataSource source = new DataSource("./src/main/data/daxreturns.arff");
			Instances insts = source.getDataSet();

			final RVinesPanel pan = new RVinesPanel();
			pan.setInstances(insts);
			pan.setLog(new LogPanel(new WekaTaskMonitor()));

			final JFrame frame = new JFrame("RVines");
			frame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					frame.dispose();
					System.exit(1);
				}
			});
			frame.setSize(800, 600);
			frame.setContentPane(pan);
			frame.setVisible(true);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}