package weka.gui.explorer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import weka.core.Instances;
import weka.core.converters.Loader;
import weka.estimators.MultivariateEstimator;
import weka.estimators.vines.RegularVine;
import weka.gui.GenericObjectEditor;
import weka.gui.Logger;
import weka.gui.PropertyPanel;
import weka.gui.ResultHistoryPanel;
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

	/** Data */
	protected Instances m_Instances;

	/** Logging object */
	protected Logger m_Log;

	/** A thread that runs the estimation */
	protected Thread m_RunThread;

	/** The loader used to load the user-supplied test set (if any). */
	protected Loader m_TestLoader;

	/** Lets the user configure the estimator. */
	protected GenericObjectEditor m_EstimatorEditor = new GenericObjectEditor();

	/** The panel showing the current estimator selection. */
	protected PropertyPanel m_CEPanel = new PropertyPanel(m_EstimatorEditor);

	/** Click to set test mode to cross-validation. */
	protected JRadioButton m_CVBut = new JRadioButton("Cross-validation");

	/** Click to set test mode to generate a % split. */
	protected JRadioButton m_PercentBut = new JRadioButton("Percentage split");

	/** Click to set test mode to test on training data. */
	protected JRadioButton m_TrainBut = new JRadioButton("Use training set");

	/** Click to set test mode to a user-specified test set. */
	protected JRadioButton m_TestSplitBut = new JRadioButton("Supplied test set");

	/** The button used to open a separate test dataset. */
	protected JButton m_SetTestBut = new JButton("Set...");

	/** Label by where the cv folds are entered. */
	protected JLabel m_CVLab = new JLabel("Folds", SwingConstants.RIGHT);

	/** The field where the cv folds are entered. */
	protected JTextField m_CVText = new JTextField("10", 3);

	/** Label by where the % split is entered. */
	protected JLabel m_PercentLab = new JLabel("%", SwingConstants.RIGHT);

	/** The field where the % split is entered. */
	protected JTextField m_PercentText = new JTextField("66", 3);

	/** Click to start running the classifier. */
	protected JButton m_StartBut = new JButton("Start");

	/** Click to stop a running classifier. */
	protected JButton m_StopBut = new JButton("Stop");

	/** The output area for classification results. */
	protected JTextArea m_OutText = new JTextArea(20, 40);

	/** A panel controlling results viewing. */
	protected ResultHistoryPanel m_History = new ResultHistoryPanel(m_OutText);
	
	/** Button for further output/visualize options. */
	protected JButton m_CopulaOptions = new JButton("Select copulas...");

	/**
	 * Constructor
	 */
	public RVinesPanel() {
		// GUI Structure
		JPanel p1 = new JPanel();
		p1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Estimator"),
				BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		p1.setLayout(new BorderLayout());
		p1.add(m_CEPanel, BorderLayout.NORTH);

		JPanel p2 = new JPanel();
		GridBagLayout gbL = new GridBagLayout();
		p2.setLayout(gbL);
		p2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Test options"),
				BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		GridBagConstraints gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.WEST;
		gbC.gridy = 0;
		gbC.gridx = 0;
		gbL.setConstraints(m_TrainBut, gbC);
		p2.add(m_TrainBut);

		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.WEST;
		gbC.gridy = 1;
		gbC.gridx = 0;
		gbL.setConstraints(m_TestSplitBut, gbC);
		p2.add(m_TestSplitBut);

		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.EAST;
		gbC.fill = GridBagConstraints.HORIZONTAL;
		gbC.gridy = 1;
		gbC.gridx = 1;
		gbC.gridwidth = 2;
		gbC.insets = new Insets(2, 10, 2, 0);
		gbL.setConstraints(m_SetTestBut, gbC);
		p2.add(m_SetTestBut);

		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.WEST;
		gbC.gridy = 2;
		gbC.gridx = 0;
		gbL.setConstraints(m_CVBut, gbC);
		p2.add(m_CVBut);

		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.EAST;
		gbC.fill = GridBagConstraints.HORIZONTAL;
		gbC.gridy = 2;
		gbC.gridx = 1;
		gbC.insets = new Insets(2, 10, 2, 10);
		gbL.setConstraints(m_CVLab, gbC);
		p2.add(m_CVLab);

		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.EAST;
		gbC.fill = GridBagConstraints.HORIZONTAL;
		gbC.gridy = 2;
		gbC.gridx = 2;
		gbC.weightx = 100;
		gbC.ipadx = 20;
		gbL.setConstraints(m_CVText, gbC);
		p2.add(m_CVText);

		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.WEST;
		gbC.gridy = 3;
		gbC.gridx = 0;
		gbL.setConstraints(m_PercentBut, gbC);
		p2.add(m_PercentBut);

		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.EAST;
		gbC.fill = GridBagConstraints.HORIZONTAL;
		gbC.gridy = 3;
		gbC.gridx = 1;
		gbC.insets = new Insets(2, 10, 2, 10);
		gbL.setConstraints(m_PercentLab, gbC);
		p2.add(m_PercentLab);

		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.EAST;
		gbC.fill = GridBagConstraints.HORIZONTAL;
		gbC.gridy = 3;
		gbC.gridx = 2;
		gbC.weightx = 100;
		gbC.ipadx = 20;
		gbL.setConstraints(m_PercentText, gbC);
		p2.add(m_PercentText);

		gbC = new GridBagConstraints();
	    gbC.anchor = GridBagConstraints.WEST;
	    gbC.fill = GridBagConstraints.HORIZONTAL;
	    gbC.gridy = 4;
	    gbC.gridx = 0;
	    gbC.weightx = 100;
	    gbC.gridwidth = 3;
	    gbC.insets = new Insets(3, 0, 1, 0);
	    gbL.setConstraints(m_CopulaOptions, gbC);
	    p2.add(m_CopulaOptions);

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2));
		JPanel ssButs = new JPanel();
		ssButs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		ssButs.setLayout(new GridLayout(1, 2, 5, 5));
		ssButs.add(m_StartBut);
		ssButs.add(m_StopBut);

		buttons.add(ssButs);
		
		JPanel historyHolder = new JPanel(new BorderLayout());
		historyHolder.setBorder(BorderFactory.createTitledBorder("Result list (right-click for options)"));
		historyHolder.add(m_History, BorderLayout.CENTER);
		m_EstimatorEditor.setClassType(MultivariateEstimator.class);
		m_EstimatorEditor.setValue(new RegularVine());

		JPanel p3 = new JPanel();
		p3.setBorder(BorderFactory.createTitledBorder("Estimator output"));
		p3.setLayout(new BorderLayout());
		final JScrollPane js = new JScrollPane(m_OutText);
		p3.add(js, BorderLayout.CENTER);
		js.getViewport().addChangeListener(new ChangeListener() {
			private int lastHeight;

			@Override
			public void stateChanged(ChangeEvent e) {
				JViewport vp = (JViewport) e.getSource();
				int h = vp.getViewSize().height;
				if (h != lastHeight) { // i.e. an addition not just a user
										// scrolling
					lastHeight = h;
					int x = h - vp.getExtentSize().height;
					vp.setViewPosition(new Point(0, x));
				}
			}
		});

		JPanel mondo = new JPanel();
		gbL = new GridBagLayout();
		mondo.setLayout(gbL);
		gbC = new GridBagConstraints();
		// gbC.anchor = GridBagConstraints.WEST;
		gbC.fill = GridBagConstraints.HORIZONTAL;
		gbC.gridy = 0;
		gbC.gridx = 0;
		gbL.setConstraints(p2, gbC);
		mondo.add(p2);
		gbC = new GridBagConstraints();
		gbC.anchor = GridBagConstraints.NORTH;
		gbC.fill = GridBagConstraints.HORIZONTAL;
		gbC.gridy = 1;
		gbC.gridx = 0;
		gbL.setConstraints(buttons, gbC);
		mondo.add(buttons);
		gbC = new GridBagConstraints();
		// gbC.anchor = GridBagConstraints.NORTH;
		gbC.fill = GridBagConstraints.BOTH;
		gbC.gridy = 2;
		gbC.gridx = 0;
		gbC.weightx = 0;
		gbL.setConstraints(historyHolder, gbC);
		mondo.add(historyHolder);
		gbC = new GridBagConstraints();
		gbC.fill = GridBagConstraints.BOTH;
		gbC.gridy = 0;
		gbC.gridx = 1;
		gbC.gridheight = 3;
		gbC.weightx = 100;
		gbC.weighty = 100;
		gbL.setConstraints(p3, gbC);
		mondo.add(p3);

		setLayout(new BorderLayout());
		add(p1, BorderLayout.NORTH);
		add(mondo, BorderLayout.CENTER);
		
		// Availabilities
		
		/*
		 * Training set selection not enabled since there is no method for
		 * Multivariate Density Estimation yet.
		 */
		m_TestSplitBut.setEnabled(false);
		m_SetTestBut.setEnabled(false);
		m_CVBut.setEnabled(false);
		m_CVText.setEnabled(false);
		
		/*
		 * Selection not enabled since there is no suitable Interface for
		 * Multivariate Density Estimation yet.
		 */
		m_CEPanel.setEnabled(false);
		
		m_OutText.setEditable(false);

		// Action Listeners

		m_StartBut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean proceed = true;
				if (Explorer.m_Memory.memoryIsLow()) {
					proceed = Explorer.m_Memory.showMemoryIsLow();
				}
				if (proceed) {
					start();
				}
			}
		});
		m_StopBut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});
	}

	/**
	 * Starts running the currently configured estimator with the current
	 * settings. This is run in a separate thread, and will only start if there
	 * is no estimator already running. The estimator output is sent to the
	 * results history panel.
	 */
	protected void start() {

		if (m_RunThread == null) {
			synchronized (this) {
				m_StartBut.setEnabled(false);
				m_StopBut.setEnabled(true);
			}
			m_RunThread = new Thread() {
				@Override
				public void run() {
					m_CEPanel.addToHistory();

					// Copy the current state of things
					m_Log.statusMessage("Setting up...");
					Instances inst = new Instances(m_Instances);

					boolean outputSummary = true;

					int testMode = 0;
					int numFolds = 10;
					double percent = 66;
					MultivariateEstimator estimator = (MultivariateEstimator) m_EstimatorEditor.getValue();
					MultivariateEstimator template = null;

					StringBuffer outBuff = new StringBuffer();
					String name = (new SimpleDateFormat("HH:mm:ss - ")).format(new Date());
					String cname = "";
					String cmd = "";
					
					try {
			            if (m_CVBut.isSelected()) {
			              testMode = 1;
			              numFolds = Integer.parseInt(m_CVText.getText());
			              if (numFolds <= 1) {
			                throw new Exception("Number of folds must be greater than 1");
			              }
			            } else if (m_PercentBut.isSelected()) {
			              testMode = 2;
			              percent = Double.parseDouble(m_PercentText.getText());
			              if ((percent <= 0) || (percent >= 100)) {
			                throw new Exception("Percentage must be between 0 and 100");
			              }
			            } else if (m_TrainBut.isSelected()) {
			              testMode = 3;
			            } else if (m_TestSplitBut.isSelected()) {
			              testMode = 4;
			              throw new Exception("Test mode actually not implemented.");
			            } else {
			              throw new Exception("Unknown test mode");
			            }
			            
			            
					}catch(Exception e){
						
					}
				}
			};
			m_RunThread.setPriority(Thread.MIN_PRIORITY);
			m_RunThread.start();
		}
	}

	private void stop() {
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
		m_Instances = inst;

		double[][] data = RegularVine.transform(inst);
		boolean correct = RegularVine.testData(data);

		if (correct) {
			m_StartBut.setEnabled(m_RunThread == null);
			m_StopBut.setEnabled(m_RunThread != null);
		} else {
			m_StartBut.setEnabled(false);
			m_StopBut.setEnabled(false);
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
		this.m_Log = newLog;
	}

	/**
	 * Tests out the RVines panel from the command line.
	 * 
	 * @param args
	 *            may optionally contain the name of a dataset to load.
	 */
	public static void main(String[] args) {
		try {
			final javax.swing.JFrame jf = new javax.swing.JFrame("Weka Explorer: Estimators");
			jf.getContentPane().setLayout(new BorderLayout());
			final RVinesPanel sp = new RVinesPanel();
			jf.getContentPane().add(sp, BorderLayout.CENTER);
			weka.gui.LogPanel lp = new weka.gui.LogPanel();
			sp.setLog(lp);
			jf.getContentPane().add(lp, BorderLayout.SOUTH);
			jf.addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(java.awt.event.WindowEvent e) {
					jf.dispose();
					System.exit(0);
				}
			});
			jf.pack();
			jf.setSize(800, 600);
			jf.setVisible(true);
			if (args.length == 1) {
				System.err.println("Loading instances from " + args[0]);
				java.io.Reader r = new java.io.BufferedReader(new java.io.FileReader(args[0]));
				Instances i = new Instances(r);
				sp.setInstances(i);
			}
			if (args.length == 0) {
				args = new String[] { "./src/main/data/daxreturns.arff" };
				System.err.println("Loading instances from " + args[0]);
				java.io.Reader r = new java.io.BufferedReader(new java.io.FileReader(args[0]));
				Instances i = new Instances(r);
				sp.setInstances(i);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex.getMessage());
		}
	}
}