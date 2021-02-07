package com.cgmn.msxl.comp.adpter;

import android.content.Context;
import android.widget.TextView;
import com.cgmn.msxl.R;
import com.cgmn.msxl.data.CatchItem;
import com.xj.marqueeview.base.CommonAdapter;
import com.xj.marqueeview.base.ViewHolder;

import java.util.List;

public class MarqueeAdapter extends CommonAdapter<CatchItem> {

    public MarqueeAdapter(Context context, List<CatchItem> nodes) {
        super(context, R.layout.marquee_item_view, nodes);
    }

    @Override
    protected void convert(ViewHolder viewHolder, CatchItem item, int position) {
        TextView tv = viewHolder.getView(R.id.tv);
        tv.setText(item.getText());
    }
}
