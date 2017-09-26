package com.dmsys.airdiskpro.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.dmsys.airdiskpro.model.ImageFolder;
import com.dmsys.airdiskpro.model.MediaInfo;
import com.dmsys.airdiskpro.ui.UploadBaseActivity.Mode;
import com.dmsys.airdiskpro.ui.UploadBaseActivity.OnSelectChangeListener;
import com.dmsys.airdiskpro.view.PicImageView;
import com.dmsys.dmsdk.model.DMFile;
import com.dmsys.mainbusiness.R;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.StringTokenizer;

import cn.dm.longsys.library.imageloader.core.DMImageLoader;
import cn.dm.longsys.library.imageloader.core.DisplayImageOptions;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class DatePictrueAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {

    private final Context mContext;
    //	private String[] mCountries;
    // 记录有多少组
    private int[] mSectionIndices;
    //对应每一个组的首字母
    private String[] mSectionLetters;
    private LayoutInflater mInflater;

    private ArrayList<ImageFolder> list = new ArrayList<ImageFolder>();
    private int width;
    DMImageLoader mDMImageLoader;
    DisplayImageOptions mLoaderOptions;
    private OnSelectChangeListener mOnSelectChangeListener;


    public void setOnSelectChangeListener(
            OnSelectChangeListener mOnSelectChangeListener) {
        this.mOnSelectChangeListener = mOnSelectChangeListener;
    }


    private Mode mMode = Mode.MODE_NORMAL;


    public Mode getmMode() {
        return mMode;
    }

    public void setmMode(Mode mMode) {
        this.mMode = mMode;
    }

    public DatePictrueAdapter(Context context, ArrayList<ImageFolder> list, int width) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
//		mCountries = context.getResources().getStringArray(R.array.countries);
        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
        this.list = list;
        this.width = width;

        mDMImageLoader = DMImageLoader.getInstance();
        initLoaderOptions();
    }

    private void initLoaderOptions() {
        /**
         * imageloader的新包导入
         */
        mLoaderOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageOnFail(R.drawable.filemanager_photo_fail)
                .useThumb(true).cacheOnDisk(true)
                .showImageOnLoading(R.drawable.ready_to_loading_image)
                .showImageForEmptyUri(R.drawable.filemanager_photo_fail)
                .build();
    }

    /**
     * @return
     * @function：获取每一个组的开始的单词的位置
     */
    private int[] getSectionIndices() {
        if (list.size() <= 0) return new int[0];
        ArrayList<Integer> sectionIndices = new ArrayList<Integer>();
        long lastDateParentId = list.get(0).getDateParentId();
        sectionIndices.add(0);
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).getDateParentId() != lastDateParentId) {
                lastDateParentId = list.get(i).getDateParentId();
                sectionIndices.add(i);
            }
        }
        int[] sections = new int[sectionIndices.size()];
        for (int i = 0; i < sectionIndices.size(); i++) {
            sections[i] = sectionIndices.get(i);
        }
        return sections;
    }

    /**
     * 根据首字母开始的第一个单词的位置记录，得出这一组的首字符
     *
     * @return
     * @function：获取各个组的首字符
     */
    private String[] getSectionLetters() {
        String[] letters = new String[mSectionIndices.length];
        for (int i = 0; i < mSectionIndices.length; i++) {
            letters[i] = list.get(i).Date;
        }
        return letters;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.file_browse_date_pic_item, parent, false);

            initHolder(holder, convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

//		holder.text.setText(list.get(position).mName);
        loadData2Holder(holder, list.get(position));

        return convertView;
    }


    private void loadData2Holder(ViewHolder holder, ImageFolder mPictrueGroup) {
        resetImageRLSize(holder.rlPicItem0, width);
        resetImageRLSize(holder.rlPicItem1, width);
        resetImageRLSize(holder.rlPicItem2, width);
        resetImageRLSize(holder.rlPicItem3, width);

        ArrayList<MediaInfo> list = mPictrueGroup.list;

        if (list.size() == 1) {
            holder.ivIcon0.setVisibility(View.VISIBLE);
            holder.ivIcon1.setVisibility(View.INVISIBLE);
            holder.ivIcon2.setVisibility(View.INVISIBLE);
            holder.ivIcon3.setVisibility(View.INVISIBLE);
            holder.ivOperation0.setVisibility(View.VISIBLE);
            holder.ivOperation1.setVisibility(View.INVISIBLE);
            holder.ivOperation2.setVisibility(View.INVISIBLE);
            holder.ivOperation3.setVisibility(View.INVISIBLE);

            loadImageDate(holder.ivIcon0, list.get(0));
            imgIsChoose(holder.ivOperation0, list.get(0).selected);
            String uri0 = getXLFileUri(list.get(0));

            mDMImageLoader.displayImage(uri0, holder.ivIcon0, mLoaderOptions, null);
        } else if (list.size() == 2) {
            holder.ivIcon0.setVisibility(View.VISIBLE);
            holder.ivIcon1.setVisibility(View.VISIBLE);
            holder.ivIcon2.setVisibility(View.INVISIBLE);
            holder.ivIcon3.setVisibility(View.INVISIBLE);
            holder.ivOperation0.setVisibility(View.VISIBLE);
            holder.ivOperation1.setVisibility(View.VISIBLE);
            holder.ivOperation2.setVisibility(View.INVISIBLE);
            holder.ivOperation3.setVisibility(View.INVISIBLE);

            loadImageDate(holder.ivIcon0, list.get(0));
            imgIsChoose(holder.ivOperation0, list.get(0).selected);
            String uri0 = getXLFileUri(list.get(0));

            mDMImageLoader.displayImage(uri0, holder.ivIcon0, mLoaderOptions, null);

            loadImageDate(holder.ivIcon1, list.get(1));
            imgIsChoose(holder.ivOperation1, list.get(1).selected);
            String uri1 = getXLFileUri(list.get(1));
            mDMImageLoader.displayImage(uri1, holder.ivIcon1, mLoaderOptions, null);

        } else if (list.size() == 3) {
            holder.ivIcon0.setVisibility(View.VISIBLE);
            holder.ivIcon1.setVisibility(View.VISIBLE);
            holder.ivIcon2.setVisibility(View.VISIBLE);
            holder.ivIcon3.setVisibility(View.GONE);
            holder.ivOperation0.setVisibility(View.VISIBLE);
            holder.ivOperation1.setVisibility(View.VISIBLE);
            holder.ivOperation2.setVisibility(View.VISIBLE);
            holder.ivOperation3.setVisibility(View.GONE);

            loadImageDate(holder.ivIcon0, list.get(0));
            imgIsChoose(holder.ivOperation0, list.get(0).selected);
            String uri0 = getXLFileUri(list.get(0));

            mDMImageLoader.displayImage(uri0, holder.ivIcon0, mLoaderOptions, null);


            loadImageDate(holder.ivIcon1, list.get(1));
            imgIsChoose(holder.ivOperation1, list.get(1).selected);
            String uri1 = getXLFileUri(list.get(1));
            mDMImageLoader.displayImage(uri1, holder.ivIcon1, mLoaderOptions, null);


            loadImageDate(holder.ivIcon2, list.get(2));
            imgIsChoose(holder.ivOperation2, list.get(2).selected);
            String uri2 = getXLFileUri(list.get(2));
            mDMImageLoader.displayImage(uri2, holder.ivIcon2, mLoaderOptions, null);
        } else if (list.size() == 4) {
            holder.ivIcon0.setVisibility(View.VISIBLE);
            holder.ivIcon1.setVisibility(View.VISIBLE);
            holder.ivIcon2.setVisibility(View.VISIBLE);
            holder.ivIcon3.setVisibility(View.VISIBLE);
            holder.ivOperation0.setVisibility(View.VISIBLE);
            holder.ivOperation1.setVisibility(View.VISIBLE);
            holder.ivOperation2.setVisibility(View.VISIBLE);
            holder.ivOperation3.setVisibility(View.VISIBLE);

            loadImageDate(holder.ivIcon0, list.get(0));
            imgIsChoose(holder.ivOperation0, list.get(0).selected);
            String uri0 = getXLFileUri(list.get(0));

            mDMImageLoader.displayImage(uri0, holder.ivIcon0, mLoaderOptions, null);


            loadImageDate(holder.ivIcon1, list.get(1));
            imgIsChoose(holder.ivOperation1, list.get(1).selected);
            String uri1 = getXLFileUri(list.get(1));
            mDMImageLoader.displayImage(uri1, holder.ivIcon1, mLoaderOptions, null);


            loadImageDate(holder.ivIcon2, list.get(2));
            imgIsChoose(holder.ivOperation2, list.get(2).selected);
            String uri2 = getXLFileUri(list.get(2));
            mDMImageLoader.displayImage(uri2, holder.ivIcon2, mLoaderOptions, null);


            loadImageDate(holder.ivIcon3, list.get(3));
            imgIsChoose(holder.ivOperation3, list.get(3).selected);
            String uri3 = getXLFileUri(list.get(3));

            mDMImageLoader.displayImage(uri3, holder.ivIcon3, mLoaderOptions, null);
        }
    }

    private void loadImageDate(PicImageView imageView, DMFile dmFile) {
        //后期需要做那个大图浏览的时候需要携带东西都可以在这里增加
        imageView.setDMFile(dmFile);
    }

    private void imgIsChoose(ImageView operView, boolean isSelected) {
        if (mMode == Mode.MODE_NORMAL) {
            operView.setVisibility(View.GONE);
            operView.setSelected(false);
        } else {
            //这里进行了一个显示。但是要根据operView 的一个选中状态进行显示图片，如果没有被选中的话是显示一张透明的图片
            operView.setVisibility(View.VISIBLE);
            operView.setSelected(isSelected);
        }
    }

    private void resetImageRLSize(RelativeLayout rl, int width) {
        LinearLayout.LayoutParams param = (LinearLayout.LayoutParams) rl.getLayoutParams();
        param.height = width;
        param.width = width;
        rl.setLayoutParams(param);
    }

    @Override
    public View getHeaderView(final int position, View convertView, ViewGroup parent) {
        final HeaderViewHolder holder;

        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.file_browse_date_pic_listview_header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text1);
            holder.iv_line_choose_group = (ImageView) convertView.findViewById(R.id.iv_line_choose_group);

            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }


        String date = list.get(position).Date;
        holder.text.setText(date);
        if (mMode == Mode.MODE_NORMAL) {
            holder.iv_line_choose_group.setVisibility(View.GONE);
            holder.iv_line_choose_group.setSelected(false);
        } else {
            /**
             * posPre 为同一个日期的第一个一个item 的 position
             * posNext 为同一个日期的最后一个item 的 position+1
             */


            long headerId = getHeaderId(position);
            int posPre = 0;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getDateParentId() == headerId) {
                    posPre = i;
                    break;
                }
            }
            int indexGroup = getSectionForPosition(posPre);
            int posNext;
            if (indexGroup >= mSectionIndices.length - 1) {
                posNext = list.size();
            } else {
                posNext = getPositionForSection(++indexGroup);
            }
            //检查从posPre到posNext之前的item是否全部被选中
            boolean allChoose = true;
            for (int i = posPre; i < posNext; i++) {
                ImageFolder p = list.get(i);
                for (int j = 0; j < p.list.size(); j++) {
                    if (!p.list.get(j).selected) {
                        allChoose = false;
                        break;
                    }
                }
                if (!allChoose) break;
            }

            /**--以上操作已精简到花费时间不到1ms-**/
            holder.iv_line_choose_group.setVisibility(View.VISIBLE);
            holder.iv_line_choose_group.setSelected(allChoose);


            //日期橫條的全选按钮
            holder.iv_line_choose_group.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    boolean isSelected = !holder.iv_line_choose_group.isSelected();

                    holder.iv_line_choose_group.setSelected(isSelected);
                    long headerId = getHeaderId(position);

                    for (int i = 0; i < list.size(); i++) {
                        ImageFolder p = list.get(i);
                        //找到该组
                        if (p.dateParentId == headerId) {
                            for (int j = 0; j < p.list.size(); j++) {
                                p.list.get(j).selected = isSelected;
                            }
                        }
                    }
                    notifyDataSetChanged();
                    //通知选中的个数已经发生改变
                    if (mOnSelectChangeListener != null) {
                        mOnSelectChangeListener.OnSelectChange();
                    }
                }
            });
        }


        return convertView;
    }

    /*
     *
     * 同一组的ID 必须是一样的
     */
    @Override
    public long getHeaderId(int position) {
        return list.get(position).getDateParentId();
    }

    /**
     * 获取分组数据的 第一个item位置
     */
    @Override
    public int getPositionForSection(int section) {
        if (section >= mSectionIndices.length) {
            section = mSectionIndices.length - 1;
        } else if (section < 0) {
            section = 0;
        }
        return mSectionIndices[section];
    }

    /**
     * 通过item 位置 获取分组数组的下标
     * <p>
     * 获取item 的position是属于哪一个组的
     */
    @Override
    public int getSectionForPosition(int position) {
        for (int i = 0; i < mSectionIndices.length; i++) {
            if (position < mSectionIndices[i]) {
                return i - 1;
            }
        }
        return mSectionIndices.length - 1;
    }

    @Override
    public Object[] getSections() {
        return mSectionLetters;
    }

    public void notifyDataSetChanged() {
        mSectionIndices = getSectionIndices();
        mSectionLetters = getSectionLetters();
        super.notifyDataSetChanged();
    }


    class HeaderViewHolder {
        TextView text;
        ImageView iv_line_choose_group;
    }

    private void initHolder(ViewHolder holder, View convertView) {
//		holder.ivDivide = (ImageView) convertView.findViewById(R.id.iv_line_divide);
        holder.ivIcon0 = (PicImageView) convertView.findViewById(R.id.piv_line_icon0);
        holder.ivIcon1 = (PicImageView) convertView.findViewById(R.id.piv_line_icon1);
        holder.ivIcon2 = (PicImageView) convertView.findViewById(R.id.piv_line_icon2);
        holder.ivIcon3 = (PicImageView) convertView.findViewById(R.id.piv_line_icon3);
        holder.ivOperation0 = (ImageView) convertView.findViewById(R.id.iv_line_operatinobtn0);
        holder.ivOperation1 = (ImageView) convertView.findViewById(R.id.iv_line_operatinobtn1);
        holder.ivOperation2 = (ImageView) convertView.findViewById(R.id.iv_line_operatinobtn2);
        holder.ivOperation3 = (ImageView) convertView.findViewById(R.id.iv_line_operatinobtn3);
        holder.rlPicItem0 = (RelativeLayout) convertView.findViewById(R.id.rl_line_picitem0);
        holder.rlPicItem1 = (RelativeLayout) convertView.findViewById(R.id.rl_line_picitem1);
        holder.rlPicItem2 = (RelativeLayout) convertView.findViewById(R.id.rl_line_picitem2);
        holder.rlPicItem3 = (RelativeLayout) convertView.findViewById(R.id.rl_line_picitem3);

        //这一步就是为了把引用给保存进去，在onclick可以拿出来使用
        holder.ivIcon0.setIcon(holder.ivOperation0);
        holder.ivIcon1.setIcon(holder.ivOperation1);
        holder.ivIcon2.setIcon(holder.ivOperation2);
        holder.ivIcon3.setIcon(holder.ivOperation3);


        holder.ivIcon0.setOnClickListener(new PicItemClickListener());
        holder.ivIcon1.setOnClickListener(new PicItemClickListener());
        holder.ivIcon2.setOnClickListener(new PicItemClickListener());
        holder.ivIcon3.setOnClickListener(new PicItemClickListener());

//		System.out.println("test123"+convertView.findViewById(R.id.iv_line_choose_group));
//		
//		holder.ivIcon0.setOnLongClickListener(new PicItemLongClickListener());
//		holder.ivIcon1.setOnLongClickListener(new PicItemLongClickListener());
//		holder.ivIcon2.setOnLongClickListener(new PicItemLongClickListener());
//		holder.ivIcon3.setOnLongClickListener(new PicItemLongClickListener());
    }


    /*
     * 这里就是选中的一个事件
     */
    private class PicItemClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub

            if (mMode == Mode.MODE_NORMAL) {
                // XLFile file = mDatas.get(position);
//						int positionInGroup = ((PicImageView) arg0).getUnitId() * FileExplorerView.UnitSize + ((PicImageView) arg0).getIdInLine();
//	                    int countInPreGroup = getPositionByGroupId(((PicImageView) arg0).getUnitGroupId());
//	                    int positionInAll = positionInGroup + countInPreGroup;
////						ArrayList<XLFile> files2ImageReader = (ArrayList<XLFile>) getAllDateInGroup(((PicImageView) arg0).getUnitGroupId());
//						FileOperationHelper.openPicture(mContext,groupDatas, positionInAll,((PicImageView) arg0).getXlFile(),ImagePagerActivity.IS_FROM_FileExplorerView);
            } else {

                ((PicImageView) v).getDMFile().selected = !((PicImageView) v).getDMFile().selected; // 反选
                //被选中的保存起来
//						updateSelectedList(((PicImageView) arg0).getXlFile());
                ImageView icon = ((PicImageView) v).getIcon();
                if (icon != null) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setSelected(((PicImageView) v).getDMFile().selected);
                }
//						notifyDataSetChanged();
                //通知选中的个数已经发生改变
                if (mOnSelectChangeListener != null) {
                    mOnSelectChangeListener.OnSelectChange();
                }
            }
        }

    }

    public String getXLFileUri(DMFile item) {
        String uri;
        if (item.mLocation == DMFile.LOCATION_UDISK) {
            uri = encodeUri("http://192.168.222.254" + item.mPath);
        } else {
            uri = "file://" + item.mPath;
        }
        return uri;
    }

    public static String encodeUri(String uri) {
        String newUri = "";

        if (uri.contains("http://")) {
            int uriIP_end = uri.indexOf("/", "http://".length()) + 1;

            newUri = uri.substring(0, uriIP_end);

            uri = uri.substring(uriIP_end);
        }

        StringTokenizer st = new StringTokenizer(uri, "/ ", true);


        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (tok.equals("/")) {
                newUri += "/";
            } else if (tok.equals(" "))
                newUri += "%20";
            else {
                newUri += URLEncoder.encode(tok);
            }
        }
        return newUri;
    }

    private class ViewHolder {
        public TextView tvDate;
        public RelativeLayout rlPicItem0;
        public PicImageView ivIcon0;
        public ImageView ivOperation0;
        public RelativeLayout rlPicItem1;
        public PicImageView ivIcon1;
        public ImageView ivOperation1;
        public RelativeLayout rlPicItem2;
        public PicImageView ivIcon2;
        public ImageView ivOperation2;
        public RelativeLayout rlPicItem3;
        public PicImageView ivIcon3;
        public ImageView ivOperation3;
    }


}
