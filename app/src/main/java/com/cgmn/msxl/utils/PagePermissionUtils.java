package com.cgmn.msxl.utils;

import android.app.Fragment;
import com.cgmn.msxl.R;
import com.cgmn.msxl.data.PageMainItem;
import com.cgmn.msxl.data.SplitItem;
import com.cgmn.msxl.service.GlobalDataHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagePermissionUtils {

    public static Map<String, String> getItemMap(Fragment fragment){
        Map<String, String> hash = new HashMap();
        hash.put(PageMainItem.LEADING_STRATEGY+"", String.format("%s##%s##trading", fragment.getString(R.string.dragTrain), fragment.getString(R.string.trading)));
        hash.put(PageMainItem.NORMAL_STRATEGY+"", String.format("%s##%s##trading", fragment.getString(R.string.tradTrain), fragment.getString(R.string.trading)));
        hash.put(PageMainItem.FREE_KLINE+"", String.format("%s##%s##trading", fragment.getString(R.string.free_kline), fragment.getString(R.string.trading)));
        hash.put(PageMainItem.FREE_TIME_SHARE+"", String.format("%s##%s##trading", fragment.getString(R.string.free_time_share), fragment.getString(R.string.trading)));

        hash.put(PageMainItem.SUM_PL_LINE+"", String.format("%s##%s##pl_rate_line", fragment.getString(R.string.train_sum_line), fragment.getString(R.string.pl_rate_line)));
        hash.put(PageMainItem.LEADING_LINE+"", String.format("%s##%s##pl_rate_line", fragment.getString(R.string.leading_line), fragment.getString(R.string.pl_rate_line)));
        hash.put(PageMainItem.NORMARL_LINE+"", String.format("%s##%s##pl_rate_line", fragment.getString(R.string.normal_line), fragment.getString(R.string.pl_rate_line)));
        hash.put(PageMainItem.FREE_RATE_CURV+"", String.format("%s##%s##pl_rate_line", fragment.getString(R.string.normal_kline_curv), fragment.getString(R.string.pl_rate_line)));

        hash.put(PageMainItem.TOTAL_RANK+"", String.format("%s##%s##ranking", fragment.getString(R.string.total_ranking), fragment.getString(R.string.ranking)));
        hash.put(PageMainItem.DAN_RANK+"", String.format("%s##%s##ranking", fragment.getString(R.string.dan_ranking), fragment.getString(R.string.ranking)));
        hash.put(PageMainItem.FREE_RANK_LIST+"", String.format("%s##%s##ranking", fragment.getString(R.string.free_rank_list), fragment.getString(R.string.ranking)));

        hash.put(PageMainItem.TREND_BREAK_UP+"", String.format("%s##%s##market", fragment.getString(R.string.trend_break_up), fragment.getString(R.string.market)));
        hash.put(PageMainItem.OPTIONAL_STOCKS+"", String.format("%s##%s##market", fragment.getString(R.string.optional_stock), fragment.getString(R.string.market)));

        hash.put(PageMainItem.MODEL_SETTING+"", String.format("%s##%s##mode_title", fragment.getString(R.string.mode_setting), fragment.getString(R.string.mode_title)));
        hash.put(PageMainItem.VIOLATE_MODE_DETAI+"", String.format("%s##%s##mode_title", fragment.getString(R.string.violate_mode_detai), fragment.getString(R.string.mode_title)));

        hash.put(PageMainItem.COMMENT_TO_ME+"", String.format("%s##%s##my_dynamic", fragment.getString(R.string.comment_to_me), fragment.getString(R.string.my_dynamic)));
        hash.put(PageMainItem.APPROVE_TO_ME+"", String.format("%s##%s##my_dynamic", fragment.getString(R.string.approve_to_me), fragment.getString(R.string.my_dynamic)));


        hash.put(PageMainItem.MY_GENERAL_INFO+"", String.format("%s##%s##personal", fragment.getString(R.string.acct_info), fragment.getString(R.string.personal)));
        hash.put(PageMainItem.PERSONAL_INFO+"", String.format("%s##%s##personal", fragment.getString(R.string.personal_info), fragment.getString(R.string.personal)));
        hash.put(PageMainItem.VIP_INFO+"", String.format("%s##%s##personal", fragment.getString(R.string.vip_way), fragment.getString(R.string.personal)));
        hash.put(PageMainItem.CHARGE_INFO+"", String.format("%s##%s##personal", fragment.getString(R.string.chargev_way), fragment.getString(R.string.personal)));


        hash.put(PageMainItem.CONTACT_US+"", String.format("%s##%s##system", fragment.getString(R.string.contact_us), fragment.getString(R.string.system)));
        hash.put(PageMainItem.CHECK_NEW_VERSION+"", String.format("%s##%s##system", fragment.getString(R.string.check_new_version), fragment.getString(R.string.system)));
        hash.put(PageMainItem.USER_AGREMENT+"", String.format("%s##%s##system", fragment.getString(R.string.user_agrement_title), fragment.getString(R.string.system)));

        return hash;
    }

    public static ArrayList<Object> getPageDatas(List<Integer> preList, Fragment fragment){
        Map<String, String> hash = getItemMap(fragment);
        List<String> list = (List<String>) GlobalDataHelper.getData("pagePermissions");
        if(list == null || list.isEmpty()){
            return null;
        }
        ArrayList<Object> datas = new ArrayList<>();
        String currentGroup = null;
        for(Integer pageType : preList){
            if(list.contains(pageType+"")){
                String text = hash.get(pageType+"");
                String[] arr = text.split("##");
                if(currentGroup == null || !currentGroup.equals(arr[2])){
                    datas.add(new SplitItem(arr[1]));
                    currentGroup = arr[2];
                }
                datas.add(new PageMainItem(R.drawable.item_header, arr[0], pageType));
            }
        }

        return datas;
    }
}
