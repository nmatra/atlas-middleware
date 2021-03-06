package fuzzingengine;

import middleware.core.MOOSVariableUpdate;

public abstract class NumericVariableChangeFuzzingEngine extends FuzzingEngine {
	public boolean doFuzzing = true;
	private static final boolean debugging = false;
	private String regexNumberScanner = "[+-]?([0-9]*\\.)?[0-9]+";
	
	public abstract String getReplacement(String inValue);
	
	public NumericVariableChangeFuzzingEngine() {

	}
		
	public synchronized MOOSVariableUpdate fuzzMessage(MOOSVariableUpdate mup) {
		MOOSVariableUpdate mupOut = new MOOSVariableUpdate(mup);
				
		if (debugging) {
			System.out.println("FUZZ ENGINE VARIABLE IN: " + mupOut.getValue());
		}
		if (doFuzzing) {
			String inval = mup.getValue();
			String replaceValue = getReplacement(inval);
			String outval = inval.replaceAll(regexNumberScanner, replaceValue);
			System.out.println("outval=" + outval);
			mupOut.setValue(outval);
		}
		
		if (debugging) {
			System.out.println("FUZZ ENGINE VARIABLE OUT:" + mupOut.getValue());
		}
		return mupOut;
	}
}
