/*
 * LICENCE[[
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1/CeCILL 2.O 
 *
 * The contents of this file are subject to the Mozilla Public License Version 
 * 1.1 (the "License"); you may not use this file except in compliance with 
 * the License. You may obtain a copy of the License at http://www.mozilla.org/MPL/ 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis, 
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
 * for the specific language governing rights and limitations under the 
 * License. 
 * 
 * The Original Code is kelis.fr code. 
 * 
 * The Initial Developer of the Original Code is 
 * sylvain.spinelli@kelis.fr 
 * 
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved. 
 * 
 * Contributor(s): thibaut.arribe@kelis.fr
 * 
 * 
 * Alternatively, the contents of this file may be used under the terms of 
 * either of the GNU General Public License Version 2 or later (the "GPL"), 
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"), 
 * or the CeCILL Licence Version 2.0 (http://www.cecill.info/licences.en.html), 
 * in which case the provisions of the GPL, the LGPL or the CeCILL are applicable 
 * instead of those above. If you wish to allow use of your version of this file 
 * only under the terms of either the GPL or the LGPL, and not to allow others 
 * to use your version of this file under the terms of the MPL, indicate your 
 * decision by deleting the provisions above and replace them with the notice 
 * and other provisions required by the GPL or the LGPL. If you do not delete 
 * the provisions above, a recipient may use your version of this file under 
 * the terms of any one of the MPL, the GPL, the LGPL or the CeCILL.
 * ]]LICENCE
 */
package eu.scenari.gephi.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import eu.scenari.commons.util.coder.HexaUtils;
//import eu.scenari.commons.util.lang.Exception;

/**
 * Parser Json.
 * Cette implémentation de ce parser transforme en objet java : 
 * <ul>
 * <li> {@link HashMap<String, Object>} pour un Object json.
 * <li> {@link ArrayList<Object>} pour un Array json.
 * <li> {@link String} pour une string json.
 * <li> {@link Integer} or {@link Double} pour un number json.
 * <li> {@link Boolean} pour un boolean json.
 * <li> <code>null</code> pour une valeur null json.
 * </ul>
 * 
 * Note : Inspiré de la classe org.mozilla.javascript.json.JsonParser
 */
public class JsonParser {

	protected int fPos;

	protected int fLength;

	protected CharSequence fSrc;

	public JsonParser() {
	}

	public Object parseValue(CharSequence pJson) throws Exception {
		if (pJson == null) throw new Exception("Input string may not be null");
		fPos = 0;
		fLength = pJson.length();
		fSrc = pJson;
		Object vValue = readValue();
		consumeWhitespace();
		if (fPos < fLength) throw new Exception("Expected end of stream at char " + fPos);
		return vValue;
	}

	protected Object readValue() throws Exception{
		consumeWhitespace();
		while (fPos < fLength) {
			char vC = fSrc.charAt(fPos++);
			switch (vC) {
			case '{':
				return readObject();
			case '[':
				return readArray();
			case 't':
				return readTrue();
			case 'f':
				return readFalse();
			case '"':
				return readString();
			case 'n':
				return readNull();
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case '0':
			case '-':
				return readNumber(vC);
			default:
				throw new Exception("Unexpected token: " + vC);
			}
		}
		throw new Exception("Empty JSON string");
	}

	protected Object readObject() throws Exception{
		Map<String, Object> vObject = new HashMap<String, Object>();
		boolean vNeedsComma = false;
		consumeWhitespace();
		while (fPos < fLength) {
			char vCar = fSrc.charAt(fPos++);
			switch (vCar) {
			case '}':
				return vObject;
			case ',':
				if (!vNeedsComma) { throw new Exception("Unexpected comma in object literal"); }
				vNeedsComma = false;
				break;
			case '"':
				if (vNeedsComma) { throw new Exception("Missing comma in object literal"); }
				String vKey = readString();
				consume(':');
				vObject.put(vKey, readValue());
				vNeedsComma = true;
				break;
			default:
				throw new Exception("Unexpected token in object literal");
			}
			consumeWhitespace();
		}
		throw new Exception("Unterminated object literal");
	}

	protected Object readArray() throws Exception{
		List<Object> vList = new ArrayList<Object>();
		boolean vNeedsComma = false;
		consumeWhitespace();
		while (fPos < fLength) {
			char vCar = fSrc.charAt(fPos);
			switch (vCar) {
			case ']':
				fPos++;
				return vList;
			case ',':
				if (!vNeedsComma) { throw new Exception("Unexpected comma in array literal"); }
				vNeedsComma = false;
				fPos++;
				break;
			default:
				if (vNeedsComma) { throw new Exception("Missing comma in array literal"); }
				vList.add(readValue());
				vNeedsComma = true;
			}
			consumeWhitespace();
		}
		throw new Exception("Unterminated array literal");
	}

	protected String readString() throws Exception{
		StringBuilder vBuf = new StringBuilder();
		while (fPos < fLength) {
			char vC = fSrc.charAt(fPos++);
			if (vC <= '\u001F') { throw new Exception("String contains control character"); }
			switch (vC) {
			case '\\':
				if (fPos >= fLength) { throw new Exception("Unterminated string"); }
				vC = fSrc.charAt(fPos++);
				switch (vC) {
				case '"':
					vBuf.append('"');
					break;
				case '\'':
					vBuf.append('\'');
					break;
				case '\\':
					vBuf.append('\\');
					break;
				case '/':
					vBuf.append('/');
					break;
				case 'b':
					vBuf.append('\b');
					break;
				case 'f':
					vBuf.append('\f');
					break;
				case 'n':
					vBuf.append('\n');
					break;
				case 'r':
					vBuf.append('\r');
					break;
				case 't':
					vBuf.append('\t');
					break;
				case 'u':
					if (fLength - fPos < 5) { throw new Exception("Invalid character code: \\u" + fSrc.subSequence(fPos, fLength)); }
					try {
						vBuf.append((char) JsonParser.decodeHexa(fSrc.charAt(fPos++), fSrc.charAt(fPos++), fSrc.charAt(fPos++), fSrc.charAt(fPos++)));
					} catch (NumberFormatException nfx) {
						throw new Exception("Invalid character code: " + fSrc.subSequence(fPos, fPos + 4));
					}
					break;
				default:
					throw new Exception("Unexpected character in string: '\\" + vC + "'");
				}
				break;
			case '"':
				return vBuf.toString();
			default:
				vBuf.append(vC);
				break;
			}
		}
		throw new Exception("Unterminated string literal");
	}

	protected Number readNumber(char pFirst) throws Exception{
		StringBuilder vBuf = new StringBuilder();
		vBuf.append(pFirst);
		while (fPos < fLength) {
			char vCar = fSrc.charAt(fPos);
			if (!Character.isDigit(vCar) && vCar != '-' && vCar != '+' && vCar != '.' && vCar != 'e' && vCar != 'E') {
				break;
			}
			fPos += 1;
			vBuf.append(vCar);
		}
		String vNum = vBuf.toString();
		try {
			int vNumLength = vNum.length();
			// check for leading zeroes
			for (int i = 0; i < vNumLength; i++) {
				char vC = vNum.charAt(i);
				if (Character.isDigit(vC)) {
					if (vC == '0' && vNumLength > i + 1 && Character.isDigit(vNum.charAt(i + 1))) throw new Exception("Unsupported number format: " + vNum);
					break;
				}
			}
			final double vDval = Double.parseDouble(vNum);
			final int vIval = (int) vDval;
			if (vIval == vDval) {
				return Integer.valueOf(vIval);
			} else {
				return Double.valueOf(vDval);
			}
		} catch (NumberFormatException e) {
			throw new Exception("Unsupported number format: " + vNum);
		}
	}

	protected Boolean readTrue() throws Exception{
		if (fLength - fPos < 3 || fSrc.charAt(fPos) != 'r' || fSrc.charAt(fPos + 1) != 'u' || fSrc.charAt(fPos + 2) != 'e') { throw new Exception("Unexpected token: t"); }
		fPos += 3;
		return Boolean.TRUE;
	}

	protected Boolean readFalse() throws Exception{
		if (fLength - fPos < 4 || fSrc.charAt(fPos) != 'a' || fSrc.charAt(fPos + 1) != 'l' || fSrc.charAt(fPos + 2) != 's' || fSrc.charAt(fPos + 3) != 'e') { throw new Exception("Unexpected token: f"); }
		fPos += 4;
		return Boolean.FALSE;
	}

	protected Object readNull() throws Exception{
		if (fLength - fPos < 3 || fSrc.charAt(fPos) != 'u' || fSrc.charAt(fPos + 1) != 'l' || fSrc.charAt(fPos + 2) != 'l') { throw new Exception("Unexpected token: n"); }
		fPos += 3;
		return null;
	}

	protected void consumeWhitespace() {
		while (fPos < fLength) {
			char vCar = fSrc.charAt(fPos);
			switch (vCar) {
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				fPos++;
				break;
			default:
				return;
			}
		}
	}

	protected void consume(char pToken) throws Exception{
		consumeWhitespace();
		if (fPos >= fLength) { throw new Exception("Expected " + pToken + " but reached end of stream"); }
		char vCar = fSrc.charAt(fPos++);
		if (vCar == pToken) {
			return;
		} else {
			throw new Exception("Expected " + pToken + " found " + vCar);
		}
	}
        
        /**
	 * Décode un char hexadécimal (0 -> F).
	 */
	public static int decodeHexa(char pChar1) throws Exception{
		int v1 = pChar1 - 48;
		if (v1 > 9) {
			//Majuscules
			v1 -= 7;
			if (v1 > 15) {
				//Minuscules
				v1 -= 32;
			}
		}
		if (v1 < 0 || v1 > 15) throw new Exception("Not an hexadecimal character : " + pChar1);
		return v1;
	}
        /**
	 * Décode 2 chars de type hexadécimal (00 -> FF).
	 */
	public static int decodeHexa(char pChar1, char pChar2) throws Exception {
		return (decodeHexa(pChar1) << 4) + decodeHexa(pChar2);
	}

	/**
	 * Décode 4 chars de type hexadécimal (0000 -> FFFF).
	 */
	public static int decodeHexa(char pChar1, char pChar2, char pChar3, char pChar4) throws Exception {
		return (decodeHexa(pChar1) << 12) + (decodeHexa(pChar2) << 8) + (decodeHexa(pChar3) << 4) + decodeHexa(pChar4);
        }
}
