package com.marcosedo.lagramola;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HelpFragment extends Fragment {

    private ListView listView;
    private ArrayList<Map<String, Object>> data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configurarToolbar();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.help, container, false);


        //////////LISTVIEW, ADAPTER Y TEXTO DE LOS MENUS
        final Resources resources = getResources();
        listView = (ListView) view.findViewById(android.R.id.list);
        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        String[] titles_str =  {resources.getString(R.string.appVersionTxt),resources.getString(R.string.feedbackMail), resources.getString(R.string.OSLicenses)};
        String[] subtitles_str =  {resources.getString(R.string.appVersionNumber),resources.getString(R.string.myEmailAddress), ""};

        for (int i=0; i<titles_str.length; i++) {
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("title", titles_str[i]);
            datum.put("subtitle", subtitles_str[i]);
            data.add(datum);
        }

        SimpleAdapter adapter = new SimpleAdapter(getActivity(), data,
                android.R.layout.simple_list_item_2,
                new String[]{"title", "subtitle"},
                new int[]{android.R.id.text1,
                        android.R.id.text2});
        listView.setAdapter(adapter);

        //LISTENER DEL LIST VIEW esto nos saca el context menu con un solo click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterview, View v, int position,long arg3) {

                Resources resource = getActivity().getResources();

                switch (position) {
                    case 1:
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto", Constantes.MY_EMAIL , null));
                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
                        startActivity(Intent.createChooser(emailIntent, "Enviar email..."));
                        break;
                    case 2:
                        String LicenseInfo = resource.getString(R.string.headerOSLicenses)+GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getActivity());
                        AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(getActivity());
                        LicenseDialog.setTitle(resource.getString(R.string.OSLicenses));

                        if (LicenseInfo != null) {
                            LicenseDialog.setMessage(LicenseInfo);
                            LicenseDialog.show();
                            break;
                        }
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu != null) {//Escondemos el añadir evento
            menu.setGroupVisible(R.id.tab_listaeventos_admin_group, false);
            menu.setGroupVisible(R.id.base_group,false);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        getVisibility();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPause(){
        super.onStop();
        lostVisibility();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void lostVisibility(){
        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabLayout);
        tabLayout.setVisibility(View.VISIBLE);
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewPager);
        viewPager.setVisibility(View.VISIBLE);
        FrameLayout outTabContent = (FrameLayout) getActivity().findViewById(R.id.outTabContent);
        outTabContent.setVisibility(View.GONE);
    }

    private void getVisibility(){
        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabLayout);
        tabLayout.setVisibility(View.GONE);
        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.viewPager);
        viewPager.setVisibility(View.GONE);
        FrameLayout outTabContent = (FrameLayout) getActivity().findViewById(R.id.outTabContent);
        outTabContent.setVisibility(View.VISIBLE);
    }

    private void configurarToolbar() {
        setHasOptionsMenu(true);
        ((Toolbar) (getActivity()).findViewById(R.id.toolbar))
                .setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View w) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                });
    }
}
