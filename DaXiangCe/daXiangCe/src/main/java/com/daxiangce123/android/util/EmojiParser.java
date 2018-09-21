package com.daxiangce123.android.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

import com.daxiangce123.R;

public class EmojiParser {
	public final static String EMOJI_TAG = "emoji";
	public final static String[] emoji_string_list_0 = { "1f604", "1f60a",
			"1f603", "263a", "1f609", "1f60d", "1f618", "1f61a", "1f633",
			"1f60c", "1f601", "1f61c", "1f61d", "1f612", "1f60f", "1f613",
			"1f614", "1f61e", "1f616", "1f625", "1f630", "1f628", "1f623",
			"1f622", "1f62d", "1f602", "1f632", "1f631", "1f620", "1f621",
			"1f62a", "1f637", "1f47f", "1f47d", "1f49b", "1f499", "1f49c",
			"1f497", "1f49a", "2764", "1f494", "1f493", "1f498", "2728",
			"2b50", "1f31f", "1f4a2", "2757", "2755", "2753", "2754", "1f4a4",
			"1f4a8", "1f4a6", "1f3b6", "1f3b5", "1f525", "1f4a9", "1f44d",
			"1f44e", "1f44c", "1f44a", "270a", "270c", "1f44b", "270b",
			"1f450", "1f446", "1f447", "1f449", "1f448", "1f64c", "1f64f",
			"261d", "1f44f", "1f4aa", "1f6b6", "1f3c3", "1f46b", "1f483",
			"1f46f", "1f646", "1f645", "1f481", "1f647", "1f48f", "1f491",
			"1f486", "1f487", "1f485", "1f466", "1f467", "1f469", "1f468",
			"1f476", "1f475", "1f474", "1f471", "1f472", "1f473", "1f477",
			"1f46e", "1f47c", "1f478", "1f482", "1f480", "1f463", "1f48b",
			"1f444", "1f442", "1f440", "1f443" };
	public final static String[] emoji_string_list_1 = { "2600", "2614",
			"2601", "26c4", "1f319", "26a1", "1f300", "1f30a", "1f431",
			"1f436", "1f42d", "1f439", "1f430", "1f43a", "1f438", "1f42f",
			"1f428", "1f43b", "1f437", "1f42e", "1f417", "1f412", "1f434",
			"1f40e", "1f42b", "1f411", "1f418", "1f40d", "1f426", "1f424",
			"1f414", "1f427", "1f41b", "1f419", "1f435", "1f420", "1f41f",
			"1f433", "1f42c", "1f490", "1f338", "1f337", "1f340", "1f339",
			"1f33b", "1f33a", "1f341", "1f343", "1f342", "1f334", "1f335",
			"1f33e", "1f41a" };
	public final static String[] emoji_string_list_2 = { "1f38d", "1f49d",
			"1f38e", "1f392", "1f393", "1f38f", "1f386", "1f387", "1f390",
			"1f391", "1f383", "1f47b", "1f385", "1f384", "1f381", "1f514",
			"1f389", "1f388", "1f4bf", "1f4c0", "1f4f7", "1f3a5", "1f4bb",
			"1f4fa", "1f4f1", "1f4e0", "260e", "1f4bd", "1f4fc", "1f50a",
			"1f4e2", "1f4e3", "1f4fb", "1f4e1", "27bf", "1f50d", "1f513",
			"1f512", "1f511", "2702", "1f528", "1f4a1", "1f4f2", "1f4e9",
			"1f4eb", "1f4ee", "1f6c0", "1f6bd", "1f4ba", "1f4b0", "1f531",
			"1f6ac", "1f4a3", "1f52b", "1f48a", "1f489", "1f3c8", "1f3c0",
			"26bd", "26be", "1f3be", "26f3", "1f3b1", "1f3ca", "1f3c4",
			"1f3bf", "2660", "2665", "2663", "2666", "1f3c6", "1f47e", "1f3af",
			"1f004", "1f3ac", "1f4dd", "1f4d6", "1f3a8", "1f3a4", "1f3a7",
			"1f3ba", "1f3b7", "1f3b8", "303d", "1f45f", "1f461", "1f460",
			"1f462", "1f455", "1f454", "1f457", "1f458", "1f459", "1f380",
			"1f3a9", "1f451", "1f452", "1f302", "1f4bc", "1f45c", "1f484",
			"1f48d", "1f48e", "2615", "1f375", "1f37a", "1f37b", "1f378",
			"1f376", "1f374", "1f354", "1f35f", "1f35d", "1f35b", "1f371",
			"1f363", "1f359", "1f358", "1f35a", "1f35c", "1f372", "1f35e",
			"1f373", "1f362", "1f361", "1f366", "1f367", "1f382", "1f370",
			"1f34e", "1f34a", "1f349", "1f353", "1f346", "1f345" };
	public final static String[] emoji_string_list_3 = { "1f3e0", "1f3eb",
			"1f3e2", "1f3e3", "1f3e5", "1f3e6", "1f3ea", "1f3e9", "1f3e8",
			"1f492", "26ea", "1f3ec", "1f307", "1f306", "1f3ef", "1f3f0",
			"26fa", "1f3ed", "1f5fc", "1f5fb", "1f304", "1f305", "1f303",
			"1f5fd", "1f308", "1f3a1", "26f2", "1f3a2", "1f6a2", "1f6a4",
			"26f5", "2708", "1f680", "1f6b2", "1f699", "1f697", "1f695",
			"1f68c", "1f693", "1f692", "1f691", "1f69a", "1f683", "1f689",
			"1f684", "1f685", "1f3ab", "26fd", "1f6a5", "26a0", "1f6a7",
			"1f530", "1f3e7", "1f3b0", "1f68f", "1f488", "2668", "1f3c1",
			"1f38c", "1f1ef_1f1f5", "1f1f0_1f1f7", "1f1e8_1f1f3",
			"1f1fa_1f1f8", "1f1eb_1f1f7" };
	public final static String[] emoji_string_list_4 = { "0023_20e3",
			"0030_20e3", "0031_20e3", "0032_20e3", "0033_20e3", "0034_20e3",
			"0035_20e3", "0036_20e3", "0037_20e3", "0038_20e3", "0039_20e3",
			"1f1ea_1f1f8", "1f1ee_1f1f9", "1f1f7_1f1fa", "1f1ec_1f1e7",
			"1f1e9_1f1ea", "2b06", "2b07", "2b05", "27a1", "2196", "2197",
			"2198", "2199", "25c0", "25b6", "23ea", "23e9", "1f197", "1f195",
			"1f51d", "1f199", "1f192", "1f3a6", "1f201", "1f4f6", "1f235",
			"1f233", "1f250", "1f239", "1f22f", "1f23a", "1f236", "1f21a",
			"1f237", "1f238", "1f202", "1f6bb", "1f6b9", "1f6ba", "1f6bc",
			"1f6ad", "1f17f", "267f", "1f687", "1f6be", "3299", "3297",
			"1f51e", "1f194", "2733", "2734", "1f49f", "1f19a", "1f4f3",
			"1f4f4", "1f4b9", "1f4b1", "2648", "2649", "264a", "264b", "264c",
			"264d", "264e", "264f", "2650", "2651", "2652", "2653", "26ce",
			"1f52f", "1f170", "1f171", "1f18e", "1f17e", "1f532", "1f534",
			"1f533", "1f55b", "1f550", "1f551", "1f552", "1f553", "1f554",
			"1f555", "1f556", "1f557", "1f558", "1f559", "1f55a", "1f55b",
			"2b55", "274c", "00a9", "00ae", "2122" };

	private EmojiParser() {
		readMap();
	}

	private HashMap<String, String> convertMap = new HashMap<String, String>();
	private static EmojiParser mParser;

	public static EmojiParser getInstance() {
		if (mParser == null) {
			mParser = new EmojiParser();
		}
		return mParser;
	}

	// private void readMap(Context mContext) {
	// if (convertMap == null || convertMap.size() == 0) {
	// convertMap = new HashMap<String, String>();
	// XmlPullParser xmlpull = null;
	// String fromAttr = null;
	// // String key = null;
	// // ArrayList<String> emos = null;
	// try {
	// XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
	// xmlpull = xppf.newPullParser();
	// InputStream stream = mContext.getAssets().open("emoji.xml");
	// xmlpull.setInput(stream, "UTF-8");
	// int eventCode = xmlpull.getEventType();
	// while (eventCode != XmlPullParser.END_DOCUMENT) {
	// switch (eventCode) {
	// case XmlPullParser.START_DOCUMENT: {
	// break;
	// }
	// case XmlPullParser.START_TAG: {
	// // if (xmlpull.getName().equals("key")) {
	// // emos = new ArrayList<String>();
	// // key = xmlpull.nextText();
	// // }
	// if (xmlpull.getName().equals("e")) {
	// fromAttr = xmlpull.nextText();
	// String key = "";
	// List<Integer> fromCodePoints = new ArrayList<Integer>();
	// if (fromAttr.length() > 6) {
	// String[] froms = fromAttr.split("_");
	// for (String part : froms) {
	// fromCodePoints.add(Integer.parseInt(part,
	// 16));
	// }
	// } else {
	// fromCodePoints.add(Integer.parseInt(fromAttr,
	// 16));
	// }
	// int s = fromCodePoints.size();
	// for (int i = 0; i < s; i++) {
	// if (i == 0) {
	// key = fromCodePoints.get(i) + "";
	// } else {
	// key = key + "_" + fromCodePoints.get(i);
	// }
	// }
	// convertMap.put(key, fromAttr);
	// }
	// break;
	// }
	// case XmlPullParser.END_TAG: {
	// // if (xmlpull.getName().equals("dict")) {
	// // emoMap.put(key, emos);
	// // }
	// break;
	// }
	// case XmlPullParser.END_DOCUMENT: {
	// break;
	// }
	// }
	// eventCode = xmlpull.next();
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }

	private void readMap() {
		if (convertMap == null || convertMap.isEmpty()) {
			// long start = System.currentTimeMillis();
			parseUnicode(emoji_string_list_0);
			parseUnicode(emoji_string_list_1);
			parseUnicode(emoji_string_list_2);
			parseUnicode(emoji_string_list_3);
			parseUnicode(emoji_string_list_4);
			// log("readMap()	" + (System.currentTimeMillis() - start)
			// + "; size = " + convertMap.size());
			// softbanToUnicode();
		}
	}

	private void parseUnicode(String[] codes) {
		if (codes == null || codes.length == 0) {
			return;
		}
		for (String str : codes) {
			String key = "";
			List<Integer> fromCodePoints = new ArrayList<Integer>();
			if (str.contains("_")) {
				String[] froms = str.split("_");
				for (String part : froms) {
					fromCodePoints.add(Integer.parseInt(part, 16));
				}
			} else {
				fromCodePoints.add(Integer.parseInt(str, 16));
			}
			int s = fromCodePoints.size();
			for (int i = 0; i < s; i++) {
				if (i == 0) {
					key = fromCodePoints.get(i) + "";
				} else {
					key = key + "_" + fromCodePoints.get(i);
				}
			}
			if (convertMap.containsKey(key)) {
				// log("containsKey : " + key + ";" + str);
			} else {
				convertMap.put(key, str);
			}
		}
	}

	// @SuppressLint("DefaultLocale")
	// private void softbanToUnicode() {
	// HashMap<String, String> unicodeToSoftbankMap = new HashMap<String,
	// String>();
	// HashMap<String, String> notExists = new HashMap<String, String>();
	// int l = 0;
	// long start = System.currentTimeMillis();
	// String source =
	// "2600=e04a#2601=e049#2614=e04b#26c4=e048#26a1=e13d#1f300=e443#1f302=e43c#1f303=e44b#1f304=e04d#1f305=e449#1f306=e146#1f307=e44a#1f308=e44c#1f309=e44b#1f30a=e43e#1f30c=e44b#1f314=e04c#1f313=e04c#1f319=e04c#1f31b=e04c#1f31f=e335#1f550=e024#1f551=e025#1f552=e026#1f553=e027#1f554=e028#1f555=e029#1f556=e02a#1f557=e02b#1f558=e02c#1f559=e02d#1f55a=e02e#1f55b=e02f#23f0=e02d#2648=e23f#2649=e240#264a=e241#264b=e242#264c=e243#264d=e244#264e=e245#264f=e246#2650=e247#2651=e248#2652=e249#2653=e24a#26ce=e24b#1f340=e110#1f337=e304#1f331=e110#1f341=e118#1f338=e030#1f339=e032#1f342=e119#1f343=e447#1f33a=e303#1f33b=e305#1f334=e307#1f335=e308#1f33e=e444#1f33c=e305#1f33f=e110#1f34e=e345#1f34a=e346#1f353=e347#1f349=e348#1f345=e349#1f346=e34a#1f34f=e345#1f440=e419#1f442=e41b#1f443=e41a#1f444=e41c#1f445=e409#1f484=e31c#1f485=e31d#1f486=e31e#1f487=e31f#1f488=e320#1f466=e001#1f467=e002#1f468=e004#1f469=e005#1f46b=e428#1f46e=e152#1f46f=e429#1f471=e515#1f472=e516#1f473=e517#1f474=e518#1f475=e519#1f476=e51a#1f477=e51b#1f478=e51c#1f47b=e11b#1f47c=e04e#1f47d=e10c#1f47e=e12b#1f47f=e11a#1f480=e11c#1f481=e253#1f482=e51e#1f483=e51f#1f40d=e52d#1f40e=e134#1f414=e52e#1f417=e52f#1f42b=e530#1f418=e526#1f428=e527#1f412=e528#1f411=e529#1f419=e10a#1f41a=e441#1f41b=e525#1f420=e522#1f421=e019#1f424=e523#1f425=e523#1f426=e521#1f423=e523#1f427=e055#1f429=e052#1f41f=e019#1f42c=e520#1f42d=e053#1f42f=e050#1f431=e04f#1f433=e054#1f434=e01a#1f435=e109#1f436=e052#1f437=e10b#1f43b=e051#1f439=e524#1f43a=e52a#1f42e=e52b#1f430=e52c#1f438=e531#1f43e=e536#1f43d=e10b#1f620=e059#1f629=e403#1f632=e410#1f61e=e058#1f635=e406#1f630=e40f#1f612=e40e#1f60d=e106#1f624=e404#1f61c=e105#1f61d=e409#1f60b=e056#1f618=e418#1f61a=e417#1f637=e40c#1f633=e40d#1f603=e057#1f606=e40a#1f601=e404#1f602=e412#1f60a=e056#263a=e414#1f604=e415#1f622=e413#1f62d=e411#1f628=e40b#1f623=e406#1f621=e416#1f60c=e40a#1f616=e407#1f614=e403#1f631=e107#1f62a=e408#1f60f=e402#1f613=e108#1f625=e401#1f62b=e406#1f609=e405#1f63a=e057#1f638=e404#1f639=e412#1f63d=e418#1f63b=e106#1f63f=e413#1f63e=e416#1f63c=e404#1f640=e403#1f645=e423#1f646=e424#1f647=e426#1f64b=e012#1f64c=e427#1f64d=e403#1f64e=e416#1f64f=e41d#1f3e0=e036#1f3e1=e036#1f3e2=e038#1f3e3=e153#1f3e5=e155#1f3e6=e14d#1f3e7=e154#1f3e8=e158#1f3e9=e501#1f3ea=e156#1f3eb=e157#26ea=e037#26f2=e121#1f3ec=e504#1f3ef=e505#1f3f0=e506#1f3ed=e508#2693=e202#1f3ee=e30b#1f5fb=e03b#1f5fc=e509#1f5fd=e51d#1f45e=e007#1f45f=e007#1f460=e13e#1f461=e31a#1f462=e31b#1f463=e536#1f455=e006#1f451=e10e#1f454=e302#1f452=e318#1f457=e319#1f458=e321#1f459=e322#1f45a=e006#1f45c=e323#1f4b0=e12f#1f4b1=e149#1f4b9=e14a#1f4b2=e12f#1f4b5=e12f#1f1e8_1f1f3=e513#1f1e9_1f1ea=e50e#1f1ea_1f1f8=e511#1f1eb_1f1f7=e50d#1f1ec_1f1e7=e510#1f1ee_1f1f9=e50f#1f1ef_1f1f5=e50b#1f1f0_1f1f7=e514#1f1f7_1f1fa=e512#1f1fa_1f1f8=e50c#1f525=e11d#1f528=e116#1f52b=e113#1f52e=e23e#1f52f=e23e#1f530=e209#1f531=e031#1f489=e13b#1f48a=e30f#1f170=e532#1f171=e533#1f18e=e534#1f17e=e535#1f380=e314#1f381=e112#1f382=e34b#1f384=e033#1f385=e448#1f38c=e143#1f386=e117#1f388=e310#1f389=e312#1f38d=e436#1f38e=e438#1f393=e439#1f392=e43a#1f38f=e43b#1f387=e440#1f390=e442#1f383=e445#1f391=e446#260e=e009#1f4de=e009#1f4f1=e00a#1f4f2=e104#1f4dd=e301#1f4e0=e00b#2709=e103#1f4e8=e103#1f4e9=e103#1f4ea=e101#1f4eb=e101#1f4ee=e102#1f4e2=e142#1f4e3=e317#1f4e1=e14b#1f4e6=e112#1f4e7=e103#1f4ba=e11f#1f4bb=e00c#270f=e301#1f4bc=e11e#1f4bd=e316#1f4be=e316#1f4bf=e126#1f4c0=e127#2702=e313#1f4c3=e301#1f4c4=e301#1f4d3=e148#1f4d6=e148#1f4d4=e148#1f4d5=e148#1f4d7=e148#1f4d8=e148#1f4d9=e148#1f4da=e148#1f4cb=e301#1f4ca=e14a#1f4c8=e14a#1f4c7=e148#1f4d2=e148#1f4d1=e301#26be=e016#26f3=e014#1f3be=e015#26bd=e018#1f3bf=e013#1f3c0=e42a#1f3c1=e132#1f3c3=e115#1f3c4=e017#1f3c6=e131#1f3c8=e42b#1f3ca=e42d#1f683=e01e#1f687=e434#24c2=e434#1f684=e435#1f685=e01f#1f697=e01b#1f699=e42e#1f68c=e159#1f68f=e150#1f6a2=e202#2708=e01d#26f5=e01c#1f689=e039#1f680=e10d#1f6a4=e135#1f695=e15a#1f69a=e42f#1f692=e430#1f691=e431#1f693=e432#26fd=e03a#1f17f=e14f#1f6a5=e14e#1f6a7=e137#1f6a8=e432#2668=e123#26fa=e122#1f3a1=e124#1f3a2=e433#1f3a3=e019#1f3a4=e03c#1f3a5=e03d#1f3a6=e507#1f3a7=e30a#1f3a8=e502#1f3a9=e503#1f3ab=e125#1f3ac=e324#1f3ad=e503#1f004=e12d#1f3af=e130#1f3b0=e133#1f3b1=e42c#1f3b5=e03e#1f3b6=e326#1f3b7=e040#1f3b8=e041#1f3ba=e042#1f3bc=e326#303d=e12c#1f4f7=e008#1f4f9=e03d#1f4fa=e12a#1f4fb=e128#1f4fc=e129#1f48b=e003#1f48d=e034#1f48e=e035#1f48f=e111#1f490=e306#1f491=e425#1f492=e43d#1f51e=e207#00a9=e24e#00ae=e24f#2122=e537#0023_20e3=e210#0031_20e3=e21c#0032_20e3=e21d#0033_20e3=e21e#0034_20e3=e21f#0035_20e3=e220#0036_20e3=e221#0037_20e3=e222#0038_20e3=e223#0039_20e3=e224#0030_20e3=e225#1f4f6=e20b#1f4f3=e250#1f4f4=e251#1f354=e120#1f359=e342#1f370=e046#1f35c=e340#1f35e=e339#1f373=e147#1f366=e33a#1f35f=e33b#1f361=e33c#1f358=e33d#1f35a=e33e#1f35d=e33f#1f35b=e341#1f362=e343#1f363=e344#1f371=e34c#1f372=e34d#1f367=e43f#1f374=e043#2615=e045#1f378=e044#1f37a=e047#1f375=e338#1f376=e30b#1f377=e044#1f37b=e30c#1f379=e044#2197=e236#2198=e238#2196=e237#2199=e239#2934=e236#2935=e238#2b06=e232#2b07=e233#27a1=e234#2b05=e235#25b6=e23a#25c0=e23b#23e9=e23c#23ea=e23d#2b55=e332#274c=e333#274e=e333#2757=e021#2753=e020#2754=e336#2755=e337#27bf=e211#2764=e022#1f493=e327#1f494=e023#1f495=e327#1f496=e327#1f497=e328#1f498=e329#1f499=e32a#1f49a=e32b#1f49b=e32c#1f49c=e32d#1f49d=e437#1f49e=e327#1f49f=e204#2665=e20c#2660=e20e#2666=e20d#2663=e20f#1f6ac=e30e#1f6ad=e208#267f=e20a#26a0=e252#26d4=e137#1f6b2=e136#1f6b6=e201#1f6b9=e138#1f6ba=e139#1f6c0=e13f#1f6bb=e151#1f6bd=e140#1f6be=e309#1f6bc=e13a#1f192=e214#1f194=e229#1f195=e212#1f197=e24d#1f199=e213#1f19a=e12e#1f201=e203#1f202=e228#1f233=e22b#1f235=e22a#1f236=e215#1f21a=e216#1f237=e217#1f238=e218#1f239=e227#1f22f=e22c#1f23a=e22d#3299=e315#3297=e30d#1f250=e226#2716=e333#1f4a1=e10f#1f4a2=e334#1f4a3=e311#1f4a4=e13c#1f4a6=e331#1f4a7=e331#1f4a8=e330#1f4a9=e05a#1f4aa=e14c#1f4ab=e407#2728=e32e#2734=e205#2733=e206#26aa=e219#26ab=e219#1f534=e219#1f535=e21a#1f532=e21a#1f533=e21b#2b50=e32f#2b1c=e21b#2b1b=e21a#25ab=e21b#25aa=e21a#25fd=e21b#25fe=e21a#25fb=e21b#25fc=e21a#1f536=e21b#1f537=e21b#1f538=e21b#1f539=e21b#2747=e32e#1f50a=e141#1f50d=e114#1f50e=e114#1f512=e144#1f513=e145#1f50f=e144#1f510=e144#1f511=e03f#1f514=e325#1f519=e235#1f51d=e24c#270a=e010#270b=e012#270c=e011#1f44a=e00d#1f44d=e00e#261d=e00f#1f446=e22e#1f447=e22f#1f448=e230#1f449=e231#1f44b=e41e#1f44f=e41f#1f44c=e420#1f44e=e421#1f450=e422";
	// String[] souList = source.split("#");
	// for (String str : souList) {
	// try {
	// String[] kv = str.split("=");
	// String unicode = kv[0].toLowerCase(Locale.US);
	// String softbank = kv[1];
	// if (convertMap.containsValue(unicode)) {
	// if (unicodeToSoftbankMap.containsKey(unicode)) {
	// log("softbanToUnicode : " + unicode + ";" + str);
	// } else {
	// File src = new File("/sdcard/emoji_iphone/"
	// + softbank.toUpperCase() + ".png");
	// unicodeToSoftbankMap.put(unicode, softbank);
	// File dst = new File(
	// "/sdcard/emoji_iphone/unicode/emoji_"
	// + unicode.toLowerCase() + ".png");
	// if (!src.exists()) {
	// notExists.put(unicode, softbank);
	// } else {
	// if (!dst.exists()) {
	// copy(src, dst);
	// }
	// }
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// if (convertMap != null && !convertMap.isEmpty()) {
	// for (Entry<String, String> entry : convertMap.entrySet()) {
	//
	// }
	// }
	// }
	// log("softbanToUnicode()	time=" + (System.currentTimeMillis() - start)
	// + "; unicodeToSoftbankMap.size = "
	// + unicodeToSoftbankMap.size() + ";" + souList.length
	// + "; len = " + l + "; \n notExists " + notExists.size() + " \n"
	// + notExists.toString());
	// }
	//
	// private void copy(File src, File dst) throws IOException {
	// InputStream in = new FileInputStream(src);
	// OutputStream out = new FileOutputStream(dst);
	// byte[] buf = new byte[1024];
	// int len;
	// while ((len = in.read(buf)) > 0) {
	// out.write(buf, 0, len);
	// }
	// in.close();
	// out.close();
	// }

	private int[] toCodePointArray(String str) {
		char[] ach = str.toCharArray();
		int len = ach.length;
		// log("toCodePointArray() ach: " + len + " " + toString(ach));
		int[] acp = new int[Character.codePointCount(ach, 0, len)];
		// log("toCodePointArray() acp: " + acp.length + " " + toString(acp));
		int j = 0;
		for (int i = 0, cp; i < len; i += Character.charCount(cp)) {
			cp = Character.codePointAt(ach, i);
			acp[j++] = cp;
		}
		// log("toCodePointArray() acp: " + acp.length + " " + toString(acp));
		return acp;
	}

	private String parseEmoji(String input) {
		if (input == null || input.length() <= 0) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		int[] codePoints = toCodePointArray(input);
		String skey = "";
		for (int i = 0; i < codePoints.length; i++) {
			if (i + 1 < codePoints.length) {
				skey = codePoints[i] + "_" + codePoints[i + 1];
				// log("skey = " + skey);
				if (convertMap.containsKey(skey)) {
					String value = convertMap.get(skey);
					if (value != null) {
						result.append("[" + EMOJI_TAG + "]" + value + "[/"
								+ EMOJI_TAG + "]");
					}
					i++;
					continue;
				}
			}
			skey = codePoints[i] + "";
			if (convertMap.containsKey(skey)) {
				String value = convertMap.get(skey);
				if (value != null) {
					result.append("[" + EMOJI_TAG + "]" + value + "[/"
							+ EMOJI_TAG + "]");
				}
				continue;
			}
			result.append(Character.toChars(codePoints[i]));
		}
		return result.toString();
	}

	/**
	 * get emoji resource id by unicode of emoji
	 * 
	 * @time Jan 22, 2014
	 * 
	 * @param source
	 *            unicode of emoji
	 * @return
	 */
	public final static int getEmojiRes(String source) {
		if (Utils.isEmpty(source)) {
			return 0;
		}
		return Utils.getResourceId("zemoji_" + source);
	}

	/**
	 * unicode -> string
	 */
	private String convertUnicode(String emo) {
		try {
			if (emo == null || emo.length() == 0) {
				return null;
			}
			if (!emo.contains("_")) {
				return new String(Character.toChars(Integer.parseInt(emo, 16)));
			}
			String[] emos = emo.split("_");
			char[] char0 = Character.toChars(Integer.parseInt(emos[0], 16));
			char[] char1 = Character.toChars(Integer.parseInt(emos[1], 16));
			char[] emoji = new char[char0.length + char1.length];
			for (int i = 0; i < char0.length; i++) {
				emoji[i] = char0[i];
			}
			for (int i = char0.length; i < emoji.length; i++) {
				emoji[i] = char1[i - char0.length];
			}
			return new String(emoji);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String convertToMsg(CharSequence cs) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(cs);
		ImageSpan[] spans = ssb.getSpans(0, cs.length(), ImageSpan.class);
		for (int i = 0; i < spans.length; i++) {
			ImageSpan span = spans[i];
			int start = ssb.getSpanStart(span);
			int end = ssb.getSpanEnd(span);
			String s = ssb.subSequence(start, end) + "";// [emoji]2764[/emoji]
			String sc = span.getSource();// 2764
			if (sc == null) {
				continue;
			}
			if (s.contains("[" + EMOJI_TAG + "]")) {
				ssb.replace(start, end, convertUnicode(sc));
			}
		}
		ssb.clearSpans();
		return ssb.toString();
	}

	public SpannableStringBuilder convetToEmoji(String content, Context mContext) {
		String regex = "\\[" + EMOJI_TAG + "\\](.*?)\\[/" + EMOJI_TAG + "\\]";
		Pattern pattern = Pattern.compile(regex);
		String emo = "";
		Resources resources = mContext.getResources();
		String unicode = parseEmoji(content);
		Matcher matcher = pattern.matcher(unicode);
		SpannableStringBuilder sBuilder = new SpannableStringBuilder(unicode);
		Drawable drawable = null;
		ImageSpan span = null;
		while (matcher.find()) {
			emo = matcher.group();
			try {
				String source = emo.substring(emo.indexOf("]") + 1,
						emo.lastIndexOf("["));
				int id = getEmojiRes(source);
				if (id != 0) {
					drawable = resources.getDrawable(id);
					drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
							drawable.getIntrinsicHeight());
					span = new ImageSpan(drawable, source);
					sBuilder.setSpan(span, matcher.start(), matcher.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		return sBuilder;
	}

	public SpannableStringBuilder convetToHeart(SpannableStringBuilder content,
			final Context mContext) {
		ImageGetter imageGetter = new ImageGetter() {
			public Drawable getDrawable(String source) {
				Drawable d = mContext.getResources().getDrawable(
						R.drawable.zemoji_2764);
				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
				return d;
			}
		};
		CharSequence cs1 = Html.fromHtml("<img src='heart'/>", imageGetter,
				null);
		content.append(cs1);
		return content;
	}

	public final static void log(String msg) {
		int len = 0;
		int divider = 1024;
		if (msg == null || msg.length() == 0) {
		} else {
			len = msg.length();
		}
		if (len <= divider) {
			Log.d(EmojiParser.EMOJI_TAG, msg);
		} else {
			int start = 0;
			while (start <= len) {
				int end = (start + divider <= len) ? start + divider : len;
				String sub = msg.substring(start, end);
				LogUtil.d(EmojiParser.EMOJI_TAG, sub);
				start = start + divider;
			}
		}
	}

	public final static String toEmojiTag(String emoji) {
		if (Utils.isEmpty(emoji)) {
			return emoji;
		}
		return "[" + EmojiParser.EMOJI_TAG + "]" + emoji + "[/"
				+ EmojiParser.EMOJI_TAG + "]";
	}

	public String toString() {
		String result = "";
		log("----------------------------------to String()----------------------------------");
		if (convertMap != null && !convertMap.isEmpty()) {
			for (Entry<String, String> entry : convertMap.entrySet()) {
				String key = entry.getKey().toString();
				String value = entry.getValue().toString();
				log("key=" + key + " value=" + value);
			}
		}
		return result;
	}

}
