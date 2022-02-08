package org.gluu.casa.plugins.emailotp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

/**
 * This class is a custom ZK converter employed to display emails in a masked format.
 * <code>String</code> to <code>String</code> conversion.
 */
public class EmailMaskingConverter implements Converter {

    /**
     * This method is called when conversion (<code>String</code> to <code>String</code>) is taking placing in
     * <code>.zul</code> templates.
     * @param val An String object representing email
     * @param comp The UI component associated to this converstion
     * @param ctx Binding context. It holds the conversion arguments: "format" and "offset" are used here
     * @return A string with the email masked
     */
    public Object coerceToUi(Object val, Component comp, BindContext ctx) {

    		String pattern = "([^@]+)@(.*)\\.(.*)";

    		Pattern r = Pattern.compile(pattern);
    		Matcher m = r.matcher(String.valueOf(val));
    		if (m.find()) {
    			StringBuilder sb = new StringBuilder("");
    			sb.append(m.group(1).charAt(0));
    			sb.append(m.group(1).substring(1).replaceAll(".", "*"));
    			sb.append("@");

    			sb.append(m.group(2).charAt(0));
    			sb.append(m.group(2).substring(1).replaceAll(".", "*"));

    			sb.append(".").append(m.group(3));
    			return sb.toString();
    		}
    		return null;

    }

    /**
     * This method is called when converting <code>String</code> to <code>Date</code>.
     * @param val  A masked email
     * @param comp Associated component
     * @param ctx Bind context for associate Binding and extra parameter (e.g. format)
     * @return null (conversion not implemented)
     */
    public Object coerceToBean(Object val, Component comp, BindContext ctx) {
        return null;
    }

}
