package com.pitchedapps.primenumbercalculator;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 7681 on 2015-12-30.
 */
public class CalculatorCreditsFragment extends Fragment {

    private List<Person> persons;
    private RecyclerView rv;

//    public CalculatorCreditsFragment() {
//        // Required empty public constructor
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.advanced_credits, container, false);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);

        initializeData();
        initializeAdapter();

//        rv.setHasFixedSize(true);



    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initializeData(){
        persons = new ArrayList<>();
        persons.add(new Person("Emma Wilson", "23 years old", android.R.drawable.arrow_up_float));
        persons.add(new Person("Lavery Maiss", "25 years old", android.R.drawable.arrow_up_float));
        persons.add(new Person("Lillie Watts", "35 years old", android.R.drawable.arrow_up_float));
    }

    private void initializeAdapter(){
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(persons);
        rv.setAdapter(adapter);
        Log.d("PNC ", "Adapter count is " + adapter.getItemCount());
    }

}
