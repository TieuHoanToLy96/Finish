package view;

import android.app.Dialog;
import android.beotron.tieuhoan.kara_2.MainApp;
import android.beotron.tieuhoan.kara_2.R;
import android.beotron.tieuhoan.kara_2.VideoYouTube;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import adapter.AdapterListVideo;
import adapter.HomeAdapter;
import model.Song;
import ulti.HangSo;
import ulti.Json;

/**
 * Created by TieuHoan on 26/02/2017.
 */

public class SearchFragment extends Fragment implements SearchView.OnQueryTextListener {

    private RecyclerView recyclerView;
    private ArrayList<Song> songsResult;
    private View view;
    private String pathSearch;
    private AdapterListVideo adapterListVideo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            pathSearch = bundle.getString("PATH_SONGS_OF_SINGER");
            Log.e("tieuhoan", pathSearch);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.search_fragment, null);

        if (pathSearch != null) {
            new Json.Load(handler, getActivity()).execute(pathSearch);
        }

        MainApp.searchView.setOnQueryTextListener(this);


        return view;
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == HangSo.KEY_HANDLER) {
                songsResult = ((ArrayList<Song>) msg.obj);
                if (songsResult != null) {
                    setUpRecycle();
                } else {
                    Dialog dialog = new Dialog(getActivity());
                    dialog.setTitle("Error");
                    dialog.show();
                    dialog.setCancelable(true);
                }
            }
        }
    };

    public void setUpRecycle() {
        adapterListVideo = new AdapterListVideo(songsResult, getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.idSongResultSearch);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(adapterListVideo);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapterListVideo.setOnItemClickRecycle(new HomeAdapter.OnItemClickRecycle() {
            @Override
            public void OnItemClick(View view, int position) {
                Bundle bundle = new Bundle();
                Log.e("song", songsResult.get(position).getTittle());
                bundle.putSerializable("SONG", songsResult.get(position));
                bundle.putSerializable("SONGS", songsResult);
                Intent intent = new Intent(getActivity(), VideoYouTube.class);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

    }


    @Override
    public boolean onQueryTextSubmit(String query) {

        View view = getActivity().getCurrentFocus();

        if (view != null) {
            InputMethodManager im = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromInputMethod(view.getWindowToken(), 0);
        }

        try {
            pathSearch = HangSo.API_URI2 + "&maxResults=20" + "&q=" + URLEncoder.encode("Karaoke" + query, "UTF-8") + "&type=video" + "&key=" + HangSo.KEY_BROWSE;
            Log.e("pathJson result", pathSearch);
            new Json.Load(handler, getContext()).execute(pathSearch);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        MainApp.searchView.clearFocus();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        //auto complete
        String test;
        try {
            test = "http://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q=" + URLEncoder.encode(newText, "UTF-8");
//            Log.e("test", test);
//            test = "http://suggestqueries.google.com/complete/search?q="+ URLEncoder.encode(newText, "UTF-8") +"&client=toolbar&hl=vie&ds=yt";
//            String json = ReaderJson.readerJson(test);
//            Log.e("json", json);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return true;
    }

    Handler handler2 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HangSo.KEY_HANDLER2) {
                super.handleMessage(msg);
                songsResult.addAll((ArrayList<Song>) msg.obj);
                adapterListVideo.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseAll();
    }

    public void releaseAll() {
        pathSearch = null;
        songsResult.clear();
    }
}
