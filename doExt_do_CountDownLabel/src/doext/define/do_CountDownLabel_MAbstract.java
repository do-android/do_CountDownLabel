package doext.define;

import core.object.DoUIModule;
import core.object.DoProperty;
import core.object.DoProperty.PropertyDataType;


public abstract class do_CountDownLabel_MAbstract extends DoUIModule{

	protected do_CountDownLabel_MAbstract() throws Exception {
		super();
	}
	
	/**
	 * 初始化
	 */
	@Override
	public void onInit() throws Exception{
        super.onInit();
        //注册属性
		this.registProperty(new DoProperty("countDown", PropertyDataType.Number, "", false));
		this.registProperty(new DoProperty("fontColor", PropertyDataType.String, "000000FF", false));
		this.registProperty(new DoProperty("fontSize", PropertyDataType.Number, "17", false));
		this.registProperty(new DoProperty("textAlign", PropertyDataType.String, "left", true));
	}
}