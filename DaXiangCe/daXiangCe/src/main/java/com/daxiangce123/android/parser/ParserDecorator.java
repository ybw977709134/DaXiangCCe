package com.daxiangce123.android.parser;

import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * @author Hiccup
 */
public abstract class ParserDecorator extends BaseParser {

	protected BaseParser mParser = null;
	
	public ParserDecorator(BaseParser parser) {
		mParser = parser;
	}
	
	public abstract void parseData(Hashtable<String, String> content, JSONObject json) throws JSONException;
	
}
