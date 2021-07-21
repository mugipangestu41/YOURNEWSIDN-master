package com.kelompok3.YOURNEWS_IDN;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kelompok3.YOURNEWS_IDN.api.ApiClient;
import com.kelompok3.YOURNEWS_IDN.api.ApiInterface;
import com.kelompok3.YOURNEWS_IDN.models.Artikel;
import com.kelompok3.YOURNEWS_IDN.models.Berita;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements  SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemSelectedListener {
    public static final String API_KEY = "a71af80671bd403fb773343bc2c1e561";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Artikel> artikels = new ArrayList<>();
    private Adapter adapter;
    private TextView topHeadline;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ConstraintLayout errorLayout;
    private ImageView errorImage;
    private TextView errorTitle, errorMessage;
    private Button btnRetry;
    private ConstraintLayout filterLayout;
    private Button filterButton;
    private Spinner countrySelect;
    private Spinner categorySelect;
    private ArrayAdapter<CharSequence> adapterCountry;
    private ArrayAdapter<CharSequence> adapterCategory;

    //Bottom Nav
    private BottomNavigationView navigation;

    //Search values
    private SearchView searchBar;
    private String country;
    private String language;
    private boolean searched;
    private String category;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        topHeadline = findViewById(R.id.topheadelines);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(HomeActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
        searchBar = findViewById(R.id.search_bar);

        onLoadingSwipeRefresh("");

        errorLayout = findViewById(R.id.errorLayout);
        errorImage = findViewById(R.id.errorImage);
        errorTitle = findViewById(R.id.errorTitle);
        errorMessage = findViewById(R.id.errorMessage);
        btnRetry = findViewById(R.id.btnRetry);

        filterLayout = findViewById(R.id.filterLayout);
        filterButton = findViewById(R.id.filterButton);
        countrySelect = (Spinner) findViewById(R.id.countrySelect);
        categorySelect = (Spinner) findViewById(R.id.categorySelect);

        country = "id";
        language  = "en";
        category ="";

        //Bottom nav
        navigation = (BottomNavigationView) findViewById(R.id.bottomNav);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onNavigationItemSelected( MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id){
                    case R.id.action_filter:
                        //Open Filter menu
                        filterLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        topHeadline.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.GONE);
                        break;
                    case R.id.action_search:
                        //Open Search bar
                        filterLayout.setVisibility(View.GONE);
                        topHeadline.setVisibility(View.VISIBLE);
                        searched = false;
                        searchBar.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        searchBar.onActionViewExpanded();
                        topHeadline.setText("Hasil pencarian untuk: ");
                        break;
                    case R.id.action_headlines:
                        if(searched){
                            onRefresh();
                        }else{
                            filterLayout.setVisibility(View.GONE);
                            topHeadline.setVisibility(View.VISIBLE);
                            searchBar.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            topHeadline.setText("Berita Populer");
                        }
                        break;
                }
                return true;
            }
        });

        //Search Bar
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchBar.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchBar.setQueryHint("Cari berita terbaru...");
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() > 2){
                    onLoadingSwipeRefresh(query);
                    searched = true;
                    swipeRefreshLayout.setRefreshing(false);
                }
                else {
                    Toast.makeText(HomeActivity.this, "Masukkan lebih dari 2 huruf!", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapterCountry = ArrayAdapter.createFromResource(this, R.array.country_code, android.R.layout.simple_spinner_item);
        adapterCategory = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterCountry.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        countrySelect.setAdapter(adapterCountry);
        categorySelect.setAdapter(adapterCategory);
        countrySelect.setOnItemSelectedListener(this);
        categorySelect.setOnItemSelectedListener(this);


        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRefresh();
            }
        });

    }

    public void LoadJson(final String keyword){

        recyclerView.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(true);

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        Call<Berita> call;
        if(category.length() > 0){
            if (keyword.length() > 0 ){
                call = apiInterface.getNewsSearchFiltered(keyword, country/*language,"popularity"*/,category, API_KEY);

            } else {
                call = apiInterface.getNewsFiltered(country,category, API_KEY);
            }
        }else{
            if (keyword.length() > 0 ){
                call = apiInterface.getNewsSearch(keyword, country/*language,"popularity"*/, API_KEY);

            } else {
                call = apiInterface.getNews(country, API_KEY);
            }
        }
        call.enqueue(new Callback<Berita>() {
            @Override
            public void onResponse(Call<Berita> call, Response<Berita> response) {
                if (response.isSuccessful() && !response.body().getArtikel().isEmpty()){

                    if (!artikels.isEmpty()){
                        artikels.clear();
                    }

                    artikels = response.body().getArtikel();
                    adapter = new Adapter(artikels, HomeActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();

                    recyclerView.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    recyclerView.setVisibility(View.INVISIBLE);
                    swipeRefreshLayout.setRefreshing(false);

                    String errorCode;
                    switch (response.code()) {
                        case 404:
                            errorCode = "404 not found";
                            break;
                        case 500:
                            errorCode = "500 server broken";
                            break;
                        default:
                            errorCode = "unknown error";
                            break;
                    }

                    showErrorMessage(
                            R.drawable.no_result,
                            "Tidak ada hasil",
                            "Mohon coba lagi!\n"+
                                    errorCode);

                }
            }

            @Override
            public void onFailure(Call<Berita> call, Throwable t) {
                recyclerView.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                showErrorMessage(
                        R.drawable.oops,
                        "Oops..",
                        "Koneksi bermasalah, mohon coba lagi\n"+
                                t.toString());
            }
        });
    }

    private void initListener(){

        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ImageView imageView = view.findViewById(R.id.img);
                Intent intent = new Intent(HomeActivity.this, NewsDetailActivity.class);

                Artikel artikel = artikels.get(position);
                intent.putExtra("url", artikel.getUrl());
                intent.putExtra("title", artikel.getTitle());
                intent.putExtra("img",  artikel.getUrlToImage());
                intent.putExtra("date",  artikel.getPublishedAt());
                intent.putExtra("source",  artikel.getSumber().getName());
                intent.putExtra("author",  artikel.getAuthor());
                intent.putExtra("desc",  artikel.getDescription());

                Pair<View, String> pair = Pair.create((View)imageView, ViewCompat.getTransitionName(imageView));
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        HomeActivity.this,
                        pair
                );


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    startActivity(intent, optionsCompat.toBundle());
                }else {
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        navigation.getMenu().findItem(R.id.action_headlines).setChecked(true);
        filterLayout.setVisibility(View.GONE);
        searchBar.setVisibility(View.GONE);
        LoadJson("");
        topHeadline.setText("Berita Populer");
        topHeadline.setVisibility(View.VISIBLE);

    }

    private void onLoadingSwipeRefresh(final String keyword){
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        LoadJson(keyword);
                    }
                }
        );
    }

    private void showErrorMessage(int imageView, String title, String message){

        recyclerView.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);

        errorImage.setImageResource(imageView);
        errorTitle.setText(title);
        errorMessage.setText(message);

        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoadingSwipeRefresh("");
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String value = adapterView.getItemAtPosition(i).toString();
        switch (adapterView.getId())
        {
            case R.id.countrySelect:
                //Your code that deal with country spinner
                country = value;
                break;
            case R.id.categorySelect:
                //Your code that deal with category spinner
                category = value;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //Do Something?
    }
}
