package com.progy.elan.companq;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    /*
    Our data object
    */
    public class Spacecraft {
        /*
        INSTANCE FIELDS
        */
        private String name;
        private String city;
        private String country;
        /*
        GETTERS AND SETTERS
        */
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getCity() {
            return city;
        }
        public void setCity(String city) {
            this.city = city;
        }
        public String getCountry() {
            return country;
        }
        public void setCountry(String country) {
            this.country = country;
        }
        /*
        TOSTRING
        */
        @Override
        public String toString() {
            return name;
        }
    }
    class FilterHelper extends Filter {
        ArrayList<Spacecraft> currentList;
        ListViewAdapter adapter;
        Context c;
        public FilterHelper(ArrayList<Spacecraft> currentList, ListViewAdapter adapter,Context c) {
            this.currentList = currentList;
            this.adapter = adapter;
            this.c=c;
        }
        /*
        - Perform actual filtering.
        */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults=new FilterResults();
            if(constraint != null && constraint.length()>0)
            {
//CHANGE TO UPPER
                constraint=constraint.toString().toUpperCase();
//HOLD FILTERS WE FIND
                ArrayList<Spacecraft> foundFilters=new ArrayList<>();
                Spacecraft spacecraft=null;
//ITERATE CURRENT LIST
                for (int i=0;i<currentList.size();i++)
                {
                    spacecraft= currentList.get(i);
//SEARCH
                    if(spacecraft.getName().toUpperCase().contains(constraint) )
                    {
//ADD IF FOUND
                        foundFilters.add(spacecraft);
                    }
                }
//SET RESULTS TO FILTER LIST
                filterResults.count=foundFilters.size();
                filterResults.values=foundFilters;
            }else
            {
//NO ITEM FOUND.LIST REMAINS INTACT
                filterResults.count=currentList.size();
                filterResults.values=currentList;
            }
//RETURN RESULTS
            return filterResults;
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            adapter.setSpacecrafts((ArrayList<Spacecraft>) filterResults.values);
            adapter.refresh();
        }
    }
    /*
    Our custom adapter class
    */
    public class ListViewAdapter extends BaseAdapter implements Filterable {
        Context c;
        ArrayList<Spacecraft> spacecrafts;
        public ArrayList<Spacecraft> currentList;
        FilterHelper filterHelper;
        public ListViewAdapter(Context c, ArrayList<Spacecraft> spacecrafts) {
            this.c = c;
            this.spacecrafts = spacecrafts;
            this.currentList=spacecrafts;
        }
        @Override
        public int getCount() {
            return spacecrafts.size();
        }
        @Override
        public Object getItem(int i) {
            return spacecrafts.get(i);
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view==null)
            {
                view= LayoutInflater.from(c).inflate(R.layout.model,viewGroup,false);
            }
            TextView txtName = view.findViewById(R.id.nameTextView);
            TextView txtCity = view.findViewById(R.id.cityTextView);
            TextView txtCountry = view.findViewById(R.id.countryTextView);
            final Spacecraft s= (Spacecraft) this.getItem(i);
            txtName.setText(s.getName());
            txtCity.setText(s.getCity());
            txtCountry.setText(s.getCountry());
//chkTechExists.setEnabled(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(c, s.getName(), Toast.LENGTH_SHORT).show();
                }
            });
            return view;
        }
        public void setSpacecrafts(ArrayList<Spacecraft> filteredSpacecrafts)
        {
            this.spacecrafts=filteredSpacecrafts;
        }
        @Override
        public Filter getFilter() {
            if(filterHelper==null)
            {
                filterHelper=new FilterHelper(currentList,this,c);
            }
            return filterHelper;
        }
        public void refresh(){
            notifyDataSetChanged();
        }
    }
    /*
    Our HTTP Client
    */
    public class JSONDownloader {
        //SAVE/RETRIEVE URLS
        private static final String JSON_DATA_URL="http://api.citybik.es/v2/networks";
        //INSTANCE FIELDS
        private final Context c;
        public JSONDownloader(Context c) {
            this.c = c;
        }
        /*
        Fetch JSON Data
        */
        public ArrayList<Spacecraft> retrieve(final ListView mListView, final ProgressBar myProgressBar)
        {
            final ArrayList<Spacecraft> downloadedData=new ArrayList<>();
            myProgressBar.setIndeterminate(true);
            myProgressBar.setVisibility(View.VISIBLE);
            AndroidNetworking.get(JSON_DATA_URL)
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {

                            Spacecraft s;
                            //napraj response so i da e sega da ti e toa od(pod) newtworks
                            try
                            {
                                JSONObject jo = new JSONObject(String.valueOf(response));
                                JSONArray companies = jo.getJSONArray("networks");
                                for(int i=0;i<companies.length();i++)
                                {
                                    JSONObject c = companies.getJSONObject(i);
                                    //jo=response.getJSONObject(i);

                                    //response = jo.getJSONArray("networks");
                                    //String id = jo.getString("id");
                                    //String href = c.getString("href");
                                    //od ovde nadolu e star kod
                                    String name=c.getString("name");

                                    //location isJSON Object
                                    JSONObject location = c.getJSONObject("location");
                                    String city=location.getString("city");
                                    String country=location.getString("country");
                                    s=new Spacecraft();
                                    s.setName(name);
                                    s.setCity(city);
                                    s.setCountry(country);
                                    downloadedData.add(s);
                                }
                                myProgressBar.setVisibility(View.GONE);
                            }catch (JSONException e)
                            {
                                myProgressBar.setVisibility(View.GONE);
                                Toast.makeText(c, "GOOD RESPONSE BUT JAVA CAN'T PARSE JSON IT RECEIEVED. "+e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                        //ERROR
                        @Override
                        public void onError(ANError anError) {
                            anError.printStackTrace();
                            myProgressBar.setVisibility(View.GONE);
                            Toast.makeText(c, "UNSUCCESSFUL :  ERROR IS : "+anError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
            return downloadedData;
        }
    }
    ArrayList<Spacecraft> spacecrafts = new ArrayList<>();
    ListView myListView;
    ListViewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myListView= findViewById(R.id.myListView);
        //final kaj progres bar
         ProgressBar myProgressBar= findViewById(R.id.myProgressBar);
       // SearchView mySearchView=findViewById(R.id.mySearchView);
      //  mySearchView.setIconified(true);
      //  mySearchView.setOnSearchClickListener(new View.OnClickListener() {
      //      @Override
      //      public void onClick(View view) {
      //      }
      //  });
      //  mySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        //    @Override
       //     public boolean onQueryTextSubmit(String s) {
       //         adapter.getFilter().filter(s);
      //          return false;
       //     }
       //     @Override
       //     public boolean onQueryTextChange(String query) {
       //         adapter.getFilter().filter(query);
        //        return false;
        //    }
      //  });
        spacecrafts=new JSONDownloader(MainActivity.this).retrieve(myListView,myProgressBar);
        adapter=new ListViewAdapter(this,spacecrafts);
        myListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                adapter.getFilter().filter(s);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

//        spacecrafts=new JSONDownloader(MainActivity.this).retrieve(myListView, myProgressBar);
//        adapter=new ListViewAdapter(this,spacecrafts);
//        myListView.setAdapter(adapter);
    }
    }

