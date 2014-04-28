package de.guj.ems.mobile.sdk.controllers.adserver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import de.guj.ems.mobile.sdk.R;
import de.guj.ems.mobile.sdk.controllers.AdViewConfiguration;
import de.guj.ems.mobile.sdk.controllers.IOnAdEmptyListener;
import de.guj.ems.mobile.sdk.controllers.IOnAdErrorListener;
import de.guj.ems.mobile.sdk.controllers.IOnAdSuccessListener;
import de.guj.ems.mobile.sdk.controllers.backfill.BackfillDelegator;
import de.guj.ems.mobile.sdk.util.SdkGlobals;
import de.guj.ems.mobile.sdk.util.SdkLog;
import de.guj.ems.mobile.sdk.util.SdkUtil;
import de.guj.ems.mobile.sdk.views.GuJEMSAdView;

/**
 * Base class for mapping various available data to adserver parameters.
 * 
 * @see de.guj.ems.mobile.sdk.controllers.adserver.IAdServerSettingsAdapter
 * 
 * @author stein16
 * 
 */
public abstract class AdServerSettingsAdapter implements
		IAdServerSettingsAdapter {

	private String requestQueryString;

	private static final long serialVersionUID = 314048983271226769L;

	private final static String TAG = "AdServerSettingsAdapter";

	private String queryAppendix;

	private final Map<String, String> attrsToParams;

	private BackfillDelegator.BackfillData directBackfill;

	private IOnAdEmptyListener onAdEmptyListener = null;

	private IOnAdSuccessListener onAdSuccessListener = null;

	private IOnAdErrorListener onAdErrorListener = null;

	private final Map<String, String> paramValues;

	private JSONArray regExps;

	private Context context;

	protected Class<?> viewClass;

	private int[] viewMetrics = { -1, -1, -1 };

	@SuppressWarnings("unused")
	private AdServerSettingsAdapter() {
		this.context = null;
		this.viewClass = null;
		this.requestQueryString = null;
		this.paramValues = new HashMap<String, String>();
		this.attrsToParams = new HashMap<String, String>();
	}

	/**
	 * Constructor when creating the settings in an Android View
	 * 
	 * @param context
	 *            application context
	 * @param set
	 *            inflated layout parameters
	 */
	public AdServerSettingsAdapter(Context context, AttributeSet set,
			Class<?> viewClass) {
		if (SdkUtil.getContext() == null) {
			SdkUtil.setContext(context);
		}
		this.context = context;
		this.viewClass = viewClass;
		this.requestQueryString = null;
		this.paramValues = new HashMap<String, String>();
		this.attrsToParams = this.init(set);
	}

	/**
	 * Constructor when creating the settings from an Android Activity
	 * 
	 * @param context
	 *            application context
	 * @param savedInstance
	 *            saved instance state
	 */
	public AdServerSettingsAdapter(Context context, Bundle savedInstance,
			Class<?> viewClass) {
		if (SdkUtil.getContext() == null) {
			SdkUtil.setContext(context);
		}
		this.context = context;
		this.viewClass = viewClass;
		this.requestQueryString = null;
		this.paramValues = new HashMap<String, String>();
		this.attrsToParams = this.init(savedInstance);
	}

	@Override
	public void addCustomRequestParameter(String param, double value) {
		addCustomRequestParameter(param, String.valueOf(value));
	}

	@Override
	public void addCustomRequestParameter(String param, int value) {
		addCustomRequestParameter(param, String.valueOf(value));
	}

	@Override
	public void addCustomRequestParameter(String param, String value) {
		putAttrToParam(param, param);
		putAttrValue(param, value);
	}

	private final void createEmptyListener(final String lMethodName) {
		this.onAdEmptyListener = new IOnAdEmptyListener() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onAdEmpty() {
				try {
					Class<?>[] noParams = null;
					Object[] noArgs = null;
					Method lMethod = context.getClass().getMethod(lMethodName,
							noParams);
					lMethod.invoke(context, noArgs);
				} catch (NoSuchMethodException nsme) {
					SdkLog.e(TAG, "OnAdEmptyListener " + lMethodName
							+ " not found. Check your xml.", nsme);

				} catch (InvocationTargetException ivte) {
					SdkLog.e(TAG, "OnAdEmptyListener could not be invoked",
							ivte);
				} catch (IllegalAccessException iae) {
					SdkLog.e(TAG, "OnAdEmptyListener could not be accessed",
							iae);
				}

			}
		};
		SdkLog.d(TAG, "Created onEmptyListener \"" + lMethodName + "\"");
	}

	private final void createEmptyListener(final Object listener) {
		try {
			this.onAdEmptyListener = (IOnAdEmptyListener) listener;
		} catch (Exception e) {
			SdkLog.e(TAG, "Error setting onAdEmptyListener", e);
		}
	}

	private final void createErrorListener(final Object listener) {
		try {
			this.onAdErrorListener = (IOnAdErrorListener) listener;
		} catch (Exception e) {
			SdkLog.e(TAG, "Error setting onAdErrorListener", e);
		}
	}

	private final void createSuccessListener(final Object listener) {
		try {
			this.onAdSuccessListener = (IOnAdSuccessListener) listener;
		} catch (Exception e) {
			SdkLog.e(TAG, "Error setting onAdSuccessListener", e);
		}
	}

	private final void createSuccessListener(final String lMethodName) {
		this.onAdSuccessListener = new IOnAdSuccessListener() {

			private static final long serialVersionUID = 2L;

			@Override
			public void onAdSuccess() {
				try {
					Class<?>[] noParams = null;
					Object[] noArgs = null;
					
					Method lMethod = context.getClass().getMethod(lMethodName,
							noParams);
					lMethod.invoke(context, noArgs);
				} catch (NoSuchMethodException nsme) {
					SdkLog.e(TAG, "OnAdSuccessListener " + lMethodName
							+ " not found. Check your xml.", nsme);

				} catch (InvocationTargetException ivte) {
					SdkLog.e(TAG, "OnAdSuccessListener could not be invoked",
							ivte);
				} catch (IllegalAccessException iae) {
					SdkLog.e(TAG, "OnAdSuccessListener could not be accessed",
							iae);
				}

			}
		};
		SdkLog.d(TAG, "Created onSuccessListener \"" + lMethodName + "\"");
	}

	private final void createErrorListener(final String lMethodName) {
		this.onAdErrorListener = new IOnAdErrorListener() {

			private static final long serialVersionUID = 3L;

			@Override
			public void onAdError(String msg, Throwable t) {
				try {
					Method lMethod = context.getClass().getMethod(lMethodName,
							String.class, Throwable.class);
					lMethod.invoke(context, msg, t);
				} catch (NoSuchMethodException nsme) {
					SdkLog.e(TAG, "OnAdErrorListener " + lMethodName
							+ " not found. Check your xml.", nsme);

				} catch (InvocationTargetException ivte) {
					SdkLog.e(TAG, "OnAdErrorListener could not be invoked",
							ivte);
				} catch (IllegalAccessException iae) {
					SdkLog.e(TAG, "OnAdErrorListener could not be accessed",
							iae);
				}

			}

			@Override
			public void onAdError(String msg) {
				try {
					Method lMethod = context.getClass().getMethod(lMethodName,
							String.class);
					lMethod.invoke(context, msg);
				} catch (NoSuchMethodException nsme) {
					SdkLog.e(TAG, "OnAdErrorListener " + lMethodName
							+ " not found. Check your xml.", nsme);

				} catch (InvocationTargetException ivte) {
					SdkLog.e(TAG, "OnAdErrorListener could not be invoked",
							ivte);
				} catch (IllegalAccessException iae) {
					SdkLog.e(TAG, "OnAdErrorListener could not be accessed",
							iae);
				}

			}
		};
		SdkLog.d(TAG, "Created onErrorListener \"" + lMethodName + "\"");
	}

	protected Map<String, String> getAttrsToParams() {
		return this.attrsToParams;
	}

	@Override
	public String getCookieRepl() {
		return SdkUtil.getCookieReplStr();
	}

	@Override
	public IOnAdEmptyListener getOnAdEmptyListener() {
		return this.onAdEmptyListener;
	};

	@Override
	public IOnAdSuccessListener getOnAdSuccessListener() {
		return this.onAdSuccessListener;
	}

	@Override
	public IOnAdErrorListener getOnAdErrorListener() {
		return this.onAdErrorListener;
	}

	@Override
	public String getQueryString() {
		if (this.requestQueryString == null
				|| this.requestQueryString.length() <= 1) {
			this.requestQueryString = "";
			Iterator<String> keys = getAttrsToParams().keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				String val = paramValues.get(key);
				String param = attrsToParams.get(key);
				if (val != null) {
					SdkLog.d(TAG, "Adding: \"" + val + "\" as \"" + param
							+ "\" for " + key);
					this.requestQueryString += "&" + param + "=" + val;
				}
			}
			if (viewMetrics[2] > 0) {
				this.requestQueryString += "&w=" + viewMetrics[0];
				this.requestQueryString += "&h=" + viewMetrics[1];
				this.requestQueryString += "&d=" + viewMetrics[2];
			}
			if (regExps != null) {
				String backup = this.requestQueryString;
				try {
					for (int i = 0; i < regExps.length(); i++) {
						JSONArray regexpn = regExps.getJSONArray(i);
						this.requestQueryString = this.requestQueryString
								.replaceAll(regexpn.getString(0),
										regexpn.getString(1));
					}
				} catch (Exception e) {
					SdkLog.e(
							TAG,
							"Error applying regular expressions to query string.",
							e);
					return backup;
				}
			}

		}

		return this.requestQueryString;
	}

	@Override
	public String getRequestUrl() {
		String query = getBaseQueryString();
		String app = getQueryAppendix();
		return getBaseUrlString() + (query != null ? query : "") + getQueryString()
				+ (app != null ? app : "");
	}

	protected final Map<String, String> init(AttributeSet attrs) {
		Map<String, String> map = new HashMap<String, String>();
		if (attrs != null) {
			for (int i = 0; i < attrs.getAttributeCount(); i++) {
				String attr = attrs.getAttributeName(i);
				if (attr != null
						&& attr.startsWith(SdkGlobals.EMS_ATTRIBUTE_PREFIX)) {
					if (attr.startsWith(SdkGlobals.EMS_LISTENER_PREFIX)) {
						String lName = attr.substring(4);
						TypedArray tVals = viewClass.equals(GuJEMSAdView.class) ? context
								.obtainStyledAttributes(attrs,
										R.styleable.GuJEMSAdView) : context
								.obtainStyledAttributes(attrs,
										R.styleable.GuJEMSNativeAdView);
						if (lName.equals(SdkGlobals.EMS_SUCCESS_LISTENER)) {
							createSuccessListener(tVals
									.getString(AdViewConfiguration.getConfig(
											viewClass).getSuccessListenerId()));
						} else if (lName.equals(SdkGlobals.EMS_EMPTY_LISTENER)) {
							createEmptyListener(tVals
									.getString(AdViewConfiguration.getConfig(
											viewClass).getEmptyListenerId()));
						} else if (lName.equals(SdkGlobals.EMS_ERROR_LISTENER)) {
							createErrorListener(tVals
									.getString(AdViewConfiguration.getConfig(
											viewClass).getErrorListenerId()));
						}

						else {
							SdkLog.w(TAG, "Unknown listener type name: "
									+ lName);
						}
						tVals.recycle();

					} else {
						map.put(attr.substring(4), attr.substring(4));
						SdkLog.d(TAG,
								"Found AdView attribute " + attr.substring(4));
					}

				}
			}
		}
		return map;
	}

	protected final Map<String, String> init(Bundle savedInstance) {

		Map<String, String> map = new HashMap<String, String>();
		if (savedInstance != null && !savedInstance.isEmpty()) {
			Iterator<String> iterator = savedInstance.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				if (key.startsWith(SdkGlobals.EMS_ATTRIBUTE_PREFIX)) {
					if (key.startsWith(SdkGlobals.EMS_LISTENER_PREFIX)) {
						String lName = key.substring(4);
						if (lName.equals(SdkGlobals.EMS_SUCCESS_LISTENER)) {
							Object l = savedInstance
									.get(SdkGlobals.EMS_ATTRIBUTE_PREFIX
											+ SdkGlobals.EMS_SUCCESS_LISTENER);
							if (String.class.equals(l.getClass())) {
								createSuccessListener((String) l);
							} else {
								createSuccessListener(l);
							}
						} else if (lName.equals(SdkGlobals.EMS_EMPTY_LISTENER)) {
							Object l = savedInstance
									.get(SdkGlobals.EMS_ATTRIBUTE_PREFIX
											+ SdkGlobals.EMS_EMPTY_LISTENER);
							if (String.class.equals(l.getClass())) {
								createEmptyListener((String) l);
							} else {
								createEmptyListener(l);
							}
						} else if (lName.equals(SdkGlobals.EMS_ERROR_LISTENER)) {
							Object l = savedInstance
									.get(SdkGlobals.EMS_ATTRIBUTE_PREFIX
											+ SdkGlobals.EMS_ERROR_LISTENER);
							if (String.class.equals(l.getClass())) {
								createErrorListener((String) l);
							} else {
								createErrorListener(l);
							}
						} else {
							SdkLog.w(TAG, "Unknown listener type name: "
									+ lName);
						}
					} else {
						map.put(key.substring(4), key.substring(4));
						SdkLog.d(TAG,
								"Found AdView attribute " + key.substring(4));
					}

				}
			}
		}
		return map;
	}

	@Override
	public void putAttrToParam(String attr, String param) {
		this.attrsToParams.put(attr, param);
	}

	@Override
	public void putAttrValue(String attr, String value) {
		this.paramValues.put(attr, value);
	}

	@Override
	public void setOnAdEmptyListener(IOnAdEmptyListener l) {
		this.onAdEmptyListener = l;
	}

	@Override
	public void setOnAdSuccessListener(IOnAdSuccessListener l) {
		this.onAdSuccessListener = l;

	}

	@Override
	public void setOnAdErrorListener(IOnAdErrorListener l) {
		this.onAdErrorListener = l;

	}

	@Override
	public BackfillDelegator.BackfillData getDirectBackfill() {
		return directBackfill;
	}

	@Override
	public void setDirectBackfill(BackfillDelegator.BackfillData directBackfill) {
		this.directBackfill = directBackfill;
	}

	@Override
	public void addCustomParams(Map<String, ?> params) {
		if (params != null) {
			Iterator<String> mi = params.keySet().iterator();
			while (mi.hasNext()) {
				String param = mi.next();
				Object value = params.get(param);
				if (value.getClass().equals(String.class)) {
					addCustomRequestParameter(param, (String) value);
				} else if (value.getClass().equals(Double.class)) {
					addCustomRequestParameter(param,
							((Double) value).doubleValue());
				} else if (value.getClass().equals(Integer.class)) {
					addCustomRequestParameter(param,
							((Integer) value).intValue());
				} else {
					SdkLog.e(TAG,
							"Unknown object in custom params. Only String, Integer, Double allowed.");
				}
			}
		} else {
			SdkLog.w(TAG, "Custom params constructor used with null-array.");
		}
	}

	@Override
	public String toString() {
		return getQueryString();
	}

	@Override
	public void addRegexp(JSONArray regexp) {
		this.regExps = regexp;
	}

	@Override
	public Map<String, String> getParams() {
		return this.paramValues;
	}

	@Override
	public String getQueryAppendix() {
		return this.queryAppendix;
	}

	@Override
	public void addQueryAppendix(String str) {
		if (this.queryAppendix != null) {
			this.queryAppendix = this.queryAppendix.concat(str);
		} else {
			this.queryAppendix = str;
		}
	}

	@Override
	public int[] getAdViewMetrics() {
		return viewMetrics;
	}

	/**
	 * Define adview width in pixels
	 * 
	 * @param w
	 *            width in pixels
	 */
	public void setAdViewWidth(int w) {
		viewMetrics[0] = w;
	}

	/**
	 * Define adview height in pixels
	 * 
	 * @param h
	 *            height in pixels
	 */
	public void setAdViewHeight(int h) {
		viewMetrics[1] = h;
	}

	/**
	 * Define adview resolution dots per inch
	 * 
	 * @param d
	 *            resolution in dots per inch
	 */
	public void setAdViewDpi(int d) {
		viewMetrics[2] = d;
	}

	/**
	 * Set adview metrics
	 * 
	 * @param w
	 *            width in pixels
	 * @param h
	 *            height in pixels
	 * @param d
	 *            resolution in dots per inch
	 */
	public void setAdViewMetrics(int w, int h, int d) {
		viewMetrics[0] = w;
		viewMetrics[1] = h;
		viewMetrics[2] = d;
	}

	/**
	 * Set adview metrics
	 * 
	 * @param m
	 *            array of width, height and resolution
	 */
	public void setAdViewMetrics(int[] m) {
		viewMetrics[0] = m[0];
		viewMetrics[1] = m[1];
		viewMetrics[2] = m[2];
	}

}
