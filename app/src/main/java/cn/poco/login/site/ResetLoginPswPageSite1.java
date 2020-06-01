package cn.poco.login.site;

/**
 * Created by lgd on 2017/8/25.
 */

import android.content.Context;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 * 登录成功无返回动画的site
 */

public class ResetLoginPswPageSite1 extends ResetLoginPswPageSite {
    public void loginSucceed(Context context) {
        MyFramework.SITE_ClosePopup2(context, null, 2, Framework2.ANIM_TRANSLATION_LEFT);
    }
}
