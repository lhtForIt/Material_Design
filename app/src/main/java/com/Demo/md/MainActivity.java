package com.Demo.md;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Gson.FuLiImageGson;
import LhtUtils.OkhttpTool;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout srl;
    private FuLiImageGson fuLiImageGson;
    private int mPageNum = 1;
    private boolean loadMore;
    private StaggeredGridLayoutManager sg;
    private DisplayMetrics metrics = new DisplayMetrics();
    private Myadapter adapter = new Myadapter();
    private String[] describle = {"繁花落尽，往事随风","男生碰到喜欢的人有哪些表现？","10分钟读完刘慈欣代表作科幻作品《三体》","这是我见过最干净利落的分手，卧槽，这个广告我给满分","看完这视频，我再也不装逼了","人生最遗憾的莫过于，轻易地放弃了不该放弃的,固执地坚持了不该坚持的。人生，要留下多少遗憾....","旅行的意义是什么","不要给我讲道理，我就是道理...也就是说出了万千女生的心声...哈哈哈~真理都在女人的嘴里,...","搞笑宋小宝新年小品：烤串","papi酱的春节街采访--是时候回家过年了"};
    private List<FuLiImageGson.ResultsBean> list = new ArrayList<>();
    private final int GET_IAMGE = 0X123;
    private final int LOAD_MORE = 0X111;
    private Handler hd = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == GET_IAMGE) {

                if (null != fuLiImageGson) {
                    list.clear();
                    list.addAll(fuLiImageGson.getResults());

                }
                adapter.notifyDataSetChanged();
                srl.setRefreshing(false);


            } else if (msg.what == LOAD_MORE) {

                Log.d("lht", "====================进来了1");
                if (null != fuLiImageGson) {
                    Log.d("lht", "====================进来了2");
                    list.addAll(fuLiImageGson.getResults());
                }
                adapter.notifyDataSetChanged();
                srl.setRefreshing(false);
                loadMore = false;


            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        setContentView(R.layout.activity_main);


        toolbar = (Toolbar) findViewById(R.id.tb);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.dl);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.xf_bt);
        recyclerView = (RecyclerView) findViewById(R.id.recyc);
        srl = (SwipeRefreshLayout) findViewById(R.id.swi);

        srl.setColorSchemeColors(Color.parseColor("#00cffb"), Color.parseColor("#1792f1"), Color.parseColor("#17b6f1"));

        srl.setRefreshing(true);
        doRefresh();

        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        ActionBar ab = getSupportActionBar();
        if (null != ab) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.mipmap.menu);
        }

        sg = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        sg.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
//        sg.offsetChildrenHorizontal(16);
        recyclerView.setLayoutManager(sg);
        recyclerView.addItemDecoration(new MyItemDecoration(12));
        recyclerView.setAdapter(adapter);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "不要点我了知道么？", Snackbar.LENGTH_INDEFINITE).setAction("I Know", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "数据恢复", Toast.LENGTH_SHORT).show();
                    }
                }).show();
            }
        });
        navigationView.setCheckedItem(R.id.call);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                drawerLayout.closeDrawers();

                return true;
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                super.onScrollStateChanged(recyclerView, newState);
                if(!loadMore && !recyclerView.canScrollVertically(1)){
                    loadMore = true;
                    doLoadMore(++mPageNum);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {


                super.onScrolled(recyclerView, dx, dy);
                //向下滚动
//                if (dy > 0) {
//                    int visibleItemCount = sg.getChildCount();
//                    int totalItemCount = sg.getItemCount();
//                    int[] pastVisiblesItems = sg.findFirstVisibleItemPositions(new int[sg.getColumnCountForAccessibility(null, null)]);
//
//                    for (int i = 0; i < pastVisiblesItems.length; i++) {
//                        if (!loadMore && (visibleItemCount + pastVisiblesItems[i]) >= totalItemCount) {
//                            loadMore = true;
//                            doLoadMore(++mPageNum);
//                        }
//                    }
//
//                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.backup:
                Toast.makeText(MainActivity.this, "backup被点击了", Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete:
                Toast.makeText(MainActivity.this, "delete被点击了", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:
                Toast.makeText(MainActivity.this, "setting被点击了", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;

            default:
                break;
        }


        return true;
    }

    private void doRefresh() {
        mPageNum = 1;
        srl.setRefreshing(true);
        OkhttpTool.getOkhttpTool().get("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/10/1", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Snackbar.make(floatingActionButton, "数据请求错误，请刷新重试！", Snackbar.LENGTH_INDEFINITE).setAction("知道了", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String s = response.body().string();
                Log.d("lht", "==========================response:" + s);
                fuLiImageGson = new Gson().fromJson(s, FuLiImageGson.class);
                hd.sendEmptyMessage(GET_IAMGE);


            }
        });
    }

    private void doLoadMore(int pageNum) {
        mPageNum++;
        OkhttpTool.getOkhttpTool().get("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/10/" + mPageNum, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Snackbar.make(floatingActionButton, "数据请求错误，请刷新重试！", Snackbar.LENGTH_INDEFINITE).setAction("知道了", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String s = response.body().string();
                Log.d("lht", "==========================response:" + s);
                fuLiImageGson = new Gson().fromJson(s, FuLiImageGson.class);
                hd.sendEmptyMessage(LOAD_MORE);


            }
        });
    }

    class Myadapter extends RecyclerView.Adapter<Myadapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            MyViewHolder myholer = new MyViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.card_view, null));

            return myholer;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {

            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(new RecyclerView.LayoutParams(metrics.widthPixels / 2 - 40, ViewGroup.LayoutParams.WRAP_CONTENT - 40));
            holder.itemView.setLayoutParams(lp);


//            holder.itemView.setPadding(8, 0, 8, 0);
//            holder.itemView.offsetLeftAndRight(16);
            Glide.with(MainActivity.this).load(list.get(position).getUrl()).placeholder(R.mipmap.touxiang).into(holder.iv);
            holder.tv.setText(describle[new Random().nextInt(10)]);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(MainActivity.this, "第" + position + "妹子被点击了", Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        public int getItemCount() {
            if (null == list) {
                return 0;
            }
            return list.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            private ImageView iv;
            private TextView tv;

            public MyViewHolder(View itemView) {
                super(itemView);
                iv = (ImageView) itemView.findViewById(R.id.image);
                tv = (TextView) itemView.findViewById(R.id.text);
            }
        }


    }

    private class MyItemDecoration extends RecyclerView.ItemDecoration{

        private int space;

        public MyItemDecoration(int space) {
            this.space=space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left=space;
            outRect.right=space;
            outRect.bottom=space;
            //注释这两行是为了上下间距相同
//        if(parent.getChildAdapterPosition(view)==0){
            outRect.top=space;
//        }
        }


        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
        }

    }

}
