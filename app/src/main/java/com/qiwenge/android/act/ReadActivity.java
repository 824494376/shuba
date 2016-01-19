package com.qiwenge.android.act;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.liuguangqiang.framework.utils.DisplayUtils;
import com.qiwenge.android.R;
import com.qiwenge.android.adapters.ReadMenuAdapter;
import com.qiwenge.android.adapters.ReadThemeAdapter;
import com.qiwenge.android.async.AsyncUtils;
import com.qiwenge.android.base.BaseActivity;
import com.qiwenge.android.constant.Constants;
import com.qiwenge.android.entity.Book;
import com.qiwenge.android.entity.Chapter;
import com.qiwenge.android.entity.Mirror;
import com.qiwenge.android.entity.Progresses;
import com.qiwenge.android.entity.ReadMenu;
import com.qiwenge.android.entity.ReadTheme;
import com.qiwenge.android.fragments.ReadFragment;
import com.qiwenge.android.listeners.ReadPageClickListener;
import com.qiwenge.android.ui.OfflineMenu;
import com.qiwenge.android.utils.ReaderUtils;
import com.qiwenge.android.utils.ScreenBrightnessUtils;
import com.qiwenge.android.utils.ThemeUtils;
import com.qiwenge.android.utils.book.BookManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class ReadActivity extends BaseActivity {

    private static final String TAG = "ReadActivity";

    /**
     * 是否初始化完毕。
     */
    private boolean inited = false;

    private static final int MESSAGE_SET_BRIGHTNESS = 0x1;

    /**
     * 最大屏幕亮度
     */
    private final static int MAX_BRIGHTNESS = 255;

    /**
     * 调节字体大小的SeekBar的最大值
     */
    private final static int MAX_VALUE_FONTSZIE_SEEKBAR = 100;

    /**
     * SeekBar的progrees，缩小5倍为字体偏移量
     */
    private final static int OFFSET_ZOOM_OUT = 5;

    /**
     * 最大字体偏移量
     */
    private final static int MAX_FONTSIZE_OFFSET = MAX_VALUE_FONTSZIE_SEEKBAR / OFFSET_ZOOM_OUT;

    /**
     * 调节屏幕亮度的增量，仅点击亮度按钮时使用
     */
    private final static int INCREMENT_BRIGHTNESS = 5;

    /**
     * 亮度SeekBar的值
     */
    private int mProgress = 0;

    /**
     * 字体大小增量。
     */
    private int mFontSizeOffest = 0;

    /**
     * 原始字体大小。字体大小的调节，在此基础上偏移
     */
    private int mFontSizeOrigin = Constants.MIN_TEXT_SIZE;

    /**
     * 上次修改的字体大小。
     */
    private int lastTextSize = 0;


    /**
     * 章节Id
     */
    public final static String Extra_ChapterId = "chapterId";

    public final static String Extra_Book = "book";

    private ReadFragment fragment;

    @InjectView(R.id.layout_read_container)
    RelativeLayout layoutContainer;

    @InjectView(R.id.layout_top_menu)
    RelativeLayout topMenu;

    @InjectView(R.id.layout_bottom_menu)
    RelativeLayout layoutBottomMenu;

    @InjectView(R.id.layout_menu_aa_set)
    RelativeLayout layoutMenuAaSet;

    @InjectView(R.id.layout_menu_offline)
    LinearLayout layoutMenuOffline;

    @InjectView(R.id.tv_book_title)
    TextView tvBookTitle;

    @InjectView(R.id.gv_menu)
    GridView gvBottomMenu;

    @InjectView(R.id.offline_menu)
    OfflineMenu offlineMenu;

    private GridView gvTheme;

    private SeekBar seekBarBrightness;
    private SeekBar seekFontSize;

    private boolean topIsShow = false;
    private boolean bottomIsShow = false;
    private boolean isShowAsSet = false;
    private boolean isShowOffline = false;

    private List<ReadMenu> menuData = new ArrayList<>();
    private List<ReadTheme> themeData = new ArrayList<>();

    private ReadMenuAdapter menuAdapter;
    private ReadThemeAdapter themeAdapter;

    /**
     * 当前选择的主题
     */
    private int currentTheme = 0;

    private Handler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        ButterKnife.inject(this);
        initData();
        initViews();
        initReadFragment();
        getIntentData();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getExtras() != null) {
            handleExtras(intent.getExtras());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !inited) {
            initListeners();
            initTheme();
            inited = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ScreenBrightnessUtils.saveScreenBright(getApplicationContext(), mProgress);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            showOrHideMenu();
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (topIsShow) showOrHideMenu();
            else finish();
        }
        return true;
    }

    private Book book;

    /**
     * 获取意图传递的数据，并获取章节详情。
     */
    private void getIntentData() {
        Bundle extra = getIntent().getExtras();
        handleExtras(extra);
    }

    private void handleExtras(Bundle extra) {
        fragment.clearReader();

        if (extra.containsKey(Extra_Book)) {
            book = extra.getParcelable(Extra_Book);
            fragment.setBook(book);
            AsyncUtils.postViewTotal(book.getId());
            tvBookTitle.setText(book.title);
            offlineMenu.setBook(book);
        }

        if (extra.containsKey(Extra_ChapterId)) {
            String chapterId = extra.getString(Extra_ChapterId);
            getChapter(chapterId);
        }
    }

    @Subscribe
    public void onEvent(Chapter chapter) {
        if (offlineMenu != null) {
            offlineMenu.setChapter(chapter);
        }
    }

    private void getChapter(String chapterId) {
        if (book != null) {
            Book record = BookManager.getInstance().getById(book.getId());

            if (record != null && record.progresses != null && chapterId.equals(record.progresses.chapter_id)) {
                int length = record.progresses.chars;
                fragment.getChapter(chapterId, length);
            } else {
                fragment.getChapter(chapterId);
            }
        }
    }

    /**
     * 初始化数据，如menu,theme
     */
    private void initData() {
        String[] titles = getResources().getStringArray(R.array.read_menu_titles);
        TypedArray icons = getResources().obtainTypedArray(R.array.read_menu_icons);

        ReadMenu menu;
        for (int i = 0; i < titles.length; i++) {
            menu = new ReadMenu();
            menu.title = titles[i];
            menu.icon = icons.getResourceId(i, R.mipmap.icon_menu_chapters);
            menuData.add(menu);
        }

        int[] themes = {ThemeUtils.NORMAL, ThemeUtils.YELLOW, ThemeUtils.GREEN, ThemeUtils.LEATHER};

        ReadTheme theme;
        for (int i = 0; i < themes.length; i++) {
            theme = new ReadTheme();
            theme.theme = themes[i];
            themeData.add(theme);
        }

        // 屏幕亮度。
        mProgress = ScreenBrightnessUtils.getBrightness(getApplicationContext());
        ScreenBrightnessUtils.setBrightness(this, mProgress);
    }

    private void initViews() {
        initBottomMenu();
        initBrightnessSeekBar();
        initFontSizeSeekBar();
    }

    private void initBottomMenu() {
        menuAdapter = new ReadMenuAdapter(getApplicationContext(), menuData);
        gvBottomMenu.setNumColumns(menuData.size());
        gvBottomMenu.setAdapter(menuAdapter);

        themeAdapter = new ReadThemeAdapter(getApplicationContext(), themeData);
        gvTheme = (GridView) this.findViewById(R.id.gv_theme);
        gvTheme.setAdapter(themeAdapter);
    }

    private void initBrightnessSeekBar() {
        seekBarBrightness = (SeekBar) this.findViewById(R.id.seekbar_screen_brightness);
        seekBarBrightness.setMax(MAX_BRIGHTNESS);
        seekBarBrightness.setProgress(mProgress);
        seekBarBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setBrightness(progress);
                }
            }
        });
    }

    private void initFontSizeSeekBar() {
        // 字体大小。
        seekFontSize = (SeekBar) this.findViewById(R.id.seekBar_font_size);
        int textSize = ReaderUtils.getTextSize(getApplicationContext());

        //字体大小改变的偏移量x5，就是SeekBar的进度
        mFontSizeOffest = textSize - mFontSizeOrigin;
        int progress = mFontSizeOffest * OFFSET_ZOOM_OUT;
        seekFontSize.setMax(MAX_VALUE_FONTSZIE_SEEKBAR);
        seekFontSize.setProgress(progress);
        seekFontSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setReadTextSize();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mFontSizeOffest = progress / OFFSET_ZOOM_OUT;
                }
            }
        });
    }

    private void initListeners() {
        gvBottomMenu.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                switch (arg2) {
                    case 0:// 夜间模式
                        if (ThemeUtils.isNightModel) {
                            showNighitModel(false);
                        } else {
                            showNighitModel(true);
                        }
                        break;
                    case 1:// Aa
                        if (isShowAsSet) {
                            hideAaSet();
                        } else {
                            showAaSet();
                        }
                        break;
                    case 2:// 目录
                        Bundle extra = new Bundle();
                        extra.putParcelable(ChapterActivity.EXTRA_BOOK, book);
                        startActivity(ChapterActivity.class, extra);
                        break;
                    case 3:
                        if (isShowOffline) {
                            hideOffline();
                        } else {
                            showOffline();
                        }
                        break;
                    default:
                        break;
                }

            }
        });

        gvTheme.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectTheme(position, true);
            }
        });
    }


    /**
     * 选择主题。
     *
     * @param i
     */
    private void selectTheme(int i, boolean cacheable) {
        if (currentTheme >= 0) themeData.get(currentTheme).selected = false;
        themeData.get(i).selected = true;
        currentTheme = i;
        themeAdapter.notifyDataSetChanged();

        ThemeUtils.setReaderTheme(themeData.get(i).theme, layoutContainer);
        ThemeUtils.setNightModle(getApplicationContext(), false);
        cancelNightModel();
        if (cacheable) ThemeUtils.setTheme(getApplicationContext(), themeData.get(i).theme);
    }

    private void clearThemeSelected() {
        themeData.get(currentTheme).selected = false;
        currentTheme = -1;
        themeAdapter.notifyDataSetChanged();
    }

    /**
     * 取消夜间模式
     */
    private void cancelNightModel() {
        menuData.get(0).icon = R.mipmap.icon_menu_mode_night;
        menuData.get(0).title = getString(R.string.reader_night_model);
        menuAdapter.notifyDataSetChanged();
        fragment.refreshTextColor();
    }

    /**
     * 设置，是否为夜间模式。
     *
     * @param isNight
     */
    private void showNighitModel(boolean isNight) {
        ThemeUtils.setNightModle(getApplicationContext(), isNight);
        ThemeUtils.setThemeBg(layoutContainer);
        if (isNight) {
            menuData.get(0).icon = R.mipmap.icon_menu_mode_normal;
            menuData.get(0).title = getString(R.string.reader_normal_model);
            clearThemeSelected();
        } else {
            selectedLastTheme();
            cancelNightModel();
        }
        menuAdapter.notifyDataSetChanged();
        fragment.refreshTextColor();
    }

    /**
     * 初始化主题
     */
    private void initTheme() {
        if (ThemeUtils.isNightModel) {
            showNighitModel(true);
        } else {
            selectedLastTheme();
        }
    }

    /**
     * 选择原来的主题
     */
    private void selectedLastTheme() {
        int theme = ThemeUtils.getCurrentTheme();
        for (int i = 0; i < themeData.size(); i++) {
            if (themeData.get(i).theme == theme) {
                selectTheme(i, false);
                break;
            }
        }
    }


    private SpringSystem springSystem = SpringSystem.create();
    private Spring springTopMenu;
    private Spring springBottomMenu;
    private Spring springAaSet;
    private Spring springOffline;
    private int topMenuTranY;

    private void animTopMenu(boolean isShow) {
        if (springTopMenu == null) {
            springTopMenu = springSystem.createSpring();
            springTopMenu.addListener(new SimpleSpringListener() {

                @Override
                public void onSpringUpdate(Spring spring) {
                    float value = (float) spring.getCurrentValue();
                    topMenu.setTranslationY(value);
                }
            });

            topMenuTranY = topMenu.getHeight() - DisplayUtils.dip2px(this, 20);
        }

        springTopMenu.setEndValue(isShow ? topMenuTranY : 0);
    }

    private void animBottomMenu(final boolean isShow) {
        if (springBottomMenu == null) {
            springBottomMenu = springSystem.createSpring();
            springBottomMenu.addListener(new SimpleSpringListener() {

                @Override
                public void onSpringUpdate(Spring spring) {
                    float value = (float) spring.getCurrentValue();
                    layoutBottomMenu.setTranslationY(value);
                }

                @Override
                public void onSpringEndStateChange(Spring spring) {
                    super.onSpringEndStateChange(spring);
                }
            });
        }

        springBottomMenu.setEndValue(isShow ? -gvBottomMenu.getHeight() : 0);
    }

    private void animAaSet(final boolean isShow) {
        if (springAaSet == null) {
            springAaSet = springSystem.createSpring();
            springAaSet.addListener(new SimpleSpringListener() {

                @Override
                public void onSpringUpdate(Spring spring) {
                    float value = (float) spring.getCurrentValue();
                    layoutMenuAaSet.setTranslationY(value);
                }

                @Override
                public void onSpringEndStateChange(Spring spring) {
                    super.onSpringEndStateChange(spring);
                }
            });
        }

        springAaSet.setEndValue(isShow ? -layoutMenuAaSet.getHeight() : 0);
    }

    private void animOffline(final boolean isShow) {
        if (springOffline == null) {
            springOffline = springSystem.createSpring();
            springOffline.addListener(new SimpleSpringListener() {

                @Override
                public void onSpringUpdate(Spring spring) {
                    float value = (float) spring.getCurrentValue();
                    layoutMenuOffline.setTranslationY(value);
                }

                @Override
                public void onSpringEndStateChange(Spring spring) {
                    super.onSpringEndStateChange(spring);
                }
            });
        }

        springOffline.setEndValue(isShow ? -layoutMenuOffline.getHeight() : 0);
    }

    private void showTop() {
        topIsShow = true;
        animTopMenu(true);
    }

    private void hideTop() {
        topIsShow = false;
        animTopMenu(false);
    }

    private void showBottomMenu() {
        animBottomMenu(true);
        bottomIsShow = true;
    }

    private void hideBottomMenu() {
        animBottomMenu(false);
        hideAaSet();
        hideOffline();
        bottomIsShow = false;
    }

    private void showAaSet() {
        animAaSet(true);
        hideOffline();
        isShowAsSet = true;
    }

    private void hideAaSet() {
        animAaSet(false);
        isShowAsSet = false;
    }

    private void showOffline() {
        animOffline(true);
        hideAaSet();
        isShowOffline = true;
    }

    private void hideOffline() {
        animOffline(false);
        isShowOffline = false;
    }

    /**
     * 显示或者隐藏菜单。
     */
    private void showOrHideMenu() {
        handleTop();
        handleBottom();
    }

    private void handleTop() {
        if (topIsShow)
            hideTop();
        else
            showTop();
    }

    private void handleBottom() {
        if (bottomIsShow) {
            hideBottomMenu();
        } else {
            showBottomMenu();
        }

    }

    /**
     * 初始化Fragment。
     */
    private void initReadFragment() {
        fragment = new ReadFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.layout_content, fragment).commit();
        fragment.setOnReadPageClickListener(new ReadPageClickListener() {

            @Override
            public void onClick() {
                showOrHideMenu();
            }
        });
    }

    private void setReadTextSize() {
        int textSize = mFontSizeOrigin + mFontSizeOffest;
        if (textSize != lastTextSize) {
            //如果没有修改字体大小，如，来回滑动，不修改阅读器的字体大小
            lastTextSize = textSize;
            fragment.setTextSize(textSize);
            ReaderUtils.saveTextSize(getApplicationContext(), textSize);
        }
    }

    private void setBrightness(int progress) {
        mProgress = progress;
        Message msg = mHandler.obtainMessage();
        msg.what = MESSAGE_SET_BRIGHTNESS;
        msg.arg1 = progress;
        msg.sendToTarget();
    }

    @OnClick(R.id.layout_top_menu)
    public void onFinish() {
        finish();
    }

    @OnClick(R.id.iv_brightness_minus)
    public void minusBrightness() {
        mProgress = mProgress - INCREMENT_BRIGHTNESS;
        if (mProgress < 0) mProgress = 0;
        seekBarBrightness.setProgress(mProgress);
        setBrightness(mProgress);
    }

    @OnClick(R.id.iv_brightness_plus)
    public void plusBirghtness() {
        mProgress = mProgress + INCREMENT_BRIGHTNESS;
        if (mProgress > MAX_BRIGHTNESS) mProgress = MAX_BRIGHTNESS;
        seekBarBrightness.setProgress(mProgress);
        setBrightness(mProgress);
    }

    @OnClick(R.id.tv_fontsize_minus)
    public void minusFontSize() {
        mFontSizeOffest = mFontSizeOffest - 1;
        if (mFontSizeOffest < 0) mFontSizeOffest = 0;
        seekFontSize.setProgress(mFontSizeOffest * OFFSET_ZOOM_OUT);
        setReadTextSize();
    }

    @OnClick(R.id.tv_fontsize_plus)
    public void plusFontSize() {
        mFontSizeOffest = mFontSizeOffest + 1;
        if (mFontSizeOffest > MAX_FONTSIZE_OFFSET) mFontSizeOffest = MAX_FONTSIZE_OFFSET;
        seekFontSize.setProgress(mFontSizeOffest * OFFSET_ZOOM_OUT);
        setReadTextSize();
    }


    /**
     * 静态的内部类，继承了Handler，防内存泄露
     */
    private static class MyHandler extends Handler {

        private WeakReference<Activity> mActivity;

        public MyHandler(Activity act) {
            mActivity = new WeakReference<Activity>(act);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_SET_BRIGHTNESS) {
                ScreenBrightnessUtils.setBrightness(mActivity.get(), msg.arg1);
            }
        }
    }

}
