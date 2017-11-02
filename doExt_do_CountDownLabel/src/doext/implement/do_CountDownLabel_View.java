package doext.implement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import core.helper.DoTextHelper;
import core.helper.DoUIModuleHelper;
import core.interfaces.DoIScriptEngine;
import core.interfaces.DoIUIModuleView;
import core.object.DoEventCenter;
import core.object.DoInvokeResult;
import core.object.DoUIModule;
import doext.define.do_CountDownLabel_IMethod;
import doext.define.do_CountDownLabel_MAbstract;

/**
 * 自定义扩展UIView组件实现类，此类必须继承相应VIEW类，并实现DoIUIModuleView,do_CountDownLabel_IMethod接口
 * ； #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.model.getUniqueKey());
 */
public class do_CountDownLabel_View extends TextView implements DoIUIModuleView, do_CountDownLabel_IMethod {

	/**
	 * 每个UIview都会引用一个具体的model实例；
	 */
	private do_CountDownLabel_MAbstract model;

	public do_CountDownLabel_View(Context context) {
		super(context);
	}

	/**
	 * 初始化加载view准备,_doUIModule是对应当前UIView的model实例
	 */
	@Override
	public void loadView(DoUIModule _doUIModule) throws Exception {
		this.model = (do_CountDownLabel_MAbstract) _doUIModule;
		this.setTextSize(TypedValue.COMPLEX_UNIT_PX, DoUIModuleHelper.getDeviceFontSize(_doUIModule, "17"));
		this.setTextColor(Color.BLACK);
		this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		this.setMaxLines(1);
		this.setEllipsize(TruncateAt.END);
	}

	/**
	 * 动态修改属性值时会被调用，方法返回值为true表示赋值有效，并执行onPropertiesChanged，否则不进行赋值；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public boolean onPropertiesChanging(Map<String, String> _changedValues) {
		return true;
	}

	/**
	 * 属性赋值成功后被调用，可以根据组件定义相关属性值修改UIView可视化操作；
	 * 
	 * @_changedValues<key,value>属性集（key名称、value值）；
	 */
	@Override
	public void onPropertiesChanged(Map<String, String> _changedValues) {
		DoUIModuleHelper.handleBasicViewProperChanged(this.model, _changedValues);
		DoUIModuleHelper.setFontProperty(this.model, _changedValues);
		if (_changedValues.containsKey("textAlign")) {
			String _textAlign = _changedValues.get("textAlign");
			if (_textAlign.equals("center")) {
				this.setGravity(Gravity.CENTER);
			} else if (_textAlign.equals("right")) {
				this.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
			} else {
				this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			}
		}
		if (_changedValues.containsKey("countDown")) {
			long _countDown = DoTextHelper.strToLong(_changedValues.get("countDown"), 0);
			getFormat(_countDown);
			start(_countDown);
		}
	}

	SimpleDateFormat sdf = null;

	private void getFormat(long data) {
		long second = data / 1000;
		if (second >= 0 && second < 3600) {
			sdf = new SimpleDateFormat("mm:ss.SSS", Locale.getDefault());
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		} else {
			sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String result = (String) msg.obj;
			do_CountDownLabel_View.this.setText(result);
		};
	};
	CountDownTimer countDownTimer = null;

	public void start(final long _countDown) {
		dispose();
		countDownTimer = new CountDownTimer(_countDown, 10) {
			@Override
			public void onTick(long l) {
				String result = getResult(l);
				sendMessage(result);
			}

			@Override
			public void onFinish() {
				String result = getResult(0);
				sendMessage(result);
				fireEvent();
			}

			private void sendMessage(String result) {
				Message message = new Message();
				message.obj = result;
				handler.sendMessage(message);
			}

			private String getResult(long time) {
				Date date = new Date(time);
				String formatStr = sdf.format(date);
				return formatStr.substring(0, formatStr.length() - 1);
			}
		};
		countDownTimer.start();
	}

	private void fireEvent() {
		DoEventCenter eventCenter = this.model.getEventCenter();
		if (eventCenter != null) {
			DoInvokeResult _invokeResult = new DoInvokeResult(this.model.getUniqueKey());
			eventCenter.fireEvent("finish", _invokeResult);
		}
	}

	private void dispose() {
		if (countDownTimer != null) {
			countDownTimer.cancel();
			countDownTimer = null;
		}
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		return false;
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V），获取参数值使用API提供DoJsonHelper类；
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.model.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) {
		return false;
	}

	/**
	 * 释放资源处理，前端JS脚本调用closePage或执行removeui时会被调用；
	 */
	@Override
	public void onDispose() {
		dispose();
	}

	/**
	 * 重绘组件，构造组件时由系统框架自动调用；
	 * 或者由前端JS脚本调用组件onRedraw方法时被调用（注：通常是需要动态改变组件（X、Y、Width、Height）属性时手动调用）
	 */
	@Override
	public void onRedraw() {
		this.setLayoutParams(DoUIModuleHelper.getLayoutParams(this.model));
	}

	/**
	 * 获取当前model实例
	 */
	@Override
	public DoUIModule getModel() {
		return model;
	}

}