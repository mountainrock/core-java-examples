package common.tools;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import util.WebDriverUtil;

public class SuperWebDriver {

	static String browseFile = "src/main/resources/webdriver/manualentry/manualentry-loadtrade.txt";
	static Map<String, String> methodNameMap = new HashMap<String, String>();
	static{
		methodNameMap.put("get","defaultGet");
		methodNameMap.put("input.auto","defaultSendKeysToInputAndAutoSelect");
		methodNameMap.put("input.readonly","defaultSendKeysToReadOnlyInput");
		methodNameMap.put("input.text","defaultSendKeysToInput");
		methodNameMap.put("select","defaultSelectOptionFromSelectDropDown");
		methodNameMap.put("click.button.xpath","defaultButtonXpathClick");
		methodNameMap.put("click.button","defaultButtonClick");
		
		methodNameMap.put("sleep","sleep");
	}
	
	/** Run SuperWebDriver [FILE PATH]
	 * <pre> Sample instructions
	    ##
		## Enter the instruction for browser to execute
		##
		get >> http://localhost:8080/test/save/trade.do
		
		input.readonly >> settlementDate=01-12-2012
		input.auto >> imntDesc=TESC
		input.auto >> partyAltCode=ABA
		input.auto >> accountCd=2632
		input.text >> tradePrice=1
		input.text >> tradeQuantity=1
		
		sleep >> 5
		select >> instrumentCurrencyCode=USD
		
		click.button.xpath >> //button[@title='Posts the trade details']
		#click.button >> Posts
	 */
	public static void main(final String[] args) throws Exception {
		if(args.length >0){
			browseFile = args[0];
		}
		log("Reading .. "+ browseFile);
		final List<String> browserInstructions = FileUtils.readLines(new File(browseFile));

		final WebDriverUtil webDriverUtil= WebDriverUtil.getInstance();
		
		for (final String instruction : browserInstructions) {
			if(instruction.startsWith("#") ||StringUtils.isEmpty(instruction)){
				continue;
			}
			log("running : "+ instruction);
			String methodName=StringUtils.substringBefore(instruction," >> ");
			methodName = methodNameMap.get(methodName);
			final String argument=StringUtils.substringAfter(instruction," >> ");
			log(String.format("\t calling %s(%s) ", methodName,argument));
			if(!methodName.toLowerCase().contains("click")){ //input
				final String[] arguments= StringUtils.split(argument, "=");
				MethodUtils.invokeMethod(webDriverUtil, methodName, arguments);
			}else{
				MethodUtils.invokeMethod(webDriverUtil, methodName, argument);
			}
			
		}
	}
	static void log(final String str){
		System.out.println(str);
	}

}
