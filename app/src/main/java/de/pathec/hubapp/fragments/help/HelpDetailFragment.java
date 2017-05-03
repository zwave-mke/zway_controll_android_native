package de.pathec.hubapp.fragments.help;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import de.pathec.hubapp.IMainActivityCommunicator;
import de.pathec.hubapp.R;
import de.pathec.hubapp.util.Util;

public class HelpDetailFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_SUBTITLE = "subtitle";
    private static final String ARG_TEXT = "subtitle";
    private String mTitle;
    private String mSubtitle;
    private String mText;


    private IMainActivityCommunicator mActivityCommunicator;

    public HelpDetailFragment() {
        // Required empty public constructor
    }

    public static HelpDetailFragment newInstance(String title, String subtitle, String text) {
        HelpDetailFragment fragment = new HelpDetailFragment();

        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_SUBTITLE, subtitle);
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
            mSubtitle = getArguments().getString(ARG_SUBTITLE);
            mText = getArguments().getString(ARG_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help_detail, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() == null) {
            Util.showMessage(getActivity(), getString(R.string.unexpected_error));
            mActivityCommunicator.showDefaultFragment(true);
            return;
        }

        String languageCode = Locale.getDefault().getLanguage();

        Set<String> supportedLanguages = new HashSet<>();
        supportedLanguages.add("de");
        supportedLanguages.add("en");

        if (!supportedLanguages.contains(languageCode)) {
            languageCode = "en";
        }

        final WebView webView = (WebView) getView().findViewById(R.id.fragment_help_detail_web_view);
        final String finalLanguageCode = languageCode;
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                webView.loadUrl("file:///android_asset/www/error_" + finalLanguageCode + ".html");
            }
        });
        webView.loadUrl("file:///android_asset/www/" + mText +"_" + languageCode + ".html");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivityCommunicator = (IMainActivityCommunicator) getActivity();

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear(); // Remove all items

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(mTitle);

        if(mActivityCommunicator != null) {
            mActivityCommunicator.setDrawerItemSelected(6);
            mActivityCommunicator.setTabStripVisibility(false);
            mActivityCommunicator.setFabVisibility(false);
            mActivityCommunicator.showBackArrow();
            // mActivityCommunicator.showHamburgerIcon();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mActivityCommunicator.showHamburgerIcon();
    }
}
