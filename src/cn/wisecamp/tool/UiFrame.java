package cn.wisecamp.tool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class UiFrame extends JFrame {
	
	private static final long serialVersionUID = -8817142763557920008L;
	private static final String newline = "\n";
    private static final int PANEL_WITH = 580;
    private static final int PANEL_HEIGHT = 200;
    private static final int BTN_HEIGHT = 32;
	private static final String FILEBTN_TITLE = "选择文件或目录";
	private static final String APP_TITLE = "YUI Compressor GUI 小工具";
	private static final String OPEN_TIP = "打开 : "; 
	private static final String DRI_TITLE = "压缩目标 : "; 
	private static final String RENAME_TITLE = "重命名后缀 : *.";
	private static final String FILE_TYPE_TITLE = "处理的文件类型: "; 
	private static final String COMPRESS_TYPE_TITLE = "压缩方式: "; 
	private static final String [] compressTypeStrs = {
        "统一压缩到一个css/js文件",
        "分别压缩并覆盖原css/js文件",
        "分别压缩并重命名css/js文件后缀"
	};

	private JButton selectFileBtn = new JButton(FILEBTN_TITLE);
	private JButton compressBtn = new JButton("压        缩");
	private JLabel dirTitle = new JLabel(DRI_TITLE);
	private JLabel fileTypeTitle = new JLabel(FILE_TYPE_TITLE);
	private JLabel compressTypeTitle = new JLabel(COMPRESS_TYPE_TITLE);
	private JLabel dirTip = new JLabel();
	private JLabel renameTip = new JLabel(RENAME_TITLE);
	private JLabel renameTail = new JLabel(".css/.js");
	private JPanel selectPanel = new JPanel();
	private JFileChooser chooser = new JFileChooser();
	private JTextArea logArea = new JTextArea();
	private JCheckBox cssBox = new JCheckBox("*.css");
	private JCheckBox jsBox = new JCheckBox("*.js");
	private JTextField tailInput = new JTextField("min");
	private JComboBox<String> compressTypes = new JComboBox<String>(compressTypeStrs);
	
	// handle file select
	ActionListener selectFileBtnEvent = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == selectFileBtn) {
				int returnVal = chooser.showOpenDialog(UiFrame.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					dirTip.setText(file.getAbsolutePath());
					logArea.append(OPEN_TIP + file.getAbsolutePath() + newline);
				}
			}
		}
	};
	
	// handle compress
	ActionListener compressBtnEvent = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == compressBtn) {
				final String targetfile = dirTip.getText();
				final int compressType = compressTypes.getSelectedIndex();
				final String tail = tailInput.getText();
				final boolean handleCss = cssBox.isSelected();
				final boolean handleJs = jsBox.isSelected();
				logArea.append(DRI_TITLE + targetfile + newline);
				logArea.append(COMPRESS_TYPE_TITLE + compressTypes.getSelectedItem() + newline);
				logArea.append(RENAME_TITLE + tail + ".css/js" + newline);
				logArea.append("是否处理Css: " + (handleCss ? "是" : "否") + newline);
				logArea.append("是否处理Js: " + (handleJs ? "是" : "否") + newline);
				final CompressJs compressor = new CompressJs(logArea);
				Thread compressJob = new Thread(new Runnable() {
					@Override
					public void run() {
						compressor.compress(targetfile, compressType, tail, handleCss, handleJs);
					}
				});
				compressJob.start();
			}
		}
	};

	public UiFrame() {
		super();
		this.setTitle(APP_TITLE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();      //得到屏幕的尺寸 
		int w = screenSize.width; //宽度
		int h = screenSize.height; //高度
		this.setBounds((w - 600) / 2, (h - 436) / 2, 600, 436);
		this.setLayout(null);
		initUi();
	}
	
	private void initUi() {
		// line 1
		compressTypeTitle.setBounds(0, 10, 80, BTN_HEIGHT);
		renameTip.setBounds(100 + 260 + 10, 10, 100, BTN_HEIGHT);
		renameTail.setBounds(100 + 260 + 10 + 160, 10, 100, BTN_HEIGHT);
		compressTypes.setBounds(70, 10, 230, BTN_HEIGHT);
		tailInput.setBounds(100 + 260 + 10 + 85, 10, 78, BTN_HEIGHT);
        // line 2
		selectFileBtn.addActionListener(selectFileBtnEvent);
		dirTitle.setBounds(0, 20 + BTN_HEIGHT, 90, BTN_HEIGHT);
		dirTip.setText(new File("").getAbsolutePath());
		dirTip.setBounds(70, 20 + BTN_HEIGHT, 580 - 140 - 95, BTN_HEIGHT);
		selectFileBtn.setBounds(100 + 260 + 10, 20 + BTN_HEIGHT, 200, BTN_HEIGHT);
        // choose
        chooser.setCurrentDirectory(new File("."));
	    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		// line 3
		fileTypeTitle.setBounds(0, (20 + BTN_HEIGHT) * 2, 100, BTN_HEIGHT);
		cssBox.setBounds(100 + 10, (20 + BTN_HEIGHT) * 2, 60, BTN_HEIGHT);
		jsBox.setBounds(100 + 10 + 60 + 10, (20 + BTN_HEIGHT) * 2, 60, BTN_HEIGHT);
		cssBox.setSelected(true);
		jsBox.setSelected(true);
		compressBtn.addActionListener(compressBtnEvent);
		compressBtn.setBounds(100 + 260 + 10, (20 + BTN_HEIGHT) * 2, 200, BTN_HEIGHT);
		// log panel
		selectPanel.setLayout(null);
		selectPanel.setBounds(8, 0, PANEL_WITH, PANEL_HEIGHT);
        logArea.setSize(PANEL_WITH, PANEL_HEIGHT);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBounds(8, 146, PANEL_WITH, PANEL_HEIGHT + 200 - 146);
	    // add to frame
		selectPanel.add(selectFileBtn);
		selectPanel.add(dirTitle);
		selectPanel.add(dirTip);
		selectPanel.add(fileTypeTitle);
		selectPanel.add(compressTypeTitle);
		selectPanel.add(cssBox);
		selectPanel.add(jsBox);
		selectPanel.add(compressTypes);
		selectPanel.add(renameTip);
		selectPanel.add(renameTail);
		selectPanel.add(tailInput);
		selectPanel.add(compressBtn);
        this.add(logScrollPane, BorderLayout.CENTER);
		this.add(selectPanel, BorderLayout.PAGE_START);
	}
}
