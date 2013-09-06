package cn.wisecamp.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;

import javax.swing.JTextArea;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class CompressJs{
	
	private class CompressOption {
		String targetfile;
		int compressType;
		String tail;
		boolean handleCss;
		boolean handleJs;
	}
	
	static final int linebreakpos = -1;
	static final String newline = "\n";
	static final boolean munge=true;
	static final boolean verbose=false;
	static final boolean preserveAllSemiColons=false;
	static final boolean disableOptimizations=false;
	static final int ALL_IN_ONE = 0; // 合并模式
	static final int REPLACE_SOURCE = 1; // 单文件覆盖模式
	static final int RENAME_TO = 2; // 单文件重命名模式
	
	JTextArea logArea = null;
	CompressOption option = new CompressOption();
	File tempCssFile = null;
	File tempJsFile = null;
	int sumCss = 0;
	int sumJs = 0;
	int sumSuceessCss = 0;
	int sumSuceessJs = 0;

	public CompressJs(JTextArea logArea) {
		this.logArea = logArea;
	}

	// 压缩处理
	public void compress(String targetfile, int compressType, String tail,
			boolean handleCss, boolean handleJs) {
		option.targetfile = targetfile;
		option.compressType = compressType;
		option.tail = tail;
		option.handleCss = handleCss;
		option.handleJs = handleJs;
		logArea.append("开始压缩"+ newline);
		long startMili=System.currentTimeMillis();// 当前时间对应的毫秒数
		checkFile(new File(targetfile));
		try {
			if (ALL_IN_ONE == option.compressType) {
				if (option.handleJs && tempJsFile != null && tempJsFile.exists()) {
					File tempFile = new File(tempJsFile.getAbsolutePath() + ".tempFile");
					compress(tempJsFile, tempFile);
					tempFile.delete();
				}
				if (option.handleCss && tempCssFile != null && tempCssFile.exists()) {
					File tempFile = new File(tempCssFile.getAbsolutePath() + ".tempFile");
					compress(tempCssFile, tempFile);	
					tempFile.delete();
				}
			}
		} catch (IOException e) {
			logArea.append(e.getMessage() + newline);
		}
		long endMili=System.currentTimeMillis();
		logArea.append("已处理Js文件:" + sumJs + "个,Css文件：" + sumCss + "个." + newline);
		logArea.append("成功处理Js文件:" + sumSuceessJs + "个,Css文件：" + sumSuceessCss + "个." + newline);
		if (ALL_IN_ONE == option.compressType) {
			if (option.handleJs && tempJsFile != null && tempJsFile.exists()) {
				logArea.append("合并" + sumSuceessJs + "个Js文件到：" + tempJsFile.getAbsolutePath() + newline);
			}
			if (option.handleCss && tempCssFile != null && tempCssFile.exists()) {
				logArea.append("合并" + sumSuceessCss + "个Css文件到：" + tempCssFile.getAbsolutePath() + newline);
			}
		}
		logArea.append("总耗时：" + (endMili-startMili) / 1000 + "秒." + newline);
	}
	
	public String getlogArea() {
		return logArea.toString();
	}

	// 检查并压缩
	private void checkFile(File file) {
		String name = file.getName();
		if (name.endsWith(".svn") || name.endsWith(".git") || name.endsWith(".settings"))
			return;
		if (file.isFile()) {
			compress(file);
			return;
		}
		File[] files = file.listFiles();
		if (files == null || files.length == 0)
			return;
		for (File f : files) {
			if (file.isFile()) {
				compress(file);
				continue;
			} else if (file.isDirectory()) {
				checkFile(f);
			}
		}
	}

	private void compress(File file) {
		String fileName = file.getName();
		if (!((fileName.endsWith(".js") && option.handleJs) || (fileName.endsWith(".css") && option.handleCss))) {
			return;
		}
		logArea.append("处理文件:" + file.getAbsolutePath() + newline);
		if (fileName.endsWith(".js")) {
			sumJs = sumJs + 1;
		} else {
			sumCss = sumCss + 1;
		}
		String filePath = file.getAbsolutePath();
		if (option.compressType == ALL_IN_ONE) {
			if (tempCssFile == null) {
				tempCssFile = new File(new File(option.targetfile) + ".all.css");
				if (tempCssFile.exists()) {
					tempCssFile.delete();
				}
			}
			if (tempJsFile == null) {
				tempJsFile = new File(new File(option.targetfile) + ".all.js");
				if (tempJsFile.exists()) {
					tempJsFile.delete();
				}
			}
		} else {
			tempCssFile = new File(filePath + ".tempCssFile");
			tempJsFile = new File(filePath + ".tempJsFile");
		}
		File tempFile = new File(filePath + ".tempFile");
		try {
			compress(file, tempFile);
		} catch (IOException e) {
			logArea.append(e.getMessage() + newline);
			return;
		}
		try {
			if (fileName.endsWith(".js")) {
				if (ALL_IN_ONE == option.compressType) {
					appendToFile(tempFile, tempJsFile);
				}
				sumSuceessJs = sumSuceessJs + 1;
			} else {
				if (ALL_IN_ONE == option.compressType) {
					appendToFile(tempFile, tempCssFile);
				}
				sumSuceessCss = sumSuceessCss + 1;
			}
		} catch (IOException e) {
			logArea.append(e.getMessage() + newline);
		}
		if (REPLACE_SOURCE == option.compressType) {
			file.delete();
			tempFile.renameTo(file);
			tempFile.delete();
		}
		if (RENAME_TO == option.compressType) {
			if (fileName.endsWith(".js")) {
				renameTempFile(file, tempFile, ".js");
			} else {
				renameTempFile(file, tempFile, ".css");
			}
		}
	}
	
	private void compress(File file, File tempFile) throws EvaluatorException, IOException {
		Reader in = new FileReader(file);
		Writer out = new FileWriter(tempFile);
		if (file.getAbsolutePath().endsWith(".js")) {
			JavaScriptCompressor jscompressor = new JavaScriptCompressor(in, new ErrorReporter() {
				public void warning(String message, String sourceName,
						int line, String lineSource, int lineOffset) {
					if (line < 0) {
						logArea.append("[WARNING] " + message + newline);
					} else {
						logArea.append("[WARNING] " + line + ':' + lineOffset + ':' + message + newline);
					}
				}

				public void error(String message, String sourceName,
						int line, String lineSource, int lineOffset) {
					if (line < 0) {
						logArea.append("[ERROR] " + message + newline);
					} else {
						logArea.append("[ERROR] " + line + ':' + lineOffset + ':' + message + newline);
					}
				}

				public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
						int lineOffset) {
					error(message, sourceName, line, lineSource, lineOffset);
					return new EvaluatorException(message);
				}
			});
			jscompressor.compress(out, linebreakpos, munge, verbose,
					preserveAllSemiColons, disableOptimizations);
		} else {
			CssCompressor csscompressor = new CssCompressor(in);
			csscompressor.compress(out, linebreakpos);
		}
		out.close();
		in.close();
	}

	private void renameTempFile(File file, File tempFile, String suffix) {
		String newName = file.getAbsolutePath();
		if (!newName.endsWith("." + option.tail + suffix)) {
			newName = newName.substring(0, newName.lastIndexOf(suffix)).concat("." + option.tail + suffix);
		}
		if (new File(newName).exists()) {
			String now = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new java.util.Date());
			logArea.append("[WARNING] " + newName + "rename to " + newName + "." + now + newline);
			newName = newName.concat(".").concat(now);
		}
		tempFile.renameTo(new File(newName));
	}

	private void appendToFile(File in, File out) throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(in));
		BufferedWriter output = new BufferedWriter(new FileWriter(out, true));
		String tmp = null;
		while ((tmp = input.readLine()) != null) {
			output.write(tmp);
		}
		input.close();
		output.close();
		in.delete();
	}
}