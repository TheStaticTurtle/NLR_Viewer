package tfgapps.video.nlrviewer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class selector extends AppCompatActivity {

    String CURRENTREVISION = "nlr_viewer.v1.2.8.apk";
    boolean DEBUGMODE = false;
    MLP_Content NLRVIDEOCONTENT;
    NLR_Content NLRCONTENT;
    selector instance =this;
    Boolean done = false;
    int preventForkBombFromUpdate = 0;
    public ActionBarDrawerToggle mDrawerToggle;

    @Override protected void onCreate(Bundle savedInstanceState) {
        setTheme(translateTheme_toID(getConfig_getTheme()));
        updateLocale(translateLang_toCodeName(getConfig_getLang()));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            firstSetUp(true);
            if (!done) {
                updateFromTheNlr();
                done = true;
            }
        } catch (Exception e) {
            Log.e("Oncreate","update",e);
        }

        CheckUpdate();
        //int margin = calcDrawerLayoutMargin();
        //LinearLayout layout = findViewById(R.id.drawerHeaderLayout);
        //setMargins(layout,0,margin,0,0);

    }
    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_selector);
        firstSetUp(false);
        updateViews(NLRVIDEOCONTENT,NLRCONTENT);
    }
    @Override public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            drawer.openDrawer(GravityCompat.START);
            //super.onBackPressed();
        }
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {  return true;   }
        return super.onOptionsItemSelected(item);
    }
    public void firstSetUp(Boolean onCreate)    {
        ViewFlipper vf = (ViewFlipper)findViewById(R.id.layoutFlipper);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Log.d("DRAWER", "onDrawerOpened: " + getTitle());
                invalidateOptionsMenu();
            }
            @Override public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                Log.d("DRAWER", "onDrawerClosed: " + getTitle());
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle = toggle;

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(onCreate) { vf.setDisplayedChild(10); }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_episodes) {
                    vf.setDisplayedChild(0);
                } else if (id == R.id.nav_films) {
                    vf.setDisplayedChild(1);
                } else if (id == R.id.nav_bugreport) {
                    vf.setDisplayedChild(2);
                    Button activity_senmail = findViewById(R.id.sendmail_send);
                    activity_senmail.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { sendBugReport(); }});
                } else if (id == R.id.nav_social) {
                    vf.setDisplayedChild(3);
                    bindSocialButtons();
                } else if (id == R.id.nav_allies) {
                    vf.setDisplayedChild(4);
                } else if (id == R.id.nav_news) {
                    vf.setDisplayedChild(5);
                } else if (id == R.id.nav_comicen) {
                    vf.setDisplayedChild(6);
                } else if (id == R.id.nav_comicfr) {
                    vf.setDisplayedChild(7);
                } else if (id == R.id.nav_aboutdev) {
                    vf.setDisplayedChild(8);
                    bindSocialButtonsForDev();
                } else if (id == R.id.nav_reload) {
                    updateFromTheNlr();
                } else if (id == R.id.nav_setting) {
                    vf.setDisplayedChild(11);
                    settings_bindViews();
                } else if (id == R.id.nav_bonus) {
                    vf.setDisplayedChild(12);
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
    public int calcDrawerLayoutMargin() {
        // (actionBarHeight * 2) - (activity_vertical_margin *2)

        TypedValue tv = new TypedValue();
        int actionBarHeight = 56;
        float activity_vertical_margin = 16;

        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))   {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        activity_vertical_margin = getResources().getDimension(R.dimen.activity_vertical_margin);

        return (actionBarHeight * 2) - ((int)(activity_vertical_margin *2));
    }
    public void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }

    public void updateFromTheNlr() {
        ViewFlipper vf = (ViewFlipper)findViewById(R.id.layoutFlipper);
        vf.setDisplayedChild(10);
        updateEveryThingFromTheNlrUsingAPI();
    }
    public void updateEveryThingFromTheNlrUsingAPI() {
        final String apiUrl_Episodes = "https://newlunarrepublic.fr/api/episodes";
        final String apiUrl_Films = "https://newlunarrepublic.fr/api/films";
        final String apiUrl_Comics = "https://newlunarrepublic.fr/api/comics";
        final String apiUrl_News = "https://newlunarrepublic.fr/api/news";
        final String apiUrl_Bonus = "https://newlunarrepublic.fr/api/bonus";
        new Thread(new Runnable() {
            public void run() {
                try {
                    String episodeJson = getJSON(apiUrl_Episodes,5000);
                    String filmJson = getJSON(apiUrl_Films,5000);
                    String comicsJson = getJSON(apiUrl_Comics,5000);
                    String newsJson = getJSON(apiUrl_News,5000);
                    String bonusJson = getJSON(apiUrl_Bonus,5000);

                    // Prase episodes
                    JSONObject jsonObjEpisodes = new JSONObject(episodeJson);
                    Iterator<?> keys = jsonObjEpisodes.keys();
                    ArrayList<MLP_Season> seasons = new ArrayList<>();
                    int globalEpisodeCounter = 0;
                    int globalSeasonCounter = 0;
                    while( keys.hasNext() ) {
                        String key = (String)keys.next();
                        if ( jsonObjEpisodes.get(key) instanceof JSONArray ) {
                            globalSeasonCounter = globalSeasonCounter +1;
                            MLP_Season currentSeason = new MLP_Season();
                            currentSeason.id = Integer.parseInt(key)-1;
                            ArrayList<MLP_Episode> eps = new ArrayList<>();
                            JSONArray epListObjArray = ((JSONArray) jsonObjEpisodes.get(key));
                            for(int i=0; i < epListObjArray.length(); i++) {
                                globalEpisodeCounter = globalEpisodeCounter +1;
                                JSONObject epObj = ((JSONObject) epListObjArray.get(i));
                                MLP_Episode currentEp = new MLP_Episode();
                                currentEp.title = epObj.getString("title");
                                currentEp.thumbUrl  = epObj.getString("thumbnail");
                                currentEp.id_local = toint(epObj.getString("episode"));
                                currentEp.id_global = toint(epObj.getString("slug"));
                                currentEp.in_season_num = toint(epObj.getString("season"));
                                currentEp.url_sub_fr = epObj.getJSONObject("subtitles").getJSONObject("fr").getString("srt");
                                currentEp.url_sub_en = epObj.getJSONObject("subtitles").getJSONObject("en").getString("srt");
                                currentEp.url_vf_240p = epObj.getJSONObject("sources").getJSONObject("vf").getString("240");
                                currentEp.url_vf_360p = epObj.getJSONObject("sources").getJSONObject("vf").getString("360");
                                currentEp.url_vf_480p = epObj.getJSONObject("sources").getJSONObject("vf").getString("480");
                                currentEp.url_vf_720p = epObj.getJSONObject("sources").getJSONObject("vf").getString("720");
                                currentEp.url_vf_1080p = epObj.getJSONObject("sources").getJSONObject("vf").getString("1080");
                                currentEp.url_vo_240p = epObj.getJSONObject("sources").getJSONObject("vo").getString("240");
                                currentEp.url_vo_360p = epObj.getJSONObject("sources").getJSONObject("vo").getString("360");
                                currentEp.url_vo_480p = epObj.getJSONObject("sources").getJSONObject("vo").getString("480");
                                currentEp.url_vo_720p = epObj.getJSONObject("sources").getJSONObject("vo").getString("720");
                                currentEp.url_vo_1080p = epObj.getJSONObject("sources").getJSONObject("vo").getString("1080");
                                if(epObj.getString("status").contains("Bient")) { currentEp.released = false; } else { currentEp.released = true; }
                                if(epObj.getString("status").contains("Embed"))  { currentEp.embedded= true; } else { currentEp.embedded = false; }
                                currentEp.releaseDate = epObj.getString("stream_date");
                                eps.add(currentEp);
                            }
                            currentSeason.episodes = eps;
                            seasons.add(currentSeason);
                        }
                    }

                    // Prase films
                    JSONArray filmListObj = new JSONArray(filmJson);
                    int globalFilmCounter = 0;
                    ArrayList<MLP_Film> films = new ArrayList<>();
                    for(int i=0; i < filmListObj.length(); i++) {
                        JSONObject filmObj = ((JSONObject) filmListObj.get(i));
                        globalFilmCounter = globalFilmCounter +1;
                        MLP_Film currentFilm = new MLP_Film();
                        currentFilm.title = filmObj.getString("title");
                        currentFilm.thumbUrl  = filmObj.getString("thumbnail");
                        currentFilm.id = toint(filmObj.getString("episode"));
                        currentFilm.codename = filmObj.getString("slug");
                        currentFilm.url_sub_fr = filmObj.getJSONObject("subtitles").getJSONObject("fr").getString("srt");
                        currentFilm.url_sub_en = filmObj.getJSONObject("subtitles").getJSONObject("en").getString("srt");
                        currentFilm.url_vf_240p = filmObj.getJSONObject("sources").getJSONObject("vf").getString("240");
                        currentFilm.url_vf_360p = filmObj.getJSONObject("sources").getJSONObject("vf").getString("360");
                        currentFilm.url_vf_480p = filmObj.getJSONObject("sources").getJSONObject("vf").getString("480");
                        currentFilm.url_vf_720p = filmObj.getJSONObject("sources").getJSONObject("vf").getString("720");
                        currentFilm.url_vf_1080p = filmObj.getJSONObject("sources").getJSONObject("vf").getString("1080");
                        currentFilm.url_vo_240p = filmObj.getJSONObject("sources").getJSONObject("vo").getString("240");
                        currentFilm.url_vo_360p = filmObj.getJSONObject("sources").getJSONObject("vo").getString("360");
                        currentFilm.url_vo_480p = filmObj.getJSONObject("sources").getJSONObject("vo").getString("480");
                        currentFilm.url_vo_720p = filmObj.getJSONObject("sources").getJSONObject("vo").getString("720");
                        currentFilm.url_vo_1080p = filmObj.getJSONObject("sources").getJSONObject("vo").getString("1080");
                        if(filmObj.getString("status").contains("Bient")) { currentFilm.released = false; } else { currentFilm.released = true; }
                        films.add(currentFilm);
                    }

                    // Prase news
                    JSONArray newsListObj = new JSONArray(newsJson);
                    ArrayList<NLR_news> newsList = new ArrayList<>();
                    for(int i=0; i < newsListObj.length(); i++) {
                        JSONObject newsObj = ((JSONObject) newsListObj.get(i));
                        NLR_news currentNews = new NLR_news();
                        currentNews.title = newsObj.getString("title");
                        currentNews.date = newsObj.getString("last_edit");
                        currentNews.url = newsObj.getString("url");
                        newsList.add(currentNews);
                    }

                    // Prase comics EN
                    JSONObject jsonObjComics = new JSONObject(comicsJson);
                    JSONArray jsonObjComicsEN = jsonObjComics.getJSONArray("en");
                    JSONArray jsonObjComicsFR = jsonObjComics.getJSONArray("fr");
                    ArrayList<NLR_ComicSeason> comicSeasonsEN = new ArrayList<>();
                    int globalComicCounterEN = 0;

                    for(int i=0; i < jsonObjComicsEN.length(); i++) {
                        JSONObject comicSeasonObj = ((JSONObject) jsonObjComicsEN.get(i));
                        NLR_ComicSeason currentSeason = new NLR_ComicSeason();
                        currentSeason.title = comicSeasonObj.getString("category");
                        JSONArray comicListObj = comicSeasonObj.getJSONArray("list");
                        ArrayList<NLR_Comic> comicsList = new ArrayList<>();
                        int localComicCounter = 0;
                        for(int ii=0; ii < comicListObj.length(); ii++) {
                            localComicCounter = localComicCounter +1;
                            globalComicCounterEN = globalComicCounterEN +1;
                            JSONObject comicObj = ((JSONObject) comicListObj.get(ii));
                            NLR_Comic currentComic = new NLR_Comic();
                            currentComic.ListTitle = currentSeason.title;
                            currentComic.icon_url = comicObj.getString("cover");
                            currentComic.title = comicObj.getString("title");
                            currentComic.idGlobal = String.valueOf(globalComicCounterEN);
                            currentComic.pdf_vo = comicObj.getJSONObject("files").getString("pdf");
                            comicsList.add(currentComic);
                        }
                        currentSeason.comicCount = localComicCounter;
                        currentSeason.commics = comicsList;
                        comicSeasonsEN.add(currentSeason);
                    }

                    //FR
                    ArrayList<NLR_ComicSeason> comicSeasonsFR = new ArrayList<>();
                    int globalComicCounterFR = 0;

                    for(int i=0; i < jsonObjComicsFR.length(); i++) {
                        JSONObject comicSeasonObj = ((JSONObject) jsonObjComicsFR.get(i));
                        NLR_ComicSeason currentSeason = new NLR_ComicSeason();
                        currentSeason.title = comicSeasonObj.getString("category");
                        JSONArray comicListObj = comicSeasonObj.getJSONArray("list");
                        ArrayList<NLR_Comic> comicsList = new ArrayList<>();
                        int localComicCounter = 0;
                        for(int ii=0; ii < comicListObj.length(); ii++) {
                            localComicCounter = localComicCounter +1;
                            globalComicCounterFR = globalComicCounterFR +1;
                            JSONObject comicObj = ((JSONObject) comicListObj.get(ii));
                            NLR_Comic currentComic = new NLR_Comic();
                            currentComic.ListTitle = currentSeason.title;
                            currentComic.icon_url = comicObj.getString("cover");
                            currentComic.title = comicObj.getString("title");
                            currentComic.idGlobal = String.valueOf(globalComicCounterFR);
                            currentComic.pdf_vo = comicObj.getJSONObject("files").getString("pdf");
                            comicsList.add(currentComic);
                        }
                        currentSeason.comicCount = localComicCounter;
                        currentSeason.commics = comicsList;
                        comicSeasonsFR.add(currentSeason);
                    }

                    // Prase bonus eps
                    JSONArray jsonObjBonusEpisodes = new JSONArray(bonusJson);
                    ArrayList<MLP_BonusSeason> seasonsBonus = new ArrayList<>();
                    int globalBonusEpisodeCounter = 0;
                    int globalBonusSeasonCounter = 0;

                    for(int i = 0; i < jsonObjBonusEpisodes.length() ; i++) {
                        globalBonusSeasonCounter++;
                        MLP_BonusSeason currentSeason = new MLP_BonusSeason();
                        JSONObject seasonObj = (JSONObject) jsonObjBonusEpisodes.get(i);
                        currentSeason.name = seasonObj.getString("title");
                        currentSeason.id = globalBonusSeasonCounter;

                        ArrayList<MLP_BonusEpisode> eps = new ArrayList<>();
                        JSONArray epListObjArray = seasonObj.getJSONArray("list");
                        for(int ii=0; ii < epListObjArray.length(); ii++) {
                            globalBonusEpisodeCounter = globalBonusEpisodeCounter +1;
                            JSONObject epObj = ((JSONObject) epListObjArray.get(ii));
                            MLP_BonusEpisode currentEp = new MLP_BonusEpisode();
                            currentEp.title = epObj.getString("title");
                            currentEp.thumbUrl  = epObj.getString("thumbnail");
                            currentEp.id_local = toint(epObj.getString("episode"));
                            currentEp.id_global = globalBonusEpisodeCounter;
                            currentEp.codename = epObj.getString("slug");
                            currentEp.in_season_num = globalSeasonCounter; //toint(epObj.getString("season"));
                            currentEp.url_sub_fr = epObj.getJSONObject("subtitles").getJSONObject("fr").getString("srt");
                            currentEp.url_sub_en = epObj.getJSONObject("subtitles").getJSONObject("en").getString("srt");
                            currentEp.url_vf_240p = epObj.getJSONObject("sources").getJSONObject("vf").getString("240");
                            currentEp.url_vf_360p = epObj.getJSONObject("sources").getJSONObject("vf").getString("360");
                            currentEp.url_vf_480p = epObj.getJSONObject("sources").getJSONObject("vf").getString("480");
                            currentEp.url_vf_720p = epObj.getJSONObject("sources").getJSONObject("vf").getString("720");
                            currentEp.url_vf_1080p = epObj.getJSONObject("sources").getJSONObject("vf").getString("1080");
                            currentEp.url_vo_240p = epObj.getJSONObject("sources").getJSONObject("vo").getString("240");
                            currentEp.url_vo_360p = epObj.getJSONObject("sources").getJSONObject("vo").getString("360");
                            currentEp.url_vo_480p = epObj.getJSONObject("sources").getJSONObject("vo").getString("480");
                            currentEp.url_vo_720p = epObj.getJSONObject("sources").getJSONObject("vo").getString("720");
                            currentEp.url_vo_1080p = epObj.getJSONObject("sources").getJSONObject("vo").getString("1080");
                            if(epObj.getString("status").contains("Bient")) { currentEp.released = false; } else { currentEp.released = true; }
                            if(epObj.getString("status").contains("Embed"))  { currentEp.embedded= true; } else { currentEp.embedded = false; }
                            currentEp.releaseDate = epObj.getString("stream_date");
                            eps.add(currentEp);
                        }
                        currentSeason.episodes = eps;
                        seasonsBonus.add(currentSeason);


                    }
/*
                    while( keysBonus.hasNext() ) {
                        String key = (String) keysBonus.next();
                        if ( jsonObjBonusEpisodes.get(key) instanceof JSONArray ) {
                            globalBonusSeasonCounter = globalBonusSeasonCounter +1;
                            MLP_BonusSeason currentSeason = new MLP_BonusSeason();
                            currentSeason.id = Integer.parseInt(key)-1;
                            ArrayList<MLP_BonusEpisode> eps = new ArrayList<>();
                            JSONArray epListObjArray = ((JSONArray) jsonObjBonusEpisodes.get(key));
                            for(int i=0; i < epListObjArray.length(); i++) {
                                globalBonusEpisodeCounter = globalBonusEpisodeCounter +1;
                                JSONObject epObj = ((JSONObject) epListObjArray.get(i));
                                MLP_BonusEpisode currentEp = new MLP_BonusEpisode();
                                currentEp.title = epObj.getString("title");
                                currentEp.thumbUrl  = epObj.getString("thumbnail");
                                currentEp.id_local = toint(epObj.getString("episode"));
                                currentEp.id_global = toint(epObj.getString("slug"));
                                currentEp.in_season_num = toint(epObj.getString("season"));
                                currentEp.url_sub_fr = epObj.getJSONObject("subtitles").getJSONObject("fr").getString("srt");
                                currentEp.url_sub_en = epObj.getJSONObject("subtitles").getJSONObject("en").getString("srt");
                                currentEp.url_vf_240p = epObj.getJSONObject("sources").getJSONObject("vf").getString("240");
                                currentEp.url_vf_360p = epObj.getJSONObject("sources").getJSONObject("vf").getString("360");
                                currentEp.url_vf_480p = epObj.getJSONObject("sources").getJSONObject("vf").getString("480");
                                currentEp.url_vf_720p = epObj.getJSONObject("sources").getJSONObject("vf").getString("720");
                                currentEp.url_vf_1080p = epObj.getJSONObject("sources").getJSONObject("vf").getString("1080");
                                currentEp.url_vo_240p = epObj.getJSONObject("sources").getJSONObject("vo").getString("240");
                                currentEp.url_vo_360p = epObj.getJSONObject("sources").getJSONObject("vo").getString("360");
                                currentEp.url_vo_480p = epObj.getJSONObject("sources").getJSONObject("vo").getString("480");
                                currentEp.url_vo_720p = epObj.getJSONObject("sources").getJSONObject("vo").getString("720");
                                currentEp.url_vo_1080p = epObj.getJSONObject("sources").getJSONObject("vo").getString("1080");
                                if(epObj.getString("status").contains("Bient")) { currentEp.released = false; } else { currentEp.released = true; }
                                if(epObj.getString("status").contains("Embed"))  { currentEp.embedded= true; } else { currentEp.embedded = false; }
                                currentEp.releaseDate = epObj.getString("stream_date");
                                eps.add(currentEp);
                            }
                            currentSeason.episodes = eps;
                            seasonsBonus.add(currentSeason);
                        }
                    }
*/

                    // Prase allied USING HTML

                    Document docAllies = Jsoup.connect("http://www.newlunarrepublic.fr/allies")
                            .maxBodySize(0)
                            .timeout(0)
                            .get();
                    Elements alliesListObj = docAllies.getElementsByClass("col md-6 text-center");
                    ArrayList<NLR_Allied> alliesList = new ArrayList<NLR_Allied>();
                    for(Element ally : alliesListObj) {
                        Element a = ally.getElementsByTag("a").first();
                        Element img = a.getElementsByTag("img").first();
                        NLR_Allied out = new NLR_Allied();
                        out.bannerUrl = "http://www.newlunarrepublic.fr" + img.attributes().get("src");
                        out.url = "" + a.attributes().get("href");
                        out.title = "" + a.text();
                        alliesList.add(out);
                    }

                    MLP_Content mlpresult = new MLP_Content();
                    mlpresult.seasons = seasons;
                    mlpresult.bonus = seasonsBonus;
                    mlpresult.totalEpisodes = globalEpisodeCounter;
                    mlpresult.totalSeason = globalSeasonCounter;
                    mlpresult.totalEpisodesBonus = globalBonusEpisodeCounter;
                    mlpresult.totalSeasonBonus = globalBonusSeasonCounter;
                    mlpresult.films = films;
                    mlpresult.totalFilms = globalFilmCounter;

                    NLR_Content nlrresult = new NLR_Content();
                    nlrresult.videos = mlpresult;
                    nlrresult.news = newsList;
                    nlrresult.commicsEN = comicSeasonsEN;
                    nlrresult.commicsFR = comicSeasonsFR;
                    nlrresult.totalComicsEN = globalComicCounterEN;
                    nlrresult.totalComicsFR = globalComicCounterFR;
                    nlrresult.allied = alliesList;

                    final MLP_Content mlp = mlpresult;
                    final NLR_Content nlr = nlrresult;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateViews(mlp,nlr);
                        }
                    });

                } catch (Exception e) {
                    Log.e("UpdateEpisodes", e.getMessage(), e);
                }
            }
        }).start();
    }
    public void updateEveryThingFromTheNlrUsingHTML() {
        new Thread(new Runnable() {
            public void run() {
                try {

                    LinkedBlockingQueue<Runnable> downaloadWorkQueue = new LinkedBlockingQueue<Runnable>();

                    ThreadPoolExecutor downloadThreadPool = new ThreadPoolExecutor(10, 20,
                            50, TimeUnit.MILLISECONDS, downaloadWorkQueue);

                    Document doc = Jsoup.connect("https://www.newlunarrepublic.fr/episodes").get();
                    Element content = doc.getElementById("tabs-elements");
                    Elements seasons = content.getElementsByClass("slideInRight");

                    int globalEpisodeCounter = 0;
                    int seasonCounter =0;
                    ArrayList<MLP_Season> seasonList = new ArrayList<MLP_Season>();
                    // Get episodes
                    for(Element season : seasons)  {
                        seasonCounter++;
                        Elements episodes = season.getElementsByClass("col sm-6 md-4 xl-3 text-center");
                        MLP_Season currentSeason = new MLP_Season();
                        ArrayList<MLP_Episode> epList = new ArrayList<MLP_Episode>();

                        int localEpisodeCounter = 0;
                        for(Element episode : episodes) {
                            globalEpisodeCounter++;
                            localEpisodeCounter++;

                            final MLP_Episode currentEpisode = new MLP_Episode();

                            Elements links = episode.getElementsByTag("a");
                            Element linkObj  = links.get(0);
                            Element thumObj = linkObj.getElementsByTag("img").get(0);
                            Element titleObj = linkObj.getElementsByTag("b").get(0);

                            int Realsed = episode.getElementsByClass("btn btn-sm btn-error").size();
                            int Embeded = episode.getElementsByClass("btn btn-sm btn-light").size();
                            Boolean epReleased = false; if(Realsed==0) { epReleased = true; }
                            Boolean epEmbeded = true; if(Embeded==0) { epEmbeded = false; }

                            String epReleaseIn = "Released"; if(!epReleased){
                                try {
                                    //WebDriver driver=new FirefoxDriver();
                                    //Document docEp = Jsoup.parse( new WebClient(BrowserVersion.CHROME).getPage("http://www.newlunarrepublic.fr/episodes/" + globalEpisodeCounter).asInstanceOf[HtmlPage].asXml );
                                    Document docEp = Jsoup.connect("http://www.newlunarrepublic.fr/episodes/" + globalEpisodeCounter)
                                            .maxBodySize(0)
                                            .timeout(0)
                                            .get();
                                    String raw = docEp.body().outerHtml();
                                    int start = raw.indexOf("var streamDate = '");
                                    int stop = raw.indexOf(" UTC'");
                                    epReleaseIn =  raw.substring(start,stop).replace("var streamDate = '","").replace("'","");
                                } catch (Exception e) {
                                    Log.e("GetReleaseDate",e.getMessage(),e);
                                    Document docEp = Jsoup.connect("http://www.newlunarrepublic.fr/episodes/" + globalEpisodeCounter).get();
                                    String raw = docEp.body().outerHtml();
                                    Log.e("GetReleaseDate",raw);
                                    epReleaseIn =  "Unknown";
                                }
                            }

                            currentEpisode.url =  "https://www.newlunarrepublic.fr" + linkObj.attributes().get("href");
                            currentEpisode.thumbUrl =  "https://www.newlunarrepublic.fr" + thumObj.attributes().get("src");
                            currentEpisode.title =  titleObj.text();
                            currentEpisode.released = epReleased;
                            currentEpisode.releaseDate = epReleaseIn;
                            currentEpisode.embedded = epEmbeded;
                            currentEpisode.id_local = localEpisodeCounter;
                            currentEpisode.id_global = globalEpisodeCounter;
                            currentEpisode.in_season_num = seasonCounter;
                            currentEpisode.url_sub_fr = "http://www.newlunarrepublic.fr/files/srt/MLP FiM - "+seasonCounter+"x"+addZero(localEpisodeCounter)+" FR.srt";
                            currentEpisode.url_sub_en = "http://www.newlunarrepublic.fr/files/srt/MLP FiM - "+seasonCounter+"x"+addZero(localEpisodeCounter)+" EN.srt";

                            if(epReleased){
                                final int seasonCounter2=seasonCounter;
                                final int localEpisodeCounter2=localEpisodeCounter;
                                final Thread thread = new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            currentEpisode.url_vo_1080p = CheckUrl("https://webm.newlunarrepublic.fr/videos/streaming/NLR-1080p-"+addZero(seasonCounter2)+"x"+addZero(localEpisodeCounter2)+".webm");
                                            currentEpisode.url_vo_720p = CheckUrl("https://webm.newlunarrepublic.fr/videos/streaming/NLR-720p-"+addZero(seasonCounter2)+"x"+addZero(localEpisodeCounter2)+".webm");
                                            currentEpisode.url_vo_480p = CheckUrl("https://webm.newlunarrepublic.fr/videos/streaming/NLR-480p-"+addZero(seasonCounter2)+"x"+addZero(localEpisodeCounter2)+".webm");
                                            currentEpisode.url_vo_360p = CheckUrl("https://webm.newlunarrepublic.fr/videos/streaming/NLR-360p-"+addZero(seasonCounter2)+"x"+addZero(localEpisodeCounter2)+".webm");
                                            currentEpisode.url_vo_240p = CheckUrl("https://webm.newlunarrepublic.fr/videos/streaming/NLR-240p-"+addZero(seasonCounter2)+"x"+addZero(localEpisodeCounter2)+".webm");
                                            currentEpisode.url_vf_1080p = CheckUrl("https://webm.newlunarrepublic.fr/videos/streaming/NLR-1080p-"+addZero(seasonCounter2)+"x"+addZero(localEpisodeCounter2)+"-VF.webm");
                                            currentEpisode.url_vf_720p = CheckUrl("https://webm.newlunarrepublic.fr/videos/streaming/NLR-720p-"+addZero(seasonCounter2)+"x"+addZero(localEpisodeCounter2)+"-VF.webm");
                                            currentEpisode.url_vf_480p = CheckUrl("https://webm.newlunarrepublic.fr/videos/streaming/NLR-480p-"+addZero(seasonCounter2)+"x"+addZero(localEpisodeCounter2)+"-VF.webm");
                                            currentEpisode.url_vf_360p = CheckUrl("https://webm.newlunarrepublic.fr/videos/streaming/NLR-360p-"+addZero(seasonCounter2)+"x"+addZero(localEpisodeCounter2)+"-VF.webm");
                                            currentEpisode.url_vf_240p = CheckUrl("https://webm.newlunarrepublic.fr/videos/streaming/NLR-240p-"+addZero(seasonCounter2)+"x"+addZero(localEpisodeCounter2)+"-VF.webm");
                                            currentThread().destroy();
                                        }
                                        catch (Exception e) {}
                                    }
                                };
                                downloadThreadPool.execute(thread);
                                //thread.start();
                            }

                            epList.add(currentEpisode);
                        }

                        currentSeason.name = "Season "+seasonCounter;
                        currentSeason.id = seasonCounter;
                        currentSeason.episodeCounts = localEpisodeCounter;
                        currentSeason.episodes = epList;

                        seasonList.add(currentSeason);
                    }

                    // Get film list :)
                    Document docFilms = Jsoup.connect("http://www.newlunarrepublic.fr/films")
                            .maxBodySize(0)
                            .timeout(0)
                            .get();
                    Elements filmObjList = docFilms.getElementsByClass("col sm-6 md-4 xl-3 text-center");
                    int globalFilmCounter = 0;
                    ArrayList<MLP_Film> filmList = new ArrayList<MLP_Film>();
                    for(Element filmObj : filmObjList){
                        MLP_Film currentFilm = new MLP_Film();
                        globalFilmCounter++;
                        Element a = filmObj.getElementsByTag("a").first();
                        int Realsed = filmObj.getElementsByClass("btn btn-sm btn-error").size();
                        Boolean epReleased = false; if(Realsed==0) { epReleased = true; }

                        currentFilm.codename = a.attributes().get("href").replace("/films/","");
                        currentFilm.thumbUrl = "http://www.newlunarrepublic.fr/" + filmObj.getElementsByTag("img").first().attributes().get("src");
                        currentFilm.id = globalFilmCounter;
                        currentFilm.released = epReleased;
                        currentFilm.title = filmObj.getElementsByTag("b").first().text();
                        currentFilm.url = "http://www.newlunarrepublic.fr/films/"+currentFilm.codename;
                        currentFilm.url_vo_240p = "https://webm.newlunarrepublic.fr/videos/streaming/NLR-240p-"+currentFilm.codename+".webm";
                        currentFilm.url_vo_360p = "https://webm.newlunarrepublic.fr/videos/streaming/NLR-360p-"+currentFilm.codename+".webm";
                        currentFilm.url_vo_480p = "https://webm.newlunarrepublic.fr/videos/streaming/NLR-480p-"+currentFilm.codename+".webm";
                        currentFilm.url_vo_720p = "https://webm.newlunarrepublic.fr/videos/streaming/NLR-720p-"+currentFilm.codename+".webm";
                        currentFilm.url_vo_1080p = "https://webm.newlunarrepublic.fr/videos/streaming/NLR-1080p-"+currentFilm.codename+".webm";
                        currentFilm.url_vf_240p = "https://webm.newlunarrepublic.fr/videos/streaming/NLR-240p-"+currentFilm.codename+"-VF.webm";
                        currentFilm.url_vf_360p = "https://webm.newlunarrepublic.fr/videos/streaming/NLR-360p-"+currentFilm.codename+"-VF.webm";
                        currentFilm.url_vf_480p = "https://webm.newlunarrepublic.fr/videos/streaming/NLR-480p-"+currentFilm.codename+"-VF.webm";
                        currentFilm.url_vf_720p = "https://webm.newlunarrepublic.fr/videos/streaming/NLR-720p-"+currentFilm.codename+"-VF.webm";
                        currentFilm.url_vf_1080p = "https://webm.newlunarrepublic.fr/videos/streaming/NLR-1080p-"+currentFilm.codename+"-VF.webm";
                        currentFilm.url_sub_en = "http://www.newlunarrepublic.fr/files/srt/MLP%20FiM%20-%20"+currentFilm.codename+"%20EN.srt";
                        currentFilm.url_sub_fr = "http://www.newlunarrepublic.fr/files/srt/MLP%20FiM%20-%20"+currentFilm.codename+"%20FR.srt";
                        filmList.add(currentFilm);
                    }
                    //// Done updateing videos






                    //// Start updating NLR
                    // Get allies
                    Document docAllies = Jsoup.connect("http://www.newlunarrepublic.fr/allies")
                            .maxBodySize(0)
                            .timeout(0)
                            .get();
                    Elements alliesListObj = docAllies.getElementsByClass("col md-6 text-center");
                    ArrayList<NLR_Allied> alliesList = new ArrayList<NLR_Allied>();
                    for(Element ally : alliesListObj) {
                        Element a = ally.getElementsByTag("a").first();
                        Element img = a.getElementsByTag("img").first();
                        NLR_Allied out = new NLR_Allied();
                        out.bannerUrl = "http://www.newlunarrepublic.fr" + img.attributes().get("src");
                        out.url = "" + a.attributes().get("href");
                        out.title = "" + a.text();
                        alliesList.add(out);
                    }

                    //Get news
                    Document docNews = Jsoup.connect("http://www.newlunarrepublic.fr/news")
                            .maxBodySize(0)
                            .timeout(0)
                            .get();
                    Elements NewsListObj = docNews.getElementById("content").getElementsByTag("h4");
                    ArrayList<NLR_news> NewsList = new ArrayList<NLR_news>();
                    for(Element news : NewsListObj) {
                        String date = news.getElementsByTag("small").first().text();
                        String url = "http://www.newlunarrepublic.fr" + news.getElementsByTag("a").first().attributes().get("href");
                        String newstitle = news.text().substring(0,news.text().length()-20);
                        NLR_news out = new NLR_news();
                        out.title = newstitle;
                        out.url = url;
                        out.date = date;
                        NewsList.add(out);
                    }

                    // Get comics
                    Document docComics = Jsoup.connect("http://www.newlunarrepublic.fr/comics")
                            .maxBodySize(0)
                            .timeout(0)
                            .get();

                    Element ComicPageVF = docComics.getElementsByClass("slideInRight").get(0).getElementsByClass("row comics").first();
                    Element ComicPageVO = docComics.getElementsByClass("slideInRight").get(1).getElementsByClass("row comics").first();

                    Element h4;
                    h4 = ComicPageVO.getElementsByTag("h4").first();

                    int globalComicsCounterEN = 0;
                    int localComicsCounterEN = 0;
                    ArrayList<NLR_ComicSeason> comicsSeasonsListEN = new ArrayList<>();
                    NLR_ComicSeason currentComicSeasonEN = new NLR_ComicSeason();
                    ArrayList<NLR_Comic> currentComicListEN = new ArrayList<>();
                    currentComicSeasonEN.title = h4.text();

                    while (true) {
                        Element next = h4.nextElementSibling();
                        if (next.nextElementSibling() != null && next.tagName().equals("div") ) {
                            globalComicsCounterEN++;
                            localComicsCounterEN++;
                            NLR_Comic currentComic = new NLR_Comic();
                            currentComic.title = next.getElementsByTag("b").text();
                            currentComic.pdf_vo = next.getElementsByTag("a").first().attributes().get("href");
                            currentComic.icon_url =  "http://www.newlunarrepublic.fr" + next.getElementsByTag("img").first().attributes().get("src");
                            currentComic.ListTitle = currentComicSeasonEN.title;
                            currentComicListEN.add(currentComic);
                            h4 = next;
                        } else if (next.nextElementSibling() != null && next.tagName().equals("h4")) {
                            currentComicSeasonEN.commics = currentComicListEN;
                            currentComicSeasonEN.comicCount=localComicsCounterEN;
                            comicsSeasonsListEN.add(currentComicSeasonEN);
                            localComicsCounterEN=0;
                            currentComicSeasonEN = new NLR_ComicSeason();
                            currentComicSeasonEN.title = h4.nextElementSibling().text();
                            h4 = next;
                        } else {
                            globalComicsCounterEN++;
                            localComicsCounterEN++;
                            NLR_Comic currentComic = new NLR_Comic();
                            currentComic.title = next.getElementsByTag("b").text();
                            currentComic.pdf_vo = next.getElementsByTag("a").first().attributes().get("href");
                            currentComic.icon_url =  "http://www.newlunarrepublic.fr" + next.getElementsByTag("img").first().attributes().get("src");
                            currentComic.ListTitle = currentComicSeasonEN.title;
                            currentComicListEN.add(currentComic);
                            currentComicSeasonEN.commics = currentComicListEN;
                            comicsSeasonsListEN.add(currentComicSeasonEN);
                            break;
                        }
                    }

                    h4 = ComicPageVF.getElementsByTag("h4").first();
                    int globalComicsCounterFR = 0;
                    int localComicsCounterFR = 0;
                    ArrayList<NLR_ComicSeason> comicsSeasonsListFR = new ArrayList<>();
                    NLR_ComicSeason currentComicSeasonFR = new NLR_ComicSeason();
                    ArrayList<NLR_Comic> currentComicListFR = new ArrayList<>();
                    currentComicSeasonFR.title = h4.text();

                    while (true) {
                        Element next = h4.nextElementSibling();
                        if (next.nextElementSibling() != null && next.tagName().equals("div") ) {
                            globalComicsCounterFR++;
                            localComicsCounterFR++;
                            NLR_Comic currentComic = new NLR_Comic();
                            currentComic.title = next.getElementsByTag("b").text();
                            currentComic.pdf_vo = next.getElementsByTag("a").first().attributes().get("href");
                            currentComic.icon_url = "http://www.newlunarrepublic.fr" + next.getElementsByTag("img").first().attributes().get("src");
                            currentComic.ListTitle = currentComicSeasonFR.title;
                            currentComicListFR.add(currentComic);
                            h4 = next;
                        } else if (next.nextElementSibling() != null && next.tagName().equals("h4")) {
                            currentComicSeasonFR.commics = currentComicListFR;
                            currentComicSeasonFR.comicCount=localComicsCounterFR;
                            comicsSeasonsListFR.add(currentComicSeasonFR);
                            localComicsCounterFR=0;
                            currentComicSeasonFR = new NLR_ComicSeason();
                            currentComicSeasonFR.title = h4.nextElementSibling().text();
                            h4 = next;
                        } else {
                            globalComicsCounterFR++;
                            localComicsCounterFR++;
                            NLR_Comic currentComic = new NLR_Comic();
                            currentComic.title = next.getElementsByTag("b").text();
                            currentComic.pdf_vo = next.getElementsByTag("a").first().attributes().get("href");
                            currentComic.icon_url = "http://www.newlunarrepublic.fr" + next.getElementsByTag("img").first().attributes().get("src");
                            currentComic.ListTitle = currentComicSeasonFR.title;
                            currentComicListFR.add(currentComic);

                            currentComicSeasonFR.commics = currentComicListFR;
                            comicsSeasonsListFR.add(currentComicSeasonFR);
                            break;
                        }
                    }


                    //Report Result
                    MLP_Content mlpresult = new MLP_Content();
                    mlpresult.totalEpisodes = globalEpisodeCounter;
                    mlpresult.totalSeason = seasonCounter;
                    mlpresult.seasons = seasonList;
                    mlpresult.films = filmList;
                    mlpresult.totalFilms = globalFilmCounter;

                    NLR_Content nlrresult = new NLR_Content();
                    nlrresult.videos = mlpresult;
                    nlrresult.allied = alliesList;
                    nlrresult.news = NewsList;
                    nlrresult.totalComicsFR = globalComicsCounterEN;
                    nlrresult.commicsEN = comicsSeasonsListEN;
                    nlrresult.totalComicsFR = globalComicsCounterFR;
                    nlrresult.commicsFR = comicsSeasonsListFR;

                    final MLP_Content mlp = mlpresult;
                    final NLR_Content nlr = nlrresult;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateViews(mlp,nlr);
                        }
                    });
                } catch (Exception e) {
                    Log.e("UpdateEpisodes",e.getMessage(),e);
                }
            }
        }).start();
    }
    public String getJSON(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }
    public int toint(String number) {
        if(number==null || number.equals("null")) { return 0; }
        return Integer.parseInt(number);
    }

    public void updateViews(MLP_Content mlp,NLR_Content nlr) {
        NLRVIDEOCONTENT = mlp;
        NLRCONTENT = nlr;
        Log.d("doneUpdating","DoneUdateingVIDEOCONTENT");
        ((TextView)findViewById(R.id.textView_loading)).setText(R.string.loadingscreen_updatingviews);

        try {
            updateViews_Episodes(NLRVIDEOCONTENT, NLRCONTENT);
            updateViews_Bonus(NLRVIDEOCONTENT, NLRCONTENT);
            updateViews_Film(NLRVIDEOCONTENT, NLRCONTENT);
            updateViews_Ally(NLRVIDEOCONTENT, NLRCONTENT);
            updateViews_News(NLRVIDEOCONTENT, NLRCONTENT);
            updateViews_ComicsEN(NLRVIDEOCONTENT, NLRCONTENT);
            updateViews_ComicsFR(NLRVIDEOCONTENT, NLRCONTENT);
            preventForkBombFromUpdate = 0;
        } catch (NullPointerException e) { preventForkBombFromUpdate += 1; if(preventForkBombFromUpdate <10) {updateFromTheNlr();} }

        ViewFlipper vf = (ViewFlipper)findViewById(R.id.layoutFlipper);
        vf.setDisplayedChild(0);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }
    public void updateViews_Episodes(MLP_Content mlp,NLR_Content nlr) {
        final ListView epList = (ListView) findViewById(R.id.listEpisode);
        final Spinner seList = (Spinner) findViewById(R.id.spinnerEpisodes);
        final ArrayList<String> seasons = new ArrayList<String>();
        for(int i = 0; i < NLRVIDEOCONTENT.totalSeason; i++) { int t = i; t++; seasons.add(getString(R.string.display_season) + " "+t);  }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, seasons);

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,seasons);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seList.setAdapter(aa);
        seList.setSelection(0);
        seList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<MLP_Episode> season1 = NLRVIDEOCONTENT.seasons.get(position).episodes;
                List<Episode> ep =  new ArrayList<Episode>();

                for(MLP_Episode e : season1) {
                    if(!e.released) {
                        ep.add(new Episode(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_NOTreleasedColor)));
                    } else if(e.embedded) {
                        ep.add(new Episode(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_embededColor)));
                    } else {
                        ep.add(new Episode(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_releasedColor)));
                    }
                }

                EpisodeAdaptater adapter = new EpisodeAdaptater(selector.this, ep);
                epList.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        ArrayList<MLP_Episode> season1 = NLRVIDEOCONTENT.seasons.get(0).episodes;
        List<Episode> ep =  new ArrayList<Episode>();


        for(MLP_Episode e : season1) {
            if(!e.released) {
                ep.add(new Episode(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_NOTreleasedColor)));
            } else if(e.embedded) {
                ep.add(new Episode(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_embededColor)));
            } else {
                ep.add(new Episode(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_releasedColor)));
            }

        }

        EpisodeAdaptater adapter = new EpisodeAdaptater(selector.this, ep);
        epList.setAdapter(adapter);
        epList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) { onEpisodeCLick(epList,seList,position);  }
        });
    }
    public void updateViews_Bonus(MLP_Content mlp,NLR_Content nlr) {
        final ListView epList = (ListView) findViewById(R.id.listBonus);
        final Spinner seList = (Spinner) findViewById(R.id.spinnerBonus);
        final ArrayList<String> seasons = new ArrayList<String>();
        for(int i = 0; i < NLRVIDEOCONTENT.totalSeasonBonus; i++) {
            int t = i; t++; seasons.add(NLRVIDEOCONTENT.bonus.get(i).name);
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, seasons);

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,seasons);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        seList.setAdapter(aa);
        seList.setSelection(0);
        seList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = seList.getItemAtPosition(position).toString();
                MLP_BonusSeason selectedSeason = new MLP_BonusSeason();
                for(MLP_BonusSeason s : mlp.bonus) { if(s.name.equals(selected)) { selectedSeason = s; }  }


                List<Bonus> ep =  new ArrayList<Bonus>();
                for(MLP_BonusEpisode e : selectedSeason.episodes) {
                    if(!e.released) {
                        ep.add(new Bonus(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_NOTreleasedColor)));
                    } else if(e.embedded) {
                        ep.add(new Bonus(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_embededColor)));
                    } else {
                        ep.add(new Bonus(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_releasedColor)));
                    }
                }

                BonusAdaptater adapter = new BonusAdaptater(selector.this, ep);
                epList.setAdapter(adapter);
                epList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) { onBonusCLick(epList,seList,position);  }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        List<Bonus> ep =  new ArrayList<Bonus>();
        for(MLP_BonusEpisode e : mlp.bonus.get(0).episodes) {
            if(!e.released) {
                ep.add(new Bonus(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_NOTreleasedColor)));
            } else if(e.embedded) {
                ep.add(new Bonus(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_embededColor)));
            } else {
                ep.add(new Bonus(e.thumbUrl,e.title,getString(R.string.display_episode)+" "+addZero(e.id_local)+" / "+getString(R.string.display_season)+" "+addZero(e.in_season_num),getResources().getColor(R.color.episode_releasedColor)));
            }
        }

        BonusAdaptater adapter = new BonusAdaptater(selector.this, ep);
        epList.setAdapter(adapter);
        epList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) { onBonusCLick(epList,seList,position);  }
        });

    }
    public void updateViews_Film(MLP_Content mlp,NLR_Content nlr) {
        final ListView fiList = (ListView) findViewById(R.id.listFilms);
        ArrayList<MLP_Film> films = NLRVIDEOCONTENT.films;
        List<Film> fi =  new ArrayList<Film>();
        for(MLP_Film a : films) { String modfiedTitle = a.title.replace(":",":\n"); fi.add(new Film(a.thumbUrl,modfiedTitle)); }
        FilmAdaptater adapter2 = new FilmAdaptater(selector.this, fi);
        fiList.setAdapter(adapter2);
        fiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) { onFilmClick(fiList,position);  }
        });
    }
    public void updateViews_Ally(MLP_Content mlp,NLR_Content nlr) {
        final ListView alList = (ListView) findViewById(R.id.listAllies);
        ArrayList<NLR_Allied> ally = NLRCONTENT.allied;
        List<Ally> al =  new ArrayList<Ally>();
        for(NLR_Allied a : ally) { String modfiedTitle = a.title; al.add(new Ally(a.bannerUrl, modfiedTitle, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                social_openlink(a.url);
            }
        })); }
        AllyAdaptater adapter3 = new AllyAdaptater(selector.this, al);
        alList.setAdapter(adapter3);
        alList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) { onAllyClick(alList,position);  }
        });
    }
    public void updateViews_News(MLP_Content mlp,NLR_Content nlr) {
        final ListView neList = (ListView) findViewById(R.id.listNews);
        ArrayList<NLR_news> news = NLRCONTENT.news;
        List<News> ne =  new ArrayList<News>();
        for(NLR_news n : news) {
            ne.add(new News(n.title, n.date, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    social_openlink(n.url);
                }
            }));
        }
        NewsAdaptater adapter4 = new NewsAdaptater(selector.this, ne);
        neList.setAdapter(adapter4);
        neList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) { onNewsClick(neList,position);  }
        });
    }
    public void updateViews_ComicsEN(MLP_Content mlp,NLR_Content nlr) {
        final ListView coVOList = (ListView) findViewById(R.id.listComicsVO);
        final Spinner coVOSpinner = (Spinner) findViewById(R.id.spinnerComicsVO);
        ArrayList<NLR_ComicSeason> co = NLRCONTENT.commicsEN;
        List<Comic> col =  new ArrayList<Comic>();

        final ArrayList<String> list = new ArrayList<String>();
        for(NLR_ComicSeason cc : co) {
            list.add(cc.title);
        }

        ArrayAdapter aaa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, list);
        aaa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        coVOSpinner.setAdapter(aaa);
        coVOSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String cat = coVOSpinner.getItemAtPosition(position).toString();
                for(NLR_ComicSeason cc : co) {
                    if(cc.title.equals(cat)) {
                        col.clear();
                        for(NLR_Comic c : cc.commics) {
                            if(c.ListTitle.equals(cat)) {
                                col.add(new Comic(c.title,cc.title,c.icon_url, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        social_openlink(c.pdf_vo);
                                        //Log.wtf("ONCLICKCOMIC",cc.title + " / " + c.title);
                                        //Log.wtf("ONCLICKCOMIC",c.pdf_vo);
                                    }
                                }));
                            }
                        }
                        ComicAdaptater a = new ComicAdaptater(selector.this, col);
                        coVOList.setAdapter(a);
                    }
                }
            }

            @Override  public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        coVOList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) { onComicENClick(coVOList,position);  }
        });
    }
    public void updateViews_ComicsFR(MLP_Content mlp,NLR_Content nlr) {
        final ListView coVFRList = (ListView) findViewById(R.id.listComicsFR);
        final Spinner coVFRSpinner = (Spinner) findViewById(R.id.spinnerComicsFR);
        ArrayList<NLR_ComicSeason> coVFR = NLRCONTENT.commicsFR;
        List<Comic> colVFR =  new ArrayList<Comic>();

        final ArrayList<String> lista = new ArrayList<String>();
        for(NLR_ComicSeason cc : coVFR) {
            lista.add(cc.title);
        }
        ArrayAdapter aaaa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, lista);
        aaaa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        coVFRSpinner.setAdapter(aaaa);

        coVFRSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String cat = coVFRSpinner.getItemAtPosition(position).toString();
                for(NLR_ComicSeason cc : coVFR) {
                    if(cc.title.equals(cat)) {
                        colVFR.clear();
                        for(NLR_Comic c : cc.commics) {
                            if(c.ListTitle.equals(cat)) {
                                colVFR.add(new Comic(c.title,cc.title,c.icon_url, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        social_openlink(c.pdf_vo);
                                        //Log.wtf("ONCLICKCOMIC",cc.title + " / " + c.title);
                                        //Log.wtf("ONCLICKCOMIC",c.pdf_vo);
                                    }
                                }));
                            }
                        }
                        ComicAdaptater a = new ComicAdaptater(selector.this, colVFR);
                        coVFRList.setAdapter(a);
                    }
                }
            }

            @Override  public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        coVFRList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) { onComicFRClick(coVFRList,position);  }
        });
    }

    public void onComicENClick(final ListView coVOList, final int position) {}
    public void onComicFRClick(final ListView coFRList, final int position) {}
    public void onAllyClick(final ListView alList, final int position) {}
    public void onNewsClick(final ListView neList, final int position) {}
    public void onFilmClick(final ListView fiList, final int position) {
        final ProgressDialog dialog = ProgressDialog.show(new ContextThemeWrapper(selector.this, R.style.AlertDialogCustom), "Loading", "Please wait...", true);
        dialog.setCancelable(false);
        new Thread(new Runnable() {
            public void run() {
                MLP_Film selectedFilm = getFilmByNumber(position+1);
                if(selectedFilm.released) {
                    final ArrayList<String> quality = new ArrayList();


                    if(CheckUrl2(selectedFilm.url_vo_240p) != null) {   quality.add("VO / 240p"); }
                    if(CheckUrl2(selectedFilm.url_vo_360p) != null) {   quality.add("VO / 360p"); }
                    if(CheckUrl2(selectedFilm.url_vo_480p) != null) {   quality.add("VO / 480p"); }
                    if(CheckUrl2(selectedFilm.url_vo_720p) != null) {   quality.add("VO / 720p"); }
                    if(CheckUrl2(selectedFilm.url_vo_1080p) != null){   quality.add("VO / 1080p"); }
                    if(CheckUrl2(selectedFilm.url_vf_240p) != null) {   quality.add("VF / 240p"); }
                    if(CheckUrl2(selectedFilm.url_vf_360p) != null) {   quality.add("VF / 360p"); }
                    if(CheckUrl2(selectedFilm.url_vf_480p) != null) {   quality.add("VF / 480p"); }
                    if(CheckUrl2(selectedFilm.url_vf_720p) != null) {   quality.add("VF / 720p"); }
                    if(CheckUrl2(selectedFilm.url_vf_1080p) != null){   quality.add("VF / 1080p"); }


                    final ArrayList<String> subs = new ArrayList();
                    subs.add("None");
                    if(CheckUrl2(selectedFilm.url_sub_fr) != null){   subs.add("SUB FR"); }
                    if(CheckUrl2(selectedFilm.url_sub_en) != null){   subs.add("SUB EN"); }

                    dialog.dismiss();


                    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(selector.this, R.style.AlertDialogCustom));
                    CharSequence[] cs = quality.toArray(new CharSequence[quality.size()]);

                    System.out.println(quality); // [foo, bar, waa]

                    builder.setTitle("Pick");
                    builder.setItems(cs, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String text = quality.get(which);

                            String url= null;
                            if(text == "VO / 240p") { url = selectedFilm.url_vo_240p; }
                            if(text == "VO / 360p") { url = selectedFilm.url_vo_360p; }
                            if(text == "VO / 480p") { url = selectedFilm.url_vo_480p; }
                            if(text == "VO / 720p") { url = selectedFilm.url_vo_720p; }
                            if(text == "VO / 1080p") { url = selectedFilm.url_vo_1080p; }
                            if(text == "VF / 240p") { url = selectedFilm.url_vf_240p; }
                            if(text == "VF / 360p") { url = selectedFilm.url_vf_360p; }
                            if(text == "VF / 480p") { url = selectedFilm.url_vf_480p; }
                            if(text == "VF / 720p") { url = selectedFilm.url_vf_720p; }
                            if(text == "VF / 1080p") { url = selectedFilm.url_vf_1080p; }

                            final AlertDialog.Builder builder2 = new AlertDialog.Builder(new ContextThemeWrapper(selector.this, R.style.AlertDialogCustom));
                            CharSequence[] cs = subs.toArray(new CharSequence[subs.size()]);

                            final String vidurl = url;
                            builder2.setTitle("Subs");
                            builder2.setItems(cs, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String text = subs.get(i);
                                    String suburl= "none";
                                    if(text == "None") { suburl = "none"; }
                                    if(text == "SUB FR") { suburl = selectedFilm.url_sub_fr; }
                                    if(text == "SUB EN") { suburl = selectedFilm.url_sub_fr; }

                                    Intent myIntent = new Intent(selector.this, VideoPlayerV2.class);
                                    Bundle b = new Bundle();
                                    b.putString("vidurl",vidurl); //Your id
                                    b.putString("suburl",suburl); //Your id
                                    b.putInt("theme",getAppThemeForPlayer()); //Your id
                                    b.putString("title",selectedFilm.title); //Your id
                                    myIntent.putExtras(b);
                                    startActivity(myIntent);
                                }
                            });
                            builder2.show();
                        }
                    });


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            builder.show();
                        }
                    });
                } else if(!selectedFilm.released) {
                    dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast t = Toast.makeText(selector.this ,"Sorry this film isn't avalible yet.\n If you think it's a bug contact me",Toast.LENGTH_SHORT);
                            t.show();
                        }
                    });
                }
            }
        }).start();
    }
    public void onEpisodeCLick(final ListView epList , final Spinner seList, final int position) {
        //(new ContextThemeWrapper(selector.this, R.style.AlertDialogCustom)
        final ProgressDialog dialog = ProgressDialog.show(new ContextThemeWrapper(selector.this, R.style.AlertDialogCustom), "Loading", "Please wait...", true);
        dialog.setCancelable(false);
        new Thread(new Runnable() {
            public void run() {
                int epPos=position+1;
                int sePos=seList.getSelectedItemPosition()+1;
                final MLP_Episode clickedEpisode = getEpisodeByNumber(epPos,sePos);

                if(clickedEpisode.released && !clickedEpisode.embedded) {

                    final ArrayList<String> quality = new ArrayList();


                    if(CheckUrl2(clickedEpisode.url_vo_240p) != null) {   quality.add("VO / 240p"); }
                    if(CheckUrl2(clickedEpisode.url_vo_360p) != null) {   quality.add("VO / 360p"); }
                    if(CheckUrl2(clickedEpisode.url_vo_480p) != null) {   quality.add("VO / 480p"); }
                    if(CheckUrl2(clickedEpisode.url_vo_720p) != null) {   quality.add("VO / 720p"); }
                    if(CheckUrl2(clickedEpisode.url_vo_1080p) != null){   quality.add("VO / 1080p"); }
                    if(CheckUrl2(clickedEpisode.url_vf_240p) != null) {   quality.add("VF / 240p"); }
                    if(CheckUrl2(clickedEpisode.url_vf_360p) != null) {   quality.add("VF / 360p"); }
                    if(CheckUrl2(clickedEpisode.url_vf_480p) != null) {   quality.add("VF / 480p"); }
                    if(CheckUrl2(clickedEpisode.url_vf_720p) != null) {   quality.add("VF / 720p"); }
                    if(CheckUrl2(clickedEpisode.url_vf_1080p) != null){   quality.add("VF / 1080p"); }

                    final ArrayList<String> subs = new ArrayList();
                    subs.add("None");
                    if(CheckUrl2(clickedEpisode.url_sub_fr) != null){   subs.add("SUB FR"); }
                    if(CheckUrl2(clickedEpisode.url_sub_en) != null){   subs.add("SUB EN"); }

                    dialog.dismiss();

                    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(selector.this, R.style.AlertDialogCustom));
                    CharSequence[] cs = quality.toArray(new CharSequence[quality.size()]);

                    builder.setTitle("Pick");
                    builder.setItems(cs, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String text = quality.get(which);

                            String url= null;
                            if(text == "VO / 240p") { url = clickedEpisode.url_vo_240p; }
                            if(text == "VO / 360p") { url = clickedEpisode.url_vo_360p; }
                            if(text == "VO / 480p") { url = clickedEpisode.url_vo_480p; }
                            if(text == "VO / 720p") { url = clickedEpisode.url_vo_720p; }
                            if(text == "VO / 1080p") { url = clickedEpisode.url_vo_1080p; }
                            if(text == "VF / 240p") { url = clickedEpisode.url_vf_240p; }
                            if(text == "VF / 360p") { url = clickedEpisode.url_vf_360p; }
                            if(text == "VF / 480p") { url = clickedEpisode.url_vf_480p; }
                            if(text == "VF / 720p") { url = clickedEpisode.url_vf_720p; }
                            if(text == "VF / 1080p") { url = clickedEpisode.url_vf_1080p; }

                            final AlertDialog.Builder builder2 = new AlertDialog.Builder(new ContextThemeWrapper(selector.this, R.style.AlertDialogCustom));
                            CharSequence[] cs = subs.toArray(new CharSequence[subs.size()]);

                            final String vidurl = url;
                            builder2.setTitle("Subs");
                            builder2.setItems(cs, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String suburl= "empty";
                                    String text = subs.get(i);
                                    if(text == "None") { suburl = "none"; }
                                    if(text == "SUB FR") { suburl = clickedEpisode.url_sub_fr; }
                                    if(text == "SUB EN") { suburl = clickedEpisode.url_sub_en; }

                                    Intent myIntent = new Intent(selector.this, VideoPlayerV2.class);
                                    Bundle b = new Bundle();
                                    b.putString("vidurl",vidurl); //Your id
                                    b.putString("suburl",suburl); //Your id
                                    b.putInt("theme",getAppThemeForPlayer()); //Your id
                                    b.putString("title",clickedEpisode.title); //Your id
                                    myIntent.putExtras(b);
                                    startActivity(myIntent);
                                }
                            });
                            builder2.show();
                        }
                    });


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            builder.show();
                        }
                    });
                } else if(!clickedEpisode.released) {
                    final String Rtext = "Release date: "+clickedEpisode.releaseDate;
                    dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast t = Toast.makeText(selector.this ,"Sorry this episode isn't avalible yet.\n"+Rtext+" \nIf you think it's a bug contact me",Toast.LENGTH_SHORT);
                            t.show();
                        }
                    });
                } else if(clickedEpisode.embedded) {
                    dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast t = Toast.makeText(selector.this ,"Sorry this episode is embedded (Not supported yet).\nGo at newlunarrepublic.fr to watch it \nIf you think it's a bug contact me",Toast.LENGTH_SHORT);
                            t.show();
                        }
                    });
                }
            }}).start();
    }
    public void onBonusCLick(final ListView epList , final Spinner seList, final int position) {
        final ProgressDialog dialog = ProgressDialog.show(new ContextThemeWrapper(selector.this, R.style.AlertDialogCustom), "Loading", "Please wait...", true);
        dialog.setCancelable(false);
        new Thread(new Runnable() {
            public void run() {
                int epPos=position+1;
                int sePos=seList.getSelectedItemPosition()+1;
                final MLP_BonusEpisode clickedEpisode = getBonusEpisodeByNumber(epPos,sePos);

                if(clickedEpisode.released && !clickedEpisode.embedded) {

                    final ArrayList<String> quality = new ArrayList();


                    if(CheckUrl2(clickedEpisode.url_vo_240p) != null) {   quality.add("VO / 240p"); }
                    if(CheckUrl2(clickedEpisode.url_vo_360p) != null) {   quality.add("VO / 360p"); }
                    if(CheckUrl2(clickedEpisode.url_vo_480p) != null) {   quality.add("VO / 480p"); }
                    if(CheckUrl2(clickedEpisode.url_vo_720p) != null) {   quality.add("VO / 720p"); }
                    if(CheckUrl2(clickedEpisode.url_vo_1080p) != null){   quality.add("VO / 1080p"); }
                    if(CheckUrl2(clickedEpisode.url_vf_240p) != null) {   quality.add("VF / 240p"); }
                    if(CheckUrl2(clickedEpisode.url_vf_360p) != null) {   quality.add("VF / 360p"); }
                    if(CheckUrl2(clickedEpisode.url_vf_480p) != null) {   quality.add("VF / 480p"); }
                    if(CheckUrl2(clickedEpisode.url_vf_720p) != null) {   quality.add("VF / 720p"); }
                    if(CheckUrl2(clickedEpisode.url_vf_1080p) != null){   quality.add("VF / 1080p"); }

                    final ArrayList<String> subs = new ArrayList();
                    subs.add("None");
                    if(CheckUrl2(clickedEpisode.url_sub_fr) != null){   subs.add("SUB FR"); }
                    if(CheckUrl2(clickedEpisode.url_sub_en) != null){   subs.add("SUB EN"); }

                    dialog.dismiss();

                    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(selector.this, R.style.AlertDialogCustom));
                    CharSequence[] cs = quality.toArray(new CharSequence[quality.size()]);

                    builder.setTitle("Pick");
                    builder.setItems(cs, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String text = quality.get(which);

                            String url= null;
                            if(text == "VO / 240p") { url = clickedEpisode.url_vo_240p; }
                            if(text == "VO / 360p") { url = clickedEpisode.url_vo_360p; }
                            if(text == "VO / 480p") { url = clickedEpisode.url_vo_480p; }
                            if(text == "VO / 720p") { url = clickedEpisode.url_vo_720p; }
                            if(text == "VO / 1080p") { url = clickedEpisode.url_vo_1080p; }
                            if(text == "VF / 240p") { url = clickedEpisode.url_vf_240p; }
                            if(text == "VF / 360p") { url = clickedEpisode.url_vf_360p; }
                            if(text == "VF / 480p") { url = clickedEpisode.url_vf_480p; }
                            if(text == "VF / 720p") { url = clickedEpisode.url_vf_720p; }
                            if(text == "VF / 1080p") { url = clickedEpisode.url_vf_1080p; }

                            final AlertDialog.Builder builder2 = new AlertDialog.Builder(new ContextThemeWrapper(selector.this, R.style.AlertDialogCustom));
                            CharSequence[] cs = subs.toArray(new CharSequence[subs.size()]);

                            final String vidurl = url;
                            builder2.setTitle("Subs");
                            builder2.setItems(cs, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String suburl= "empty";
                                    String text = subs.get(i);
                                    if(text == "None") { suburl = "none"; }
                                    if(text == "SUB FR") { suburl = clickedEpisode.url_sub_fr; }
                                    if(text == "SUB EN") { suburl = clickedEpisode.url_sub_fr; }



                                    Intent myIntent = new Intent(selector.this, VideoPlayerV2.class);
                                    Bundle b = new Bundle();
                                    b.putString("vidurl",vidurl); //Your id
                                    b.putString("suburl",suburl); //Your id
                                    b.putInt("theme",getAppThemeForPlayer()); //Your id
                                    b.putString("title",clickedEpisode.title); //Your id
                                    myIntent.putExtras(b);
                                    startActivity(myIntent);
                                }
                            });
                            builder2.show();
                        }
                    });


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            builder.show();
                        }
                    });
                } else if(!clickedEpisode.released) {
                    final String Rtext = "Release date: "+clickedEpisode.releaseDate;
                    dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast t = Toast.makeText(selector.this ,"Sorry this episode isn't avalible yet.\n"+Rtext+" \nIf you think it's a bug contact me",Toast.LENGTH_SHORT);
                            t.show();
                        }
                    });
                } else if(clickedEpisode.embedded) {
                    dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast t = Toast.makeText(selector.this ,"Sorry this episode is embedded (Not supported yet).\nGo at newlunarrepublic.fr to watch it \nIf you think it's a bug contact me",Toast.LENGTH_SHORT);
                            t.show();
                        }
                    });
                }
            }}).start();
    }

    public int getAppTheme() {
        try {
            int id = translateTheme_toID(getConfig_getTheme()); //getPackageManager().getActivityInfo(getComponentName(), 0).getThemeResource();
            return id;
        } catch (Exception e) {
            Log.e("getAppTheme","FUCKITTHISISTOOCOMPLICATED_IRAGEQUIT",e);
            return R.style.FullscreenTheme;
        }
    }
    public int getAppThemeForPlayer() {
        String appThemeStr = getResources().getResourceEntryName(getAppTheme());
        String appPlayerThemeStr = appThemeStr + "_Player";
        int appPlayerThemeId = R.style.MLPTheme_KingSombra_Player;
        try {
            appPlayerThemeId = getResources().getIdentifier(appPlayerThemeStr, "style", getPackageName());
        } catch (Exception e) {
            Log.e("GetAppThmeForPlayer","?",e);
        }
        return appPlayerThemeId;
    }
    public void sendBugReport() {
        String title = "[NLRViewer] new bug: " + ((TextView) findViewById(R.id.sendmail_MsgTitle)).getText().toString();
        String msg = "BugReport message:\n\t Reply to: "+ ((TextView) findViewById(R.id.sendmail_ReplyTo)).getText().toString() +"\n"+ ((TextView) findViewById(R.id.sendmail_MsgText)).getText().toString();


        Log.i("Send email", "");
        String[] TO = {"turtleforgamingapps@gmail.com"};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");

        //emailIntent.setData(Uri.parse("mailto:"));
        //emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        emailIntent.putExtra(Intent.EXTRA_TEXT, msg);

        try {
            Intent i =Intent.createChooser(emailIntent, getText(R.string.email_intentText));
            i.setType("text/plain");
            startActivity(i);
            finish();
            Log.i("sendBugReport", "Finished sending email...");
            Toast.makeText(selector.this, getText(R.string.email_sended), Toast.LENGTH_SHORT).show();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(selector.this, getText(R.string.email_needinstall), Toast.LENGTH_SHORT).show();
        }
    }

    public void bindSocialButtons() {
        ImageButton webBtn = findViewById(R.id.button_nlr_web);
        ImageButton fbBtn = findViewById(R.id.button_nlr_fb);
        ImageButton twBtn = findViewById(R.id.button_nlr_twitter);
        ImageButton mlBtn = findViewById(R.id.button_nlr_sendmail);
        ImageButton diBtn = findViewById(R.id.button_nlr_discord);
        ImageButton payBtn = findViewById(R.id.button_nlr_paypal);
        fbBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_openlink(getResources().getString(R.string.social_nlr_web));  } });
        fbBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_openfb(getResources().getString(R.string.social_nlr_fbPage),getResources().getString(R.string.social_nlr_fbPage_id));  } });
        twBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_opentwitter(getResources().getString(R.string.social_nlr_twitter).substring(1));  } });
        mlBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_senmail(getResources().getString(R.string.social_nlr_email));  } });
        diBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_openlink(getResources().getString(R.string.social_nlr_discord));  } });
        webBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_openlink(getResources().getString(R.string.social_nlr_web));  } });
        payBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_openpaypal(getResources().getString(R.string.social_nlr_paypal_btnid));  } });
    }
    public void bindSocialButtonsForDev() {
        ImageButton webBtn = findViewById(R.id.btnAboutDev_web);
        ImageButton gitBtn = findViewById(R.id.btnAboutDev_git);
        ImageButton mailBtn = findViewById(R.id.btnAboutDev_mail);
        ImageButton ppalBtn = findViewById(R.id.btnAboutDev_ppal);
        ImageButton btcBtn = findViewById(R.id.btnAboutDev_btc);
        Button appBtn = findViewById(R.id.btnAboutDev_appapi);
        webBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_openlink(getResources().getString(R.string.aboutdev_web));  } });
        gitBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_openlink(getResources().getString(R.string.aboutdev_github));  } });
        mailBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_senmail(getResources().getString(R.string.aboutdev_mail));  } });
        ppalBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_openlink(getResources().getString(R.string.aboutdev_ppal));  } });
        btcBtn.setOnClickListener(new View.OnClickListener() {  @Override public void onClick(View view) { social_copyClipboard(getResources().getString(R.string.aboutdev_btc).replace("BTC: ",""));  } });
        appBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View view) { ((ViewFlipper)findViewById(R.id.layoutFlipper)).setDisplayedChild(9); }});
    }

    public void social_copyClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("BTC",text);
        clipboard.setPrimaryClip(clip);
        Toast t = Toast.makeText(selector.this ,getResources().getString(R.string.aboutdev_copiedtoclip),Toast.LENGTH_SHORT);
        t.show();
    }
    public void social_openlink(String url) {
        /*
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(url));
        startActivity(browserIntent);
        */

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, Uri.parse(url));
    }
    public void social_opentwitter(String username) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("twitter://user?screen_name="+username)));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/#!/"+username)));
        }
    }
    public void social_openpaypal(String btnid) {
        social_openlink("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=" + btnid);
    }
    public void social_openfb(String page,String pageId) {
        try {
            startActivity( new Intent(Intent.ACTION_VIEW,
                    Uri.parse("fb://page/"+pageId)));
        } catch (Exception e) {
            startActivity( new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.facebook.com/"+page)));
        }
    }
    public void social_senmail(String mail) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { mail });
            intent.putExtra(Intent.EXTRA_SUBJECT, "");
            intent.putExtra(Intent.EXTRA_TEXT, "");
            startActivity(Intent.createChooser(intent, "MAIL"));
            Toast.makeText(selector.this, getText(R.string.email_sended), Toast.LENGTH_SHORT).show();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(selector.this, getText(R.string.email_needinstall), Toast.LENGTH_SHORT).show();
        }
    }

    public int spinner_wereis(Spinner spin, String query) {
        int itemcount = spin.getAdapter().getCount();
        for(int i=0; i<itemcount; i++) {
            if(query.equals(spin.getItemAtPosition(i).toString())) {
                Log.v("spinner_wereis",spin.getItemAtPosition(i).toString()+" == "+query);
                return i;
            } else {
                Log.v("spinner_wereis",spin.getItemAtPosition(i).toString()+" != "+query);
            }
        }
        return -1;
    }

    public void settings_bindViews() {
        Spinner ThemeSelector = findViewById(R.id.config_spinner_theme);
        Spinner LangSelector = findViewById(R.id.config_spinner_lang);
        Button saveBtn = findViewById(R.id.config_btn_save);

        int toSelectItem = spinner_wereis(ThemeSelector,getConfig_getThemeConf());
        if(toSelectItem==-1) {toSelectItem=0;}
        ThemeSelector.setSelection(toSelectItem);
        ThemeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = ThemeSelector.getItemAtPosition(i).toString();
                if (selected.equals("Random")) {
                    findViewById(R.id.layout_randomthemeselector).setVisibility(View.VISIBLE);
                    writeConfig_writeTheme("Random");
                } else {
                    findViewById(R.id.layout_randomthemeselector).setVisibility(View.GONE);
                    writeConfig_writeTheme(selected);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        int toSelectItem2 = spinner_wereis(LangSelector,getConfig_getLang());
        if(toSelectItem2==-1) {toSelectItem2=0;}
        LangSelector.setSelection(toSelectItem2);
        LangSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                writeConfig_writeLang(LangSelector.getItemAtPosition(i).toString());
                updateLocale(translateLang_toCodeName(LangSelector.getItemAtPosition(i).toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartApp();
            }
        });

        Switch themeRandomDefault  = findViewById(R.id.switch_theme_random_default);
        Switch themeRandomTwilight = findViewById(R.id.switch_theme_random_twilight);
        Switch themeRandomApple    = findViewById(R.id.switch_theme_random_apple);
        Switch themeRandomPinkie   = findViewById(R.id.switch_theme_random_pinkie);
        Switch themeRandomRarity   = findViewById(R.id.switch_theme_random_rarity);
        Switch themeRandomFlutter  = findViewById(R.id.switch_theme_random_flutter);
        Switch themeRandomRainbow  = findViewById(R.id.switch_theme_random_rainbow);
        Switch themeRandomSpike    = findViewById(R.id.switch_theme_random_spike);
        Switch themeRandomLuna     = findViewById(R.id.switch_theme_random_luna);
        Switch themeRandomCelestia = findViewById(R.id.switch_theme_random_celestia);
        Switch themeRandomKingSombra = findViewById(R.id.switch_theme_random_kingsombra);
        themeRandomDefault.setOnClickListener(new View.OnClickListener() { @Override  public void onClick(View view) {  writeConfig_randomThemes(); } });
        themeRandomTwilight.setOnClickListener(new View.OnClickListener() { @Override  public void onClick(View view) {  writeConfig_randomThemes(); } });
        themeRandomApple.setOnClickListener(new View.OnClickListener() { @Override  public void onClick(View view) {  writeConfig_randomThemes(); } });
        themeRandomPinkie.setOnClickListener(new View.OnClickListener() { @Override  public void onClick(View view) {  writeConfig_randomThemes(); } });
        themeRandomRarity.setOnClickListener(new View.OnClickListener() { @Override  public void onClick(View view) {  writeConfig_randomThemes(); } });
        themeRandomFlutter.setOnClickListener(new View.OnClickListener() { @Override  public void onClick(View view) {  writeConfig_randomThemes(); } });
        themeRandomRainbow.setOnClickListener(new View.OnClickListener() { @Override  public void onClick(View view) {  writeConfig_randomThemes(); } });
        themeRandomSpike.setOnClickListener(new View.OnClickListener() { @Override  public void onClick(View view) {  writeConfig_randomThemes(); } });
        themeRandomLuna.setOnClickListener(new View.OnClickListener() { @Override  public void onClick(View view) {  writeConfig_randomThemes(); } });
        themeRandomCelestia.setOnClickListener(new View.OnClickListener() { @Override  public void onClick(View view) {  writeConfig_randomThemes(); } });
        themeRandomKingSombra.setOnClickListener(new View.OnClickListener() { @Override  public void onClick(View view) {  writeConfig_randomThemes(); } });
        updateSwitches();
    }

    public String translateLang_toCodeName(String full) {
        switch (full) {
            case "Francais":
                return "fr";
            case "English":
                return "en";
            default:
                return "fr";
        }
    }
    public void updateLocale(String codename) {
        Locale l;
        switch (codename) {
            case "fr":
                l = Locale.FRENCH;
                break;
            case "en":
                l = Locale.ENGLISH;
                break;
            default:
                l = Locale.FRENCH;
        }

        Resources res = getBaseContext().getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        android.content.res.Configuration conf = res.getConfiguration();
        conf.setLocale(l); // API 17+ only.
        res.updateConfiguration(conf, dm);
    }

    public void updateSwitches() {
        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences("app_theme", MODE_PRIVATE);
        Set<String> set = new HashSet<String>(); set.add("Default");
        Set<String> randomSelected = sharedPreferences.getStringSet("appthemerandom",set);
        List<String> list = new ArrayList<String>(randomSelected);

        Switch themeRandomDefault = findViewById(R.id.switch_theme_random_default);
        Switch themeRandomTwilight = findViewById(R.id.switch_theme_random_twilight);
        Switch themeRandomApple = findViewById(R.id.switch_theme_random_apple);
        Switch themeRandomPinkie = findViewById(R.id.switch_theme_random_pinkie);
        Switch themeRandomRarity = findViewById(R.id.switch_theme_random_rarity);
        Switch themeRandomFlutter = findViewById(R.id.switch_theme_random_flutter);
        Switch themeRandomRainbow = findViewById(R.id.switch_theme_random_rainbow);
        Switch themeRandomSpike = findViewById(R.id.switch_theme_random_spike);
        Switch themeRandomLuna = findViewById(R.id.switch_theme_random_luna);
        Switch themeRandomCelestia = findViewById(R.id.switch_theme_random_celestia);
        Switch themeRandomKingSombra = findViewById(R.id.switch_theme_random_kingsombra);

        themeRandomDefault.setChecked(false);
        themeRandomTwilight.setChecked(false);
        themeRandomApple.setChecked(false);
        themeRandomPinkie.setChecked(false);
        themeRandomRarity.setChecked(false);
        themeRandomFlutter.setChecked(false);
        themeRandomRainbow.setChecked(false);
        themeRandomSpike.setChecked(false);
        themeRandomLuna.setChecked(false);
        themeRandomCelestia.setChecked(false);
        themeRandomKingSombra.setChecked(false);

        for(String k : list) {
            if(k.equals("Default")) { themeRandomDefault.setChecked(true); }
            if(k.equals("Twilight Sparke")) { themeRandomTwilight.setChecked(true); }
            if(k.equals("Apple Jack")) { themeRandomApple.setChecked(true); }
            if(k.equals("Pinkie Pie")) { themeRandomPinkie.setChecked(true); }
            if(k.equals("Rarity")) { themeRandomRarity.setChecked(true); }
            if(k.equals("Flutter Shy")) { themeRandomFlutter.setChecked(true); }
            if(k.equals("Rainbow Dash")) { themeRandomRainbow.setChecked(true); }
            if(k.equals("Spike")) { themeRandomSpike.setChecked(true); }
            if(k.equals("Luna")) { themeRandomLuna.setChecked(true); }
            if(k.equals("Celestia")) { themeRandomCelestia.setChecked(true); }
            if(k.equals("King Sombra")) { themeRandomKingSombra.setChecked(true); }
        }
    }
    public void writeConfig_writeLang(String theme) {
        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences("app_theme", MODE_PRIVATE);
        sharedPreferences.edit()
                .putString("applang",theme)
                .apply();
    }
    public void writeConfig_writeTheme(String theme) {
        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences("app_theme", MODE_PRIVATE);
        sharedPreferences.edit()
                .putString("apptheme",theme)
                .apply();
    }
    public void writeConfig_randomThemes() {
        Switch themeRandomDefault  = findViewById(R.id.switch_theme_random_default);
        Switch themeRandomTwilight = findViewById(R.id.switch_theme_random_twilight);
        Switch themeRandomApple    = findViewById(R.id.switch_theme_random_apple);
        Switch themeRandomPinkie   = findViewById(R.id.switch_theme_random_pinkie);
        Switch themeRandomRarity   = findViewById(R.id.switch_theme_random_rarity);
        Switch themeRandomFlutter  = findViewById(R.id.switch_theme_random_flutter);
        Switch themeRandomRainbow  = findViewById(R.id.switch_theme_random_rainbow);
        Switch themeRandomSpike    = findViewById(R.id.switch_theme_random_spike);
        Switch themeRandomLuna     = findViewById(R.id.switch_theme_random_luna);
        Switch themeRandomCelestia = findViewById(R.id.switch_theme_random_celestia);
        Switch themeRandomKingSombra = findViewById(R.id.switch_theme_random_kingsombra);
        ArrayList<String> arr = new ArrayList<>();
        Set<String> set = new HashSet<String>();
        if(themeRandomDefault.isChecked()) { set.add("Default"); }
        if(themeRandomTwilight.isChecked()) { set.add("Twilight Sparke"); }
        if(themeRandomApple.isChecked()) { set.add("Apple Jack"); }
        if(themeRandomPinkie.isChecked()) { set.add("Pinkie Pie"); }
        if(themeRandomRarity.isChecked()) { set.add("Rarity"); }
        if(themeRandomFlutter.isChecked()) { set.add("Flutter Shy"); }
        if(themeRandomRainbow.isChecked()) { set.add("Rainbow Dash"); }
        if(themeRandomSpike.isChecked()) { set.add("Spike"); }
        if(themeRandomLuna.isChecked()) { set.add("Luna"); }
        if(themeRandomCelestia.isChecked()) { set.add("Celestia"); }
        if(themeRandomKingSombra.isChecked()) { set.add("King Sombra"); }
        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences("app_theme", MODE_PRIVATE);
        sharedPreferences.edit()
                .putStringSet("appthemerandom",set)
                .apply();
    }
    public String getConfig_getThemeConf() {
        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences("app_theme", MODE_PRIVATE);
        String value = sharedPreferences.getString("apptheme","Default");
        return value;
    }
    public String getConfig_getTheme() {
        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences("app_theme", MODE_PRIVATE);
        String value = sharedPreferences.getString("apptheme","Default");
        Set<String> set = new HashSet<String>();
        set.add("Default");
        Set<String> randomSelected = sharedPreferences.getStringSet("appthemerandom",set);
        if(value.equals("Random")) {
            List<String> list = new ArrayList<String>(randomSelected);
            if(list.size() == 0) {
                list.add("Default");
                Set<String> sett = new HashSet<String>();
                sett.add("Default");
                getBaseContext().getSharedPreferences("app_theme", MODE_PRIVATE).edit().putStringSet("appthemerandom",sett).apply();
            }
            String newtheme = getRandomChestItem(list);
            value = newtheme;
        }
        return value;
    }
    public static String getRandomChestItem(List<String> items) {
        return items.get(new Random().nextInt(items.size()));
    }
    public String getConfig_getLang() {
        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences("app_theme", MODE_PRIVATE);
        String value = sharedPreferences.getString("applang","fr");
        return value;
    }
    public int translateTheme_toID(String themeName) {
        switch (themeName) {
            case "Twilight Sparkle":
                return R.style.MLPTheme_TwilightSparkle;
            case "Apple Jack":
                return R.style.MLPTheme_AppleJack;
            case "Pinkie Pie":
                return R.style.MLPTheme_PinkiePie;
            case "Rarity":
                return R.style.MLPTheme_Rarity;
            case "Flutter Shy":
                return R.style.MLPTheme_FluterShy;
            case "Rainbow Dash":
                return R.style.MLPTheme_RainBowDash;
            case "Spike":
                return R.style.MLPTheme_Spike;
            case "Luna":
                return R.style.MLPTheme_Luna;
            case "Celestia":
                return R.style.MLPTheme_Celestia;
            case "King Sombra":
                return R.style.MLPTheme_KingSombra;
            default:
                return R.style.AppTheme;
        }
    }
    public String translateTheme_toName(int ThemeID) {
        switch (ThemeID) {
            case R.style.MLPTheme_TwilightSparkle:
                return "Twilight Sparkle";
            case R.style.MLPTheme_AppleJack:
                return "Apple Jack";
            case R.style.MLPTheme_PinkiePie:
                return "Pinkie Pie";
            case R.style.MLPTheme_Rarity:
                return "Rarity";
            case R.style.MLPTheme_FluterShy:
                return "Flutter Shy";
            case R.style.MLPTheme_RainBowDash:
                return "Rainbow Dash";
            case R.style.MLPTheme_Spike:
                return "Spike";
            case R.style.MLPTheme_Luna:
                return "Luna";
            case R.style.MLPTheme_Celestia:
                return "Celestia";
            case R.style.MLPTheme_KingSombra:
                return "King Sombra";
            default:
                return "Default";
        }
    }
    public void restartApp() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(i);
    }

    public MLP_Film getFilmByNumber(int f) {  return NLRVIDEOCONTENT.films.get(f-1); }
    public MLP_Episode getEpisodeByNumber(int ep, int se) { return NLRVIDEOCONTENT.seasons.get(se-1).episodes.get(ep-1); }
    public MLP_BonusEpisode getBonusEpisodeByNumber(int ep, int se) { return NLRVIDEOCONTENT.bonus.get(se-1).episodes.get(ep-1); }
    public String addZero(int i) { if(i<10) { return "0"+ i; } else { return ""+i;  } }

    public String CheckUrl(String url) {
        // TO LONG
        return url;
        /*

        try{
            URL urll = new URL(url);
            HttpURLConnection huc =  ( HttpURLConnection )  urll.openConnection ();

            huc.setRequestMethod ("GET");  //OR  huc.setRequestMethod ("HEAD");
            huc.connect () ;
            int code = huc.getResponseCode() ;
            Log.w("RESULT",url+" : "+code);
            huc.disconnect();
            huc= null;
            if(code==200)
                return url;
            else
                return null;
        } catch (Exception e) { return null; }
        */
    }
    public String CheckUrl2(String url) {
        if(url==null || url.equals("null")) {return null;}
        try{
            URL urll = new URL(url);
            HttpURLConnection huc =  ( HttpURLConnection )  urll.openConnection ();

            huc.setRequestMethod ("GET");  //OR  huc.setRequestMethod ("HEAD");
            huc.connect () ;
            int code = huc.getResponseCode() ;
            Log.d("CheckUrl2",url+" : "+code);
            huc.disconnect();
            huc= null;
            if(code==200)
                return url;
            else
                return null;
        } catch (Exception e) { Log.e("CheckUrl2","Error",e); return null; }
    }

    public void CheckUpdate() {
        final String CurrentRevisonURL = "http://www.tugler.fr/samuel/turtleforgamingapps/files/download?file=nlr_viewer&latest=true&getrevision=true";
        new Thread(new Runnable() {
            public void run() {
                try {
                    Document doc = Jsoup.connect(CurrentRevisonURL).get();
                    String a = doc.text();
                    if(!a.equals(CURRENTREVISION) && !DEBUGMODE) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        social_openlink("http://www.tugler.fr/samuel/turtleforgamingapps/files/download?file=nlr_viewer&latest=true");
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(selector.this, R.style.AlertDialogCustom));
                        builder.setTitle(getString(R.string.action_newupdate))
                                .setMessage(getString(R.string.action_newversion) + ": \n" + a + "\n\n"+getString(R.string.action_actualversion)+":\n"+CURRENTREVISION)
                                .setPositiveButton("Download", dialogClickListener)
                                .setNegativeButton("Pass", dialogClickListener);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.show();
                            }
                        });
                    } else {
                        if(DEBUGMODE) {
                            Toast.makeText(getApplication(), "Woaw you're running in debug mode", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    Log.e("CheckUrl", "Thread", e);
                    Toast.makeText(getApplication(), "Error:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).start();
    }

    public class MLP_Episode {
        public String title;
        public int id_global;
        public int id_local;
        public int in_season_num;
        public Boolean released;
        public String releaseDate;
        public Boolean embedded;
        public String url;
        public String thumbUrl;
        public String url_vo_240p = null;
        public String url_vo_360p = null;
        public String url_vo_480p = null;
        public String url_vo_720p = null;
        public String url_vo_1080p = null;
        public String url_vf_240p = null;
        public String url_vf_360p = null;
        public String url_vf_480p = null;
        public String url_vf_720p = null;
        public String url_vf_1080p = null;
        public String url_sub_fr = null;
        public String url_sub_en = null;
        public String url_sub_thumbnail = null;
    }
    public class MLP_Season  {
        public String name;
        public int id;
        public int episodeCounts;
        public ArrayList<MLP_Episode> episodes;
    }
    public class MLP_BonusEpisode {
        public String title;
        public int id_local;
        public int id_global;
        public String codename;
        public int in_season_num;
        public Boolean released;
        public String releaseDate;
        public Boolean embedded;
        public String url;
        public String thumbUrl;
        public String url_vo_240p = null;
        public String url_vo_360p = null;
        public String url_vo_480p = null;
        public String url_vo_720p = null;
        public String url_vo_1080p = null;
        public String url_vf_240p = null;
        public String url_vf_360p = null;
        public String url_vf_480p = null;
        public String url_vf_720p = null;
        public String url_vf_1080p = null;
        public String url_sub_fr = null;
        public String url_sub_en = null;
    }
    public class MLP_BonusSeason  {
        public String name;
        public int id;
        public int episodeCounts;
        public ArrayList<MLP_BonusEpisode> episodes;
    }
    public class MLP_Film {
        public String title;
        public int id;
        public Boolean released;
        public String url;
        public String thumbUrl;
        public String codename;
        public String url_vo_240p = null;
        public String url_vo_360p = null;
        public String url_vo_480p = null;
        public String url_vo_720p = null;
        public String url_vo_1080p = null;
        public String url_vf_240p = null;
        public String url_vf_360p = null;
        public String url_vf_480p = null;
        public String url_vf_720p = null;
        public String url_vf_1080p = null;
        public String url_sub_fr = null;
        public String url_sub_en = null;
        public String url_sub_thumbnail = null;
    }
    public class MLP_Content {
        public int totalEpisodes;
        public int totalSeason;
        public int totalFilms;
        public int totalEpisodesBonus;
        public int totalSeasonBonus;
        public ArrayList<MLP_Season> seasons;
        public ArrayList<MLP_Film> films;
        public ArrayList<MLP_BonusSeason> bonus;
    }

    public class NLR_news{
        public String title;
        public String date;
        public String content;
        public String url;
    }
    public class NLR_Allied {
        public String title;
        public String url;
        public String bannerUrl;
    }
    public class NLR_Comic {
        public String title;
        public String ListTitle;
        public String idGlobal;
        public String pdf_vo;
        public String icon_url;
    }
    public class NLR_ComicSeason {
        public String title;
        public int comicCount;
        public ArrayList<NLR_Comic> commics;
    }
    public class NLR_Content {
        public MLP_Content videos;
        public ArrayList<NLR_Allied> allied;
        public ArrayList<NLR_ComicSeason> commicsFR;
        public int totalComicsFR;
        public ArrayList<NLR_ComicSeason> commicsEN;
        public int totalComicsEN;
        public ArrayList<NLR_news> news;
    }
}

