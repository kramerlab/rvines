package weka.gui.explorer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.SerializationHelper;
import weka.core.SerializedObject;
import weka.core.Utils;
import weka.core.converters.Loader;
import weka.estimators.DensityEstimator;
import weka.estimators.vines.RegularVine;
import weka.estimators.vines.copulas.Copula;
import weka.estimators.vines.VineUtils;
import weka.gui.GenericObjectEditor;
import weka.gui.Logger;
import weka.gui.PropertyPanel;
import weka.gui.ResultHistoryPanel;
import weka.gui.SaveBuffer;
import weka.gui.TaskLogger;
import weka.gui.explorer.Explorer;
import weka.gui.explorer.Explorer.ExplorerPanel;
import weka.gui.explorer.Explorer.LogHandler;
import weka.gui.ExtensionFileFilter;

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

	/** Lets the user configure the rvine. */
	protected GenericObjectEditor m_RVineEditor = new GenericObjectEditor();

	/** The panel showing the current rvine selection. */
	protected PropertyPanel m_CEPanel = new PropertyPanel(m_RVineEditor);

	/** Click to set test mode to cross-validation. */
	protected JRadioButton m_CVBut = new JRadioButton("Cross-validation");

	/** Click to set test mode to generate a % split. */
	protected JRadioButton m_PercentBut = new JRadioButton("Percentage split");

	/** Click to set test mode to test on training data. */
	protected JRadioButton m_TrainBut = new JRadioButton("Use training set");

	/** Click to set test mode to a user-specified test set. */
	protected JRadioButton m_TestSplitBut = new JRadioButton(
			"Supplied test set");

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

	/** Click to start running the rvine. */
	protected JButton m_StartBut = new JButton("Start");

	/** Click to stop a running rvine. */
	protected JButton m_StopBut = new JButton("Stop");

	/** The output area for rvine results. */
	protected JTextArea m_OutText = new JTextArea(20, 40);

	/** A panel controlling results viewing. */
	protected ResultHistoryPanel m_History = new ResultHistoryPanel(m_OutText);

	/** Button for further output/visualize options. */
	protected JButton m_CopulaOptions = new JButton("Select copulas...");

	/** The buffer saving object for saving output. */
	SaveBuffer m_SaveOut = new SaveBuffer(m_Log, this);

	/** The filename extension that should be used for model files. */
	public static String MODEL_FILE_EXTENSION = ".model";

	/** The file chooser for selecting model files. */
	protected JFileChooser m_FileChooser = new JFileChooser(new File(
			System.getProperty("user.dir")));

	/** Filter to ensure only model files are selected. */
	protected FileFilter m_ModelFilter = new ExtensionFileFilter(
			MODEL_FILE_EXTENSION, "Model object files");

	/**
	 * Alters the enabled/disabled status of elements associated with each radio
	 * button.
	 */
	ActionListener m_RadioListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			updateRadioLinks();
		}
	};

	/**
	 * Constructor
	 */
	public RVinesPanel() {
		// GUI Structure
		JPanel p1 = new JPanel();
		p1.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Estimator"),
				BorderFactory.createEmptyBorder(0, 5, 5, 5)));
		p1.setLayout(new BorderLayout());
		p1.add(m_CEPanel, BorderLayout.NORTH);

		JPanel p2 = new JPanel();
		GridBagLayout gbL = new GridBagLayout();
		p2.setLayout(gbL);
		p2.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Test options"),
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

		m_CopulaOptions.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				RegularVine rvine = (RegularVine) m_RVineEditor.getValue();
				String cname = rvine.getClass().getName();

				copulaFrame(rvine, cname + " :: Copula Selection");
			}
		});

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2));
		JPanel ssButs = new JPanel();
		ssButs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		ssButs.setLayout(new GridLayout(1, 2, 5, 5));
		ssButs.add(m_StartBut);
		ssButs.add(m_StopBut);

		buttons.add(ssButs);

		JPanel historyHolder = new JPanel(new BorderLayout());
		historyHolder.setBorder(BorderFactory
				.createTitledBorder("Result list (right-click for options)"));
		historyHolder.add(m_History, BorderLayout.CENTER);
		m_RVineEditor.setClassType(DensityEstimator.class);
		m_RVineEditor.setValue(new RegularVine());

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

		m_CVBut.setSelected(false);
		m_PercentBut.setSelected(false);
		m_TrainBut.setSelected(true);
		m_TestSplitBut.setSelected(false);
		updateRadioLinks();
		ButtonGroup bg = new ButtonGroup();
		bg.add(m_TrainBut);
		bg.add(m_CVBut);
		bg.add(m_PercentBut);
		bg.add(m_TestSplitBut);

		m_TrainBut.addActionListener(m_RadioListener);
		m_CVBut.addActionListener(m_RadioListener);
		m_PercentBut.addActionListener(m_RadioListener);
		m_TestSplitBut.addActionListener(m_RadioListener);

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
				start();
			}
		});
		m_StopBut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stop();
			}
		});

		m_History.setHandleRightClicks(false);
		// see if we can popup a menu for the selected result
		m_History.getList().addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void mouseClicked(MouseEvent e) {
				if (((e.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK)
						|| e.isAltDown()) {
					int index = m_History.getList().locationToIndex(
							e.getPoint());
					if (index != -1) {
						List<String> selectedEls = (List<String>) m_History
								.getList().getSelectedValuesList();
						visualize(selectedEls, e.getX(), e.getY());
					} else {
						visualize(null, e.getX(), e.getY());
					}
				}
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

					int testMode = 0;
					int numFolds = 10;
					double percent = 66;
					RegularVine estimator = (RegularVine) m_RVineEditor
							.getValue();

					StringBuffer outBuff = new StringBuffer();
					String name = (new SimpleDateFormat("HH:mm:ss - "))
							.format(new Date());
					String cname = "";
					String cmd = "";
					double dens = 0;

					// for timing
					long trainTimeStart = 0, trainTimeElapsed = 0;
					long testTimeStart = 0, testTimeElapsed = 0;

					try {
						Instances train = inst;
						Instances test = new Instances(train);

						if (m_CVBut.isSelected()) {
							testMode = 1;
							numFolds = Integer.parseInt(m_CVText.getText());
							if (numFolds <= 1) {
								throw new Exception(
										"Number of folds must be greater than 1");
							}
							throw new Exception(
									"Test mode actually not implemented.");
						} else if (m_PercentBut.isSelected()) {
							testMode = 2;
							percent = Double.parseDouble(m_PercentText
									.getText());
							if ((percent <= 0) || (percent >= 100)) {
								throw new Exception(
										"Percentage must be between 0 and 100");
							}

							train = new Instances(inst, 0,
									(int) (inst.size() * percent / 100));
							test = new Instances(inst, train.size(),
									inst.size());

						} else if (m_TrainBut.isSelected()) {
							testMode = 3;
						} else if (m_TestSplitBut.isSelected()) {
							testMode = 4;
							throw new Exception(
									"Test mode actually not implemented.");
						} else {
							throw new Exception("Unknown test mode");
						}

						cname = estimator.getClass().getName();
						if (cname.startsWith("weka.estimators.")) {
							name += cname
									.substring("weka.estimators.".length());
						} else {
							name += cname;
						}
						cmd = estimator.getClass().getName();
						if (estimator instanceof OptionHandler) {
							cmd += " "
									+ Utils.joinOptions(((OptionHandler) estimator)
											.getOptions());
						}

						m_Log.logMessage("Started " + cname);
						m_Log.logMessage("Command: " + cmd);
						if (m_Log instanceof TaskLogger) {
							((TaskLogger) m_Log).taskStarted();
						}

						outBuff.append("=== Run information ===\n\n");
						outBuff.append("Scheme:       " + cname);
						if (estimator instanceof OptionHandler) {
							String[] o = ((OptionHandler) estimator)
									.getOptions();
							outBuff.append(" " + Utils.joinOptions(o));
						}
						outBuff.append("\n");
						outBuff.append("Relation:     " + inst.relationName()
								+ '\n');
						outBuff.append("Instances:    " + inst.numInstances()
								+ '\n');
						outBuff.append("Attributes:   " + inst.numAttributes()
								+ '\n');
						if (inst.numAttributes() < 100) {
							for (int i = 0; i < inst.numAttributes(); i++) {
								outBuff.append("              "
										+ inst.attribute(i).name() + '\n');
							}
						} else {
							outBuff.append("[list of attributes omitted]\n");
						}

						outBuff.append("Test mode:    ");
						switch (testMode) {
						case 3: // Test on training
							outBuff.append("evaluate on training data\n");
							break;
						case 1: // CV mode
							outBuff.append("" + numFolds
									+ "-fold cross-validation\n");
							break;
						case 2: // Percent split
							outBuff.append("split " + percent
									+ "% train, remainder test\n");
							break;
						case 4: // Test on user split
							// not implemented
							break;
						}

						m_History.addResult(name, outBuff);
						m_History.setSingle(name);

						switch (testMode) {
						case 3: // Test on training
							trainTimeStart = System.currentTimeMillis();
							estimator.buildEstimator(train);
							trainTimeElapsed = System.currentTimeMillis()
									- trainTimeStart;

							outBuff.append("=== Estimator model (full training set) ===\n\n");
							outBuff.append("\nTime taken to build model: "
									+ Utils.doubleToString(
											trainTimeElapsed / 1000.0, 2)
									+ " seconds\n\n");
							m_History.updateResult(name);

							m_Log.statusMessage("Evaluating on training data...");
							testTimeStart = System.currentTimeMillis();
							dens = estimator.logDensity(test);
							testTimeElapsed = System.currentTimeMillis()
									- testTimeStart;
							outBuff.append("=== Evaluation on training set ===\n");
							outBuff.append("Pseudo-Log-Density : " + dens
									+ "(sum) \n");
							outBuff.append("Test-Set Size : " + test.size()
									+ " \n");
							outBuff.append("\nTime taken to evaluate model: "
									+ Utils.doubleToString(
											testTimeElapsed / 1000.0, 2)
									+ " seconds\n\n");
							m_History.updateResult(name);
							break;

						case 2: // Percent split
							int trainSize = (int) Math.round(inst
									.numInstances() * percent / 100);
							int testSize = inst.numInstances() - trainSize;
							train = new Instances(inst,	0, trainSize);
							test = new Instances(inst, trainSize, testSize);
							m_Log.statusMessage("Building model on training split ("
									+ trainSize + " instances)...");

							trainTimeStart = System.currentTimeMillis();
							estimator.buildEstimator(train);
							trainTimeElapsed = System.currentTimeMillis()
									- trainTimeStart;

							outBuff.append("=== Estimator model (percent training set) ===\n\n");
							outBuff.append("\nTime taken to build model: "
									+ Utils.doubleToString(
											trainTimeElapsed / 1000.0, 2)
									+ " seconds\n\n");
							m_History.updateResult(name);

							m_Log.statusMessage("Evaluating on training data...");
							testTimeStart = System.currentTimeMillis();
							dens = estimator.logDensity(test);
							testTimeElapsed = System.currentTimeMillis()
									- testTimeStart;

							outBuff.append("=== Evaluation on test split ===\n");
							outBuff.append("Pseudo-Log-Density : " + dens
									+ "(sum) \n");
							outBuff.append("Test-Set Size : " + test.size()
									+ " \n");
							outBuff.append("\nTime taken to evaluate model: "
									+ Utils.doubleToString(
											testTimeElapsed / 1000.0, 2)
									+ " seconds\n\n");
							m_History.updateResult(name);
							break;
						default:
							throw new Exception("Test mode not implemented");
						}

						// copy full model for output
						SerializedObject so = new SerializedObject(estimator);
						RegularVine fullEstimator = (RegularVine) so
								.getObject();

						ArrayList<Object> vv = new ArrayList<Object>();

						vv.add(fullEstimator);
						Instances trainHeader = new Instances(m_Instances, 0);
						vv.add(trainHeader);
						m_History.addObject(name, vv);

					} catch (Exception e) {
						e.printStackTrace();
					}

					if (isInterrupted()) {
						m_Log.logMessage("Interrupted " + cname);
						m_Log.statusMessage("Interrupted");
					} else {
						m_Log.logMessage("Finished " + cname);
						m_Log.statusMessage("OK");
					}

					synchronized (this) {
						m_StartBut.setEnabled(true);
						m_StopBut.setEnabled(false);
						m_RunThread = null;
					}
					if (m_Log instanceof TaskLogger) {
						((TaskLogger) m_Log).taskFinished();
					}
				}
			};
			m_RunThread.setPriority(Thread.MIN_PRIORITY);
			m_RunThread.start();
		}
	}

	/**
	 * Stops the currently running rvine (if any).
	 */
	@SuppressWarnings("deprecation")
	protected void stop() {

		if (m_RunThread != null) {
			m_RunThread.interrupt();

			// This is deprecated (and theoretically the interrupt should do).
			m_RunThread.stop();
		}
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
	 * wrapped Panel
	 * 
	 * @param inst
	 *            the instances to use
	 */
	@Override
	public void setInstances(Instances inst) {
		m_Instances = inst;
		boolean correct = VineUtils.testData(inst);

		if (correct) {
			m_StartBut.setEnabled(m_RunThread == null);
			m_StopBut.setEnabled(m_RunThread != null);
		} else {
			m_StartBut.setEnabled(false);
			m_StopBut.setEnabled(false);
		}
	}

	/**
	 * Updates the enabled status of the input fields and labels.
	 */
	protected void updateRadioLinks() {
		m_SetTestBut.setEnabled(m_TestSplitBut.isSelected());
		// if ((m_SetTestFrame != null) && (!m_TestSplitBut.isSelected())) {
		// m_SetTestFrame.setVisible(false);
		// }
		m_CVText.setEnabled(m_CVBut.isSelected());
		m_CVLab.setEnabled(m_CVBut.isSelected());
		m_PercentText.setEnabled(m_PercentBut.isSelected());
		m_PercentLab.setEnabled(m_PercentBut.isSelected());
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
	 * Handles constructing a popup menu with visualization options.
	 * 
	 * @param names
	 *            the name of the result history list entry clicked on by the
	 *            user
	 * @param x
	 *            the x coordinate for popping up the menu
	 * @param y
	 *            the y coordinate for popping up the menu
	 */
	@SuppressWarnings("unchecked")
	protected void visualize(List<String> names, int x, int y) {
		final List<String> selectedNames = names;
		JPopupMenu resultListMenu = new JPopupMenu();

		JMenuItem visMainBuffer = new JMenuItem("View in main window");
		if (selectedNames != null && selectedNames.size() == 1) {
			visMainBuffer.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_History.setSingle(selectedNames.get(0));
				}
			});
		} else {
			visMainBuffer.setEnabled(false);
		}

		JMenuItem visSepBuffer = new JMenuItem("View in separate window");
		if (selectedNames != null && selectedNames.size() == 1) {
			visSepBuffer.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					m_History.openFrame(selectedNames.get(0));
				}
			});
		} else {
			visSepBuffer.setEnabled(false);
		}

		JMenuItem saveOutput = new JMenuItem("Save result buffer");
		if (selectedNames != null && selectedNames.size() == 1) {
			saveOutput.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveBuffer(selectedNames.get(0));
				}
			});
		} else {
			saveOutput.setEnabled(false);
		}

		JMenuItem deleteOutput = new JMenuItem("Delete result buffer(s)");
		if (selectedNames != null) {
			deleteOutput.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_History.removeResults(selectedNames);
				}
			});
		} else {
			deleteOutput.setEnabled(false);
		}

		JMenuItem loadModel = new JMenuItem("Load model");
		loadModel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				loadModel();
			}
		});

		ArrayList<Object> o = null;
		if (selectedNames != null && selectedNames.size() == 1) {
			o = (ArrayList<Object>) m_History.getNamedObject(selectedNames
					.get(0));
		}

		RegularVine temp_estimator = null;
		Instances temp_trainHeader = null;

		if (o != null) {
			for (int i = 0; i < o.size(); i++) {
				Object temp = o.get(i);
				if (temp instanceof RegularVine) {
					temp_estimator = (RegularVine) temp;
				} else if (temp instanceof Instances) { // training header
					temp_trainHeader = (Instances) temp;
				}
			}
		}

		final RegularVine estimator = temp_estimator;
		final Instances trainHeader = temp_trainHeader;

		JMenuItem saveModel = new JMenuItem("Save model");
		if (estimator != null && selectedNames != null
				&& selectedNames.size() == 1) {
			saveModel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					saveEstimator(selectedNames.get(0), estimator, trainHeader);
				}
			});
		} else {
			saveModel.setEnabled(false);
		}

		JMenuItem reApplyConfig = new JMenuItem(
				"Re-apply this model's configuration");
		if (estimator != null && selectedNames != null
				&& selectedNames.size() == 1) {
			reApplyConfig.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					m_RVineEditor.setValue(estimator);
				}
			});
		} else {
			reApplyConfig.setEnabled(false);
		}

		JMenuItem visSum = new JMenuItem("RVine Summary");
		if (estimator != null && selectedNames != null
				&& selectedNames.size() == 1) {
			visSum.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String name = selectedNames.get(0);
					openFrame(estimator.summary(), name + " :: RVine Summary");
				}
			});
		} else {
			visSum.setEnabled(false);
		}

		JMenuItem visGrph = new JMenuItem("Visualize tree");
		if (estimator != null && selectedNames != null
				&& selectedNames.size() == 1) {
			visGrph.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String name = selectedNames.get(0);
					final JFrame jf = new RVineVisualization(estimator, name
							+ " :: RVine Visualization");
					jf.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							jf.dispose();
						}
					});
					jf.pack();
					jf.setVisible(true);
				}
			});
		} else {
			visGrph.setEnabled(false);
		}

		JMenuItem visMat = new JMenuItem("RVine Matrix");
		if (estimator != null && selectedNames != null
				&& selectedNames.size() == 1) {
			visMat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String name = selectedNames.get(0);
					openFrame(matrixToString(estimator.getRVineMatrix2()), name
							+ " :: RVine Matrix");
				}
			});
		} else {
			visMat.setEnabled(false);
		}

		JMenuItem visFam = new JMenuItem("Family Matrix");
		if (estimator != null && selectedNames != null
				&& selectedNames.size() == 1) {
			visFam.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String name = selectedNames.get(0);
					openFrame(matrixToString(estimator.getFamilyMatrix()), name
							+ " :: Family Matrix");
				}
			});
		} else {
			visFam.setEnabled(false);
		}

		JMenuItem visPar = new JMenuItem("Parameter Matrices");
		if (estimator != null && selectedNames != null
				&& selectedNames.size() == 1) {
			visPar.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String name = selectedNames.get(0);
					openFrame(matrixToString(estimator.getParMatrices()[0]),
							name + " :: Par1 Matrix");
					openFrame(matrixToString(estimator.getParMatrices()[1]),
							name + " :: Par2 Matrix");
				}
			});
		} else {
			visPar.setEnabled(false);
		}

		JMenuItem visLogs = new JMenuItem("Log-Liks Matrix");
		if (estimator != null && selectedNames != null
				&& selectedNames.size() == 1) {
			visLogs.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String name = selectedNames.get(0);
					openFrame(matrixToString(estimator.getLogliksMatrix()),
							name + " :: Log-Liks Matrix");
				}
			});
		} else {
			visLogs.setEnabled(false);
		}

		JMenuItem visTau = new JMenuItem("Tau Matrix");
		if (estimator != null && selectedNames != null
				&& selectedNames.size() == 1) {
			visTau.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String name = selectedNames.get(0);
					openFrame(matrixToString(estimator.getTauMatrix()), name
							+ " :: Tau Matrix");
				}
			});
		} else {
			visTau.setEnabled(false);
		}

		JMenuItem visEmpTau = new JMenuItem("Empirical Tau Matrix");
		if (estimator != null && selectedNames != null
				&& selectedNames.size() == 1) {
			visEmpTau.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String name = selectedNames.get(0);
					openFrame(matrixToString(estimator.getEmpTauMatrix()), name
							+ " :: Emp Tau Matrix");
				}
			});
		} else {
			visEmpTau.setEnabled(false);
		}

		resultListMenu.add(visMainBuffer);
		resultListMenu.add(visSepBuffer);
		resultListMenu.add(saveOutput);
		resultListMenu.add(deleteOutput);
		resultListMenu.addSeparator();
		resultListMenu.add(loadModel);
		resultListMenu.add(saveModel);
		resultListMenu.add(reApplyConfig);
		resultListMenu.addSeparator();
		resultListMenu.add(visSum);
		resultListMenu.add(visGrph);
		resultListMenu.add(visMat);
		resultListMenu.add(visFam);
		resultListMenu.add(visPar);
		resultListMenu.add(visLogs);
		resultListMenu.add(visTau);
		resultListMenu.add(visEmpTau);
		resultListMenu.show(m_History.getList(), x, y);
	}

	/**
	 * Save the currently selected rvine output to a file.
	 * 
	 * @param name
	 *            the name of the buffer to save
	 */
	protected void saveBuffer(String name) {
		StringBuffer sb = m_History.getNamedBuffer(name);
		if (sb != null) {
			if (m_SaveOut.save(sb)) {
				m_Log.logMessage("Save successful.");
			}
		}
	}

	/**
	 * Loads a model.
	 */
	protected void loadModel() {

		m_FileChooser.setFileFilter(m_ModelFilter);
		int returnVal = m_FileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selected = m_FileChooser.getSelectedFile();
			RegularVine estimator = null;
			Instances trainHeader = null;

			m_Log.statusMessage("Loading model from file...");
			try {
				InputStream is = new FileInputStream(selected);

				if (selected.getName().endsWith(".gz")) {
					is = new GZIPInputStream(is);
				}
				// ObjectInputStream objectInputStream = new
				// ObjectInputStream(is);
				ObjectInputStream objectInputStream = SerializationHelper
						.getObjectInputStream(is);
				estimator = (RegularVine) objectInputStream.readObject();
				try { // see if we can load the header
					trainHeader = (Instances) objectInputStream.readObject();
				} catch (Exception e) {
				} // don't fuss if we can't
				objectInputStream.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e, "Load Failed",
						JOptionPane.ERROR_MESSAGE);
			}

			m_Log.statusMessage("OK");

			if (estimator != null) {
				m_Log.logMessage("Loaded model from file '"
						+ selected.getName() + "'");
				String name = (new SimpleDateFormat("HH:mm:ss - "))
						.format(new Date());
				String cname = estimator.getClass().getName();
				if (cname.startsWith("weka.classifiers.")) {
					cname = cname.substring("weka.classifiers.".length());
				}
				name += cname + " from file '" + selected.getName() + "'";
				StringBuffer outBuff = new StringBuffer();

				outBuff.append("=== Model information ===\n\n");
				outBuff.append("Filename:     " + selected.getName() + "\n");
				outBuff.append("Scheme:       "
						+ estimator.getClass().getName());
				if (estimator instanceof OptionHandler) {
					String[] o = ((OptionHandler) estimator).getOptions();
					outBuff.append(" " + Utils.joinOptions(o));
				}
				outBuff.append("\n");
				if (trainHeader != null) {
					outBuff.append("Relation:     "
							+ trainHeader.relationName() + '\n');
					outBuff.append("Attributes:   "
							+ trainHeader.numAttributes() + '\n');
					if (trainHeader.numAttributes() < 100) {
						for (int i = 0; i < trainHeader.numAttributes(); i++) {
							outBuff.append("              "
									+ trainHeader.attribute(i).name() + '\n');
						}
					} else {
						outBuff.append("              [list of attributes omitted]\n");
					}
				} else {
					outBuff.append("\nTraining data unknown\n");
				}

				outBuff.append("\n=== Estimator model ===\n\n");
				outBuff.append(estimator.toString() + "\n");

				m_History.addResult(name, outBuff);
				m_History.setSingle(name);
				ArrayList<Object> vv = new ArrayList<Object>();
				vv.add(estimator);
				if (trainHeader != null) {
					vv.add(trainHeader);
				}

				m_History.addObject(name, vv);
			}
		}
	}

	/**
	 * Saves the currently selected vine.
	 * 
	 * @param name
	 *            the name of the run
	 * @param estimator
	 *            the regular vine to save
	 * @param trainHeader
	 *            the header of the training instances
	 */
	protected void saveEstimator(String name, RegularVine estimator,
			Instances trainHeader) {

		File sFile = null;
		boolean saveOK = true;

		m_FileChooser.setFileFilter(m_ModelFilter);
		int returnVal = m_FileChooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			sFile = m_FileChooser.getSelectedFile();
			if (!sFile.getName().toLowerCase().endsWith(MODEL_FILE_EXTENSION)) {
				sFile = new File(sFile.getParent(), sFile.getName()
						+ MODEL_FILE_EXTENSION);
			}
			m_Log.statusMessage("Saving model to file...");

			try {
				OutputStream os = new FileOutputStream(sFile);
				if (sFile.getName().endsWith(".gz")) {
					os = new GZIPOutputStream(os);
				}
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(
						os);
				objectOutputStream.writeObject(estimator);
				trainHeader = trainHeader.stringFreeStructure();
				if (trainHeader != null) {
					objectOutputStream.writeObject(trainHeader);
				}
				objectOutputStream.flush();
				objectOutputStream.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e, "Save Failed",
						JOptionPane.ERROR_MESSAGE);
				saveOK = false;
			}
			if (saveOK) {
				m_Log.logMessage("Saved model (" + name + ") to file '"
						+ sFile.getName() + "'");
			}
			m_Log.statusMessage("OK");
		}
	}

	/**
	 * Transforms a String matrix to a String representation.
	 * 
	 * @param matrix
	 *            input matrix.
	 * 
	 * @return matrix as String format.
	 */
	public String matrixToString(String[][] matrix) {
		String out = "";

		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				out += matrix[i][j];

				if (j < matrix.length - 1) {
					out += "\t&\t";
				} else {
					out += "\\\\";
				}
			}
			out += "\n";
		}
		return out;
	}

	/**
	 * Opens the named matrix in a separate frame.
	 * 
	 * @param matrix
	 *            the matrix.
	 * @param name
	 *            the name of the matrix to open.
	 */
	public void openFrame(String matrix, String name) {
		if (matrix != null) {
			// Open the frame.
			JTextArea ta = new JTextArea();
			ta.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
			ta.setEditable(false);
			ta.setText(matrix);
			final JFrame jf = new JFrame(name);
			jf.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					jf.dispose();
				}
			});
			jf.getContentPane().setLayout(new BorderLayout());
			jf.getContentPane().add(new JScrollPane(ta), BorderLayout.CENTER);
			jf.pack();
			jf.setSize(450, 350);
			jf.setVisible(true);
		}
	}

	/**
	 * Opens the copula selection in a separate frame.
	 * 
	 * @param rvine
	 *            the regular vine.
	 * @param name
	 *            the name of the frame to open.
	 */
	public void copulaFrame(final RegularVine rvine, String name) {
		// Open the frame.
		JPanel jp = new JPanel();
		jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));

		// initialize checkboxes
		Copula[] cops = rvine.getLoadedCopulas();
		final JCheckBox[] copBox = new JCheckBox[cops.length];
		boolean[] copSel = rvine.getSelected();

		for (int i = 0; i < cops.length; i++) {
			copBox[i] = new JCheckBox(cops[i].name());
			copBox[i].setSelected(copSel[i]);
			jp.add(copBox[i]);
		}

		// add buttons
		JPanel buttons = new JPanel();
		JButton ok = new JButton("Ok");
		JButton cancel = new JButton("Cancel");
		buttons.add(ok);
		buttons.add(cancel);
		jp.add(buttons);
		final JFrame jf = new JFrame(name);

		ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String out = "";
				boolean first = true;
				for (int i = 0; i < copBox.length; i++) {
					if (copBox[i].isSelected()) {
						if (first) {
							out += i;
							first = false;
						} else {
							out += "," + i;
						}
					}
				}
				if (!first) {
					rvine.setCopulaSelection(out);
					m_RVineEditor.setValue(rvine);
				} else
					m_Log.logMessage("Error: Select at least one Copula!");
				jf.dispose();
			}
		});

		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				jf.dispose();
			}
		});

		jf.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				jf.dispose();
			}
		});
		jf.getContentPane().setLayout(new BorderLayout());
		jf.getContentPane().add(new JScrollPane(jp), BorderLayout.CENTER);
		jf.pack();
		jf.setSize(450, 350);
		jf.setVisible(true);
	}

	/**
	 * Tests out the RVines panel from the command line.
	 * 
	 * @param args
	 *            may optionally contain the name of a dataset to load.
	 */
	public static void main(String[] args) {
		try {
			final javax.swing.JFrame jf = new javax.swing.JFrame(
					"Weka Explorer: Estimators");
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
				java.io.Reader r = new java.io.BufferedReader(
						new java.io.FileReader(args[0]));
				Instances i = new Instances(r);
				sp.setInstances(i);
			}
			if (args.length == 0) {
				args = new String[] { "./src/main/data/daxreturns.arff" };
				System.err.println("Loading instances from " + args[0]);
				java.io.Reader r = new java.io.BufferedReader(
						new java.io.FileReader(args[0]));
				Instances i = new Instances(r);
				sp.setInstances(i);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println(ex.getMessage());
		}
	}
}