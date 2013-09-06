package cn.wisecamp.tool;


public class Main {
	
	public static void main(String[] args) {
		UISetting.enableAntiAliasing();
		UISetting.setUI();
		UiFrame win = new UiFrame();
		win.setVisible(true);
		win.setResizable(false);
	}
}
